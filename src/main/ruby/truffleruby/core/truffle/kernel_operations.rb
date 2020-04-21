# frozen_string_literal: true

# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module KernelOperations
    def self.to_enum_with_size(enum, method, size_method)
      enum.to_enum(method) { enum.send(size_method) }
    end

    def self.define_hooked_variable(name, getter, setter, defined = proc { 'global-variable' })
      getter = Truffle::Graal.always_split(getter) if getter.arity == 1
      setter = Truffle::Graal.always_split(setter) if setter.arity == 2
      defined = Truffle::Graal.always_split(defined) if defined.arity == 1
      define_hooked_variable_with_is_defined(name, getter, setter, defined)
    end

    def self.define_read_only_global(name, getter)
      setter = -> _ { raise NameError, "#{name} is a read-only variable." }
      define_hooked_variable(name, getter, setter)
    end

    LOAD_PATH = []
    LOADED_FEATURES = []

    define_read_only_global(:$LOAD_PATH, -> { LOAD_PATH })
    define_read_only_global(:$LOADED_FEATURES, -> { LOADED_FEATURES })

    alias $: $LOAD_PATH
    alias $-I $LOAD_PATH
    alias $" $LOADED_FEATURES

    # The runtime needs to access these values, so we want them to be set in the variable storage.
    Primitive.global_variable_set :$LOAD_PATH, LOAD_PATH
    Primitive.global_variable_set :$LOADED_FEATURES, LOADED_FEATURES

    define_read_only_global(:$*, -> { ARGV })

    define_read_only_global(:$-a, -> { Truffle::Boot.get_option 'split-loop' })
    define_read_only_global(:$-l, -> { Truffle::Boot.get_option 'chomp-loop' })
    define_read_only_global(:$-p, -> { Truffle::Boot.get_option 'print-loop' })

    define_hooked_variable(
      :$/,
      -> { Primitive.global_variable_get :$/ },
      -> v {
        if v && !Primitive.object_kind_of?(v, String)
          raise TypeError, '$/ must be a String'
        end
        Primitive.global_variable_set :$/, v
      })

    $/ = "\n".freeze

    Truffle::Boot.delay do
      if Truffle::Boot.get_option 'chomp-loop'
        $\ = $/
      end
    end

    alias $-0 $/

    define_hooked_variable(
      :'$,',
      -> { Primitive.global_variable_get :$, },
      -> v {
        if v && !Primitive.object_kind_of?(v, String)
          raise TypeError, '$, must be a String'
        end
        Primitive.global_variable_set :$,, v
      })

    $, = nil # It should be defined by the time boot has finished.

    $= = false

    define_hooked_variable(
      :$VERBOSE,
      -> { Primitive.global_variable_get :$VERBOSE },
      -> v {
        v = v.nil? ? nil : !!v
        Primitive.global_variable_set :$VERBOSE, v
      })

    Truffle::Boot.redo do
      $DEBUG = Truffle::Boot.get_option_or_default('debug', false)
      $VERBOSE = case Truffle::Boot.get_option_or_default('verbose', false)
                 when :TRUE
                   true
                 when :FALSE
                   false
                 when :NIL
                   nil
                 end
    end

    alias $-d $DEBUG
    alias $-v $VERBOSE
    alias $-w $VERBOSE

    define_hooked_variable(
      :$stdout,
      -> { Primitive.global_variable_get :$stdout },
      -> v {
        raise TypeError, "$stdout must have a write method, #{v.class} given" unless v.respond_to?(:write)
        Primitive.global_variable_set :$stdout, v
      })

    alias $> $stdout

    define_hooked_variable(
      :$stderr,
      -> { Primitive.global_variable_get :$stderr },
      -> v {
        raise TypeError, "$stderr must have a write method, #{v.class} given" unless v.respond_to?(:write)
        Primitive.global_variable_set :$stderr, v
      })

    def self.load_error(name)
      load_error = LoadError.new("cannot load such file -- #{name}")
      load_error.path = name
      load_error
    end

    def self.internal_raise(exc, msg, ctx, internal)
      skip = false
      if Primitive.undefined? exc
        exc = $!
        if exc
          skip = true
        else
          exc = RuntimeError.new ''
        end
      elsif exc.respond_to? :exception
        if Primitive.undefined? msg
          exc = exc.exception
        else
          exc = exc.exception msg
        end
        raise TypeError, 'exception class/object expected' unless exc.kind_of?(Exception)
      elsif exc.kind_of? String
        exc = RuntimeError.exception exc
      else
        raise TypeError, 'exception class/object expected'
      end

      unless skip
        exc.set_context ctx if ctx
        exc.capture_backtrace!(2) unless exc.backtrace?
        Primitive.exception_set_cause exc, $! unless exc.equal?($!)
      end

      if $DEBUG
        STDERR.puts "Exception: `#{exc.class}' #{caller(2, 1)[0]} - #{exc.message}\n"
      end

      Primitive.vm_raise_exception exc, internal
    end

    @loaded_features_index = {}
    @loaded_features_copy = []

    def self.is_so_ext(ext)
      ext == '.so' || ext == '.o'
    end

    def self.is_dl_ext(ext)
      ext == '.bundle'
    end

    # MRI: search_required
    def self.find_feature_or_file(feature)
      feature_ext = extension(feature)
      if feature_ext
        if feature_ext == '.rb'
          if Truffle::KernelOperations.feature_provided?(feature, false)
            return [:feature_loaded, nil]
          end
          path = Primitive.find_file(feature)
          if path
            if Truffle::KernelOperations.feature_provided?(path, true)
              return [:feature_loaded, nil]
            else
              return [:feature_found, path]
            end
          end
          return [:not_found, nil]
        elsif is_so_ext(feature_ext)
          if Truffle::KernelOperations.feature_provided?(feature, false)
            return [:feature_loaded, nil]
          else
            feature_no_ext = [0...(-feature_ext.size)]
            path = Primitive.find_file(feature_no_ext)
            if path
              return [:feature_found, path]
            end
          end
        elsif is_dl_ext(feature_ext)
          if Truffle::KernelOperations.feature_provided?(feature, false)
            return [:feature_loaded, nil]
          else
            path = Primitive.find_file(feature)
            if path
              return [:feature_found, path]
            end
          end
        end
      else
        if (found = Truffle::KernelOperations.feature_provided?(feature, false)) == :r
          return [:feature_loaded, nil]
        end
      end
      path = Primitive.find_file(feature)
      if path
        if Truffle::KernelOperations.feature_provided?(path, true)
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
                return :u
              end
            else
              loaded_feature_ext = extension(loaded_feature)
              if (!feature_has_rb_ext || !feature_has_ext) && is_binary_ext(loaded_feature_ext)
                return :s
              end
              if (feature_has_rb_ext || !feature_has_ext) && loaded_feature_ext == '.rb'
                return :r
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

    def self.is_binary_ext(ext)
      ext == '.so' || ext == '.bundle' || ext == '.o'
    end

    def self.has_extension?(path)
      !path.nil? && path.rindex('.') && (path.rindex('/').nil? || path.rindex('.') > path.rindex('/'))
    end

    # MRI: get_loaded_features_index
    def self.get_loaded_features_index
      #if !Primitive.array_storage_equal?(@loaded_features_copy, $LOADED_FEATURES)
      if @loaded_features_copy.hash != $LOADED_FEATURES.hash
        @loaded_features_index.clear
        $LOADED_FEATURES.map! do |val|
          val = StringValue(val)
          val.freeze
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
      feature.freeze
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
      if !path.nil? && path.rindex('.') && (path.rindex('/').nil? || path.rindex('.') >  path.rindex('/'))
        path[(path.rindex('.'))..-1]
      else
        nil
      end
    end

    def self.update_loaded_features_snapshot
      @loaded_features_copy = $LOADED_FEATURES.dup
    end

    # MRI: features_index_add
    def self.features_index_add(feature, index)
      feature, ext = if feature.rindex('.') && (feature.rindex('/').nil? || feature.rindex('.') > feature.rindex('/'))
                       [feature[0...feature.rindex('.')], feature[feature.rindex('.')..-1]]
                     else
                       [feature, nil]
                     end
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
      if @loaded_features_index.has_key?(feature)
        @loaded_features_index[feature] << offset
      else
        @loaded_features_index[feature] = [offset]
      end
    end

    def self.check_last_line(line)
      unless Primitive.object_kind_of? line, String
        raise TypeError, "$_ value need to be String (#{Truffle::ExceptionOperations.to_class_name(line)} given)"
      end
      line
    end

    # Will throw an exception if the arguments are invalid, and potentially convert a range to [omit, length] format.
    def self.normalize_backtrace_args(omit, length)
      if Integer === length && length < 0
        raise ArgumentError, "negative size (#{length})"
      end
      if Range === omit
        range = omit
        omit = Truffle::Type.coerce_to_int(range.begin)
        unless range.end.nil?
          end_index = Truffle::Type.coerce_to_int(range.end)
          if end_index < 0
            length = end_index
          else
            end_index += (range.exclude_end? ? 0 : 1)
            length = omit > end_index ? 0 : end_index - omit
          end
        end
      end
      [omit, length]
    end

    KERNEL_FROZEN = Kernel.instance_method(:frozen?)
    private_constant :KERNEL_FROZEN

    # Returns whether the value is frozen, even if the value's class does not include `Kernel`.
    def self.value_frozen?(value)
      KERNEL_FROZEN.bind(value).call
    end

    # To get the class even if the value's class does not inlucde `Kernel`, use `Truffle::Type.object_class`.
  end
end
