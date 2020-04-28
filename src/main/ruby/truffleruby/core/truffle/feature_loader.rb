# frozen_string_literal: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module FeatureLoader

    DOT_DLEXT = ".#{Truffle::Platform::DLEXT}"

    @loaded_features_index = {}
    @loaded_features_copy = []

    class FeatureEntry
      attr_reader :feature, :ext, :key, :feature_no_ext

      def initialize(feature, key)
        @key = key
        @ext = Truffle::FeatureLoader.extension(feature)
        @feature = feature
        @feature_no_ext =  if @ext
                             feature[0...(-@ext.size)]
                           else
                             feature
                           end
        @base = @feature_no_ext.split('/').last
      end

      def ==(other)
        if other.key
          if self.ext
            other.feature.end_with?(self.feature)
          else
            other.feature_no_ext.end_with?(self.feature)
          end
        else
          if other.ext
            @feature.end_with?(other.feature)
          else
            @feature_no_ext.end_with?(other.feature)
          end
        end
      end

      alias eql? ==

      def hash
        @base.hash
      end

    end

    def self.find_file(feature)
      feature = File.expand_path(feature) if feature.start_with?('~')
      Primitive.find_file(feature)
    end

    # MRI: search_required
    def self.find_feature_or_file(feature)
      feature_ext = extension(feature)
      if feature_ext
        if feature_ext == '.rb'
          if feature_provided?(feature, false)
            return [:feature_loaded, nil]
          end
          path = find_file(feature)
          return expanded_path_provided(path) if path
          return [:not_found, nil]
        elsif feature_ext == '.so'
          if feature_provided?(feature, false)
            return [:feature_loaded, nil]
          else
            feature_no_ext = feature[0...(-feature_ext.size)]
            path = find_file("#{feature_no_ext}.#{Truffle::Platform::DLEXT}")
            return expanded_path_provided(path) if path
          end
        elsif feature_ext == DOT_DLEXT
          if feature_provided?(feature, false)
            return [:feature_loaded, nil]
          else
            path = find_file(feature)
            return expanded_path_provided(path) if path
          end
        end
      else
        if (found = feature_provided?(feature, false)) == :rb
          return [:feature_loaded, nil]
        end
      end
      path = find_file(feature)
      if path
        if feature_provided?(path, true)
          return [:feature_loaded, nil]
        else
          return [:feature_found, path]
        end
      else
        if found
          return [:feature_loaded, nil]
        end
        return [:not_found, nil]
      end
    end

    # MRI: rb_feature_p
    def self.feature_provided?(feature, expanded)
      feature_has_rb_ext = feature.end_with?('.rb')
      feature_has_ext = has_extension?(feature)
      with_synchronized_features do
        get_loaded_features_index
        feature_entry = FeatureEntry.new(feature, false)
        if @loaded_features_index.has_key?(feature_entry)
          @loaded_features_index[feature_entry].each do |i|
            loaded_feature = $LOADED_FEATURES[i]
            next if loaded_feature.size < feature.size
            feature_path = if loaded_feature.start_with?(feature)
                             feature
                           else
                             if expanded
                               nil
                             else
                               loaded_feature_path(loaded_feature, feature, $LOAD_PATH)
                             end
                           end
            if feature_path
              if !has_extension?(loaded_feature)
                if feature_has_ext
                  false
                else
                  return :unknown
                end
              else
                loaded_feature_ext = extension(loaded_feature)
                if (!feature_has_rb_ext || !feature_has_ext) && binary_ext?(loaded_feature_ext)
                  return :so
                end
                if (feature_has_rb_ext || !feature_has_ext) && loaded_feature_ext == '.rb'
                  return :rb
                end
              end
            else
              false
            end
          end
          false
        else
          false
        end
      end
    end

    def self.expanded_path_provided(path)
      if feature_provided?(path, true)
        [:feature_loaded, nil]
      else
        [:feature_found, path]
      end
    end

    def self.binary_ext?(ext)
      ext == '.so' || ext == DOT_DLEXT
    end

    def self.has_extension?(path)
      !path.nil? && !File.extname(path).empty?
    end

    # MRI: get_loaded_features_index
    def self.get_loaded_features_index
      if !Primitive.array_storage_equal?(@loaded_features_copy, $LOADED_FEATURES)
        @loaded_features_index.clear
        $LOADED_FEATURES.map! do |val|
          val = StringValue(val)
          #val.freeze # TODO freeze these but post-boot.rb issue using replace
          val
        end
        $LOADED_FEATURES.each_with_index do |val, idx|
          features_index_add(val, idx)
        end
        update_loaded_features_snapshot
      end
      @loaded_features_index
    end

    # MRI: rb_provide_feature
    def self.provide_feature(feature)
      raise '$LOADED_FEATURES is frozen; cannot append feature' if $LOADED_FEATURES.frozen?
      #feature.freeze # TODO freeze these but post-boot.rb issue using replace
      with_synchronized_features do
        get_loaded_features_index
        $LOADED_FEATURES << feature
        features_index_add(feature, $LOADED_FEATURES.size - 1)
        update_loaded_features_snapshot
      end
    end

    def self.with_synchronized_features
      TruffleRuby.synchronized($LOADED_FEATURES) do
        yield
      end
    end

    # MRI: loaded_feature_path
    def self.loaded_feature_path(name, feature, load_path)
      name_ext = extension(name)
      load_path.find do |p|
        name == "#{p}/#{feature}#{name_ext}" || name == "#{p}/#{feature}"
      end
    end

    def self.extension(path)
      if !path.nil?
        ext = File.extname(path)
        ext.empty? ? nil : ext
      else
        nil
      end
    end

    def self.update_loaded_features_snapshot
      @loaded_features_copy = $LOADED_FEATURES.dup
    end

    # MRI: features_index_add
    def self.features_index_add(feature, offset)
      feature_entry = FeatureEntry.new(feature, true)
      if @loaded_features_index.has_key?(feature_entry)
        @loaded_features_index[feature_entry] << offset
      else
        @loaded_features_index[feature_entry] = [offset]
      end
    end

  end
end
