module Truffle
  module FeatureLoader

    @loaded_features_index = {}
    @loaded_features_copy = []

    def self.so_ext?(ext)
      ext == '.so'
    end

    def self.dl_ext?(ext)
      ext == ".#{Truffle::Platform::DLEXT}"
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
          if path
            if feature_provided?(path, true)
              return [:feature_loaded, nil]
            else
              return [:feature_found, path]
            end
          end
          return [:not_found, nil]
        elsif so_ext?(feature_ext)
          if feature_provided?(feature, false)
            return [:feature_loaded, nil]
          else
            feature_no_ext = feature[0...(-feature_ext.size)]
            path = find_file("#{feature_no_ext}.#{Truffle::Platform::DLEXT}")
            if path
              if feature_provided?(path, true)
                return [:feature_loaded, nil]
              else
                return [:feature_found, path]
              end
            end
          end
        elsif dl_ext?(feature_ext)
          if feature_provided?(feature, false)
            return [:feature_loaded, nil]
          else
            path = find_file(feature)
            if path
              if feature_provided?(path, true)
                return [:feature_loaded, nil]
              else
                return [:feature_found, path]
              end
            end
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
      loaded_features_index = get_loaded_features_index
      if loaded_features_index.has_key?(feature)
        loaded_features_index[feature].each do |i|
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

    def self.binary_ext?(ext)
      so_ext?(ext) || dl_ext?(ext)
    end

    def self.has_extension?(path)
      !path.nil? && !File.extname(path).empty?
    end

    # MRI: get_loaded_features_index
    def self.get_loaded_features_index
      #if !Primitive.array_storage_equal?(@loaded_features_copy, $LOADED_FEATURES)
      if @loaded_features_copy.hash != $LOADED_FEATURES.hash
        TruffleRuby.synchronized(@loaded_features_index) do
          @loaded_features_index.clear
        end

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
      $LOADED_FEATURES << feature
      features_index_add(feature, $LOADED_FEATURES.size - 1)
      update_loaded_features_snapshot
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
    def self.features_index_add(feature, index)
      ext = extension(feature)
      feature = feature[0...(-ext.size)] if ext
      starting_slash = feature.start_with?('/')
      feature_split = feature.split('/').delete_if(&:empty?)
      path = []
      feature_split.reverse_each do |part|
        path.unshift part
        features_index_add_single(path.join('/'), index)
        if ext
          features_index_add_single("#{path.join('/')}#{ext}", index)
        end
      end
      if starting_slash
        features_index_add_single("/#{path.join('/')}", index)
        if ext
          features_index_add_single("/#{path.join('/')}#{ext}", index)
        end
      end
    end

    # MRI: features_index_add_single
    def self.features_index_add_single(feature, offset)
      TruffleRuby.synchronized(@loaded_features_index) do
        if @loaded_features_index.has_key?(feature)
          @loaded_features_index[feature] << offset
        else
          @loaded_features_index[feature] = [offset]
        end
      end
    end

  end
end
