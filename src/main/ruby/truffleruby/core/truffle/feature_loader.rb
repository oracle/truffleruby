# frozen_string_literal: true

# Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module FeatureLoader

    DOT_DLEXT = ".#{Truffle::Platform::DLEXT}"

    # FeatureEntry => [*index_inside_$LOADED_FEATURES]
    @loaded_features_index = {}
    # A snapshot of $LOADED_FEATURES, to check if the @loaded_features_index cache is up to date.
    @loaded_features_copy = []

    @expanded_load_path = []
    # A snapshot of $LOAD_PATH, to check if the @expanded_load_path cache is up to date.
    @load_path_copy = []
    # nil if there is no relative path in $LOAD_PATH, a copy of the cwd to check if the cwd changed otherwise.
    @working_directory_copy = nil

    def self.clear_cache
      @loaded_features_index.clear
      @loaded_features_copy.clear
      @expanded_load_path.clear
      @load_path_copy.clear
      @working_directory_copy = nil
    end

    class FeatureEntry
      attr_reader :feature, :ext, :feature_no_ext
      attr_accessor :part_of_index

      def initialize(feature)
        @part_of_index = false
        @ext = Truffle::FeatureLoader.extension(feature)
        @feature = feature
        @feature_no_ext = @ext ? feature[0...(-@ext.size)] : feature
        @base = File.basename(@feature_no_ext)
      end

      def ==(other)
        # The looked up feature has to be the trailing part of an already-part_of_index entry.
        # We always want to check part_of_index_feature.end_with?(lookup_feature).
        # We compare extensions only if the lookup_feature has an extension.

        if @part_of_index
          stored = self
          lookup = other
        elsif other.part_of_index
          stored = other
          lookup = self
        else
          raise 'Expected that at least one of the FeatureEntry instances is part of the index'
        end

        if lookup.ext
          stored.feature.end_with?(lookup.feature)
        else
          stored.feature_no_ext.end_with?(lookup.feature_no_ext)
        end
      end
      alias_method :eql?, :==

      def hash
        @base.hash
      end
    end

    def self.find_file(feature)
      feature = File.expand_path(feature) if feature.start_with?('~')
      Primitive.find_file(feature)
    end

    # MRI: search_required
    def self.find_feature_or_file(feature, use_feature_provided = true)
      feature_ext = extension_symbol(feature)
      if feature_ext
        case feature_ext
        when :rb
          if use_feature_provided && feature_provided?(feature, false)
            return [:feature_loaded, nil, :rb]
          end
          path = find_file(feature)
          return expanded_path_provided(path, :rb, use_feature_provided) if path
          return [:not_found, nil, nil]
        when :so
          if use_feature_provided && feature_provided?(feature, false)
            return [:feature_loaded, nil, :so]
          else
            feature_no_ext = feature[0...-3] # remove ".so"
            path = find_file("#{feature_no_ext}.#{Truffle::Platform::DLEXT}")
            return expanded_path_provided(path, :so, use_feature_provided) if path
          end
        when :dlext
          if use_feature_provided && feature_provided?(feature, false)
            return [:feature_loaded, nil, :so]
          else
            path = find_file(feature)
            return expanded_path_provided(path, :so, use_feature_provided) if path
          end
        end
      else
        found = use_feature_provided && feature_provided?(feature, false)
        if found == :rb
          return [:feature_loaded, nil, :rb]
        else
          found = :so if found == :unknown
        end
      end

      path = find_file(feature)
      if path
        ext_normalized = extension_symbol(path) == :rb ? :rb : :so
        if found && ext_normalized != :rb
          [:feature_loaded, nil, found]
        else
          found_expanded = use_feature_provided && feature_provided?(path, true)
          if found_expanded
            [:feature_loaded, nil, ext_normalized]
          else
            [:feature_found, path, ext_normalized]
          end
        end
      else
        if found
          [:feature_loaded, nil, found]
        else
          found = use_feature_provided && feature_provided?(feature, true)
          if found
            found = :so if found == :unknown
            [:feature_loaded, nil, found]
          else
            [:not_found, nil, nil]
          end
        end
      end
    end

    def self.expanded_path_provided(path, ext, use_feature_provided)
      if use_feature_provided && feature_provided?(path, true)
        [:feature_loaded, path, ext]
      else
        [:feature_found, path, ext]
      end
    end

    # MRI: rb_feature_p
    # Whether feature is already loaded, i.e., part of $LOADED_FEATURES,
    # using the @loaded_features_index to lookup faster.
    # expanded is true if feature is an expanded path (and exists).
    def self.feature_provided?(feature, expanded)
      feature_ext = extension_symbol(feature)
      feature_has_rb_ext = feature_ext == :rb

      with_synchronized_features do
        get_loaded_features_index
        feature_entry = FeatureEntry.new(feature)
        if @loaded_features_index.key?(feature_entry)
          @loaded_features_index[feature_entry].each do |i|
            loaded_feature = $LOADED_FEATURES[i]

            next if loaded_feature.size < feature.size
            feature_path = if loaded_feature.start_with?(feature)
                             feature
                           else
                             if expanded
                               nil
                             else
                               loaded_feature_path(loaded_feature, feature, get_expanded_load_path)
                             end
                           end
            if feature_path
              loaded_feature_ext = extension_symbol(loaded_feature)
              if !loaded_feature_ext
                return :unknown unless feature_ext
              else
                if (!feature_has_rb_ext || !feature_ext) && binary_ext?(loaded_feature_ext)
                  return :so
                end
                if (feature_has_rb_ext || !feature_ext) && loaded_feature_ext == :rb
                  return :rb
                end
              end
            end
          end
        end

        false
      end
    end

    # MRI: loaded_feature_path
    # Search if $LOAD_PATH[i]/feature corresponds to loaded_feature.
    # Returns the $LOAD_PATH entry containing feature.
    def self.loaded_feature_path(loaded_feature, feature, load_path)
      name_ext = extension(loaded_feature)
      load_path.find do |p|
        loaded_feature == "#{p}/#{feature}#{name_ext}" || loaded_feature == "#{p}/#{feature}"
      end
    end

    # MRI: rb_provide_feature
    # Add feature to $LOADED_FEATURES and the index, called from RequireNode
    def self.provide_feature(feature)
      raise '$LOADED_FEATURES is frozen; cannot append feature' if $LOADED_FEATURES.frozen?
      #feature.freeze # TODO freeze these but post-boot.rb issue using replace
      with_synchronized_features do
        get_loaded_features_index
        $LOADED_FEATURES << feature
        features_index_add(feature, $LOADED_FEATURES.size - 1)
        @loaded_features_copy = $LOADED_FEATURES.dup
      end
    end

    def self.relative_feature(expanded_path)
      load_path_entry = get_expanded_load_path.find do |load_dir|
        expanded_path.start_with?(load_dir) and expanded_path[load_dir.size] == '/'
      end
      if load_path_entry
        before_dot_rb = expanded_path.end_with?('.rb') ? -4 : -1
        expanded_path[load_path_entry.size+1..before_dot_rb]
      else
        nil
      end
    end

    def self.binary_ext?(ext)
      ext == :so || ext == :dlext
    end

    # Done this way to avoid many duplicate Strings representing the file extensions
    def self.extension_symbol(path)
      if !Primitive.nil?(path)
        if path.end_with?('.rb')
          :rb
        elsif path.end_with?('.so')
          :so
        elsif path.end_with?(DOT_DLEXT)
          :dlext
        else
          ext = File.extname(path)
          ext.empty? ? nil : :other
        end
      else
        nil
      end
    end

    # Done this way to avoid many duplicate Strings representing the file extensions
    def self.extension(path)
      if !Primitive.nil?(path)
        if path.end_with?('.rb')
          '.rb'
        elsif path.end_with?('.so')
          '.so'
        elsif path.end_with?(DOT_DLEXT)
          DOT_DLEXT
        else
          ext = File.extname(path)
          ext.empty? ? nil : ext
        end
      else
        nil
      end
    end

    def self.with_synchronized_features
      TruffleRuby.synchronized($LOADED_FEATURES) do
        yield
      end
    end

    # MRI: get_loaded_features_index
    # always called inside #with_synchronized_features
    def self.get_loaded_features_index
      unless Primitive.array_storage_equal?(@loaded_features_copy, $LOADED_FEATURES)
        @loaded_features_index.clear
        $LOADED_FEATURES.map! do |val|
          val = StringValue(val)
          #val.freeze # TODO freeze these but post-boot.rb issue using replace
          val
        end
        $LOADED_FEATURES.each_with_index do |val, idx|
          features_index_add(val, idx)
        end
        @loaded_features_copy = $LOADED_FEATURES.dup
      end
      @loaded_features_index
    end

    # MRI: features_index_add
    # always called inside #with_synchronized_features
    def self.features_index_add(feature, offset)
      feature_entry = FeatureEntry.new(feature)
      if @loaded_features_index.key?(feature_entry)
        @loaded_features_index[feature_entry] << offset
      else
        @loaded_features_index[feature_entry] = [offset]
        feature_entry.part_of_index = true
      end
    end

    def self.get_expanded_load_path
      with_synchronized_features do
        unless Primitive.array_storage_equal?(@load_path_copy, $LOAD_PATH) && same_working_directory_for_load_path?
          @expanded_load_path = $LOAD_PATH.map do |path|
            path = Truffle::Type.coerce_to_path(path)
            unless @working_directory_copy
              unless File.absolute_path?(path)
                @working_directory_copy = Primitive.working_directory
              end
            end
            Primitive.canonicalize_path(path)
          end
          @load_path_copy = $LOAD_PATH.dup
        end
        @expanded_load_path
      end
    end

    def self.same_working_directory_for_load_path?
      if working_directory_copy = @working_directory_copy
        if Primitive.working_directory == working_directory_copy
          true
        else
          @working_directory_copy = Primitive.working_directory
          false
        end
      else
        true # no relative path in $LOAD_PATH, no need to check the working directory
      end
    end
  end
end
