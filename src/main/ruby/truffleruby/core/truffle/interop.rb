# frozen_string_literal: true

# Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Used by Truffle::Interop.lookup_symbol
Truffle::Interop::MAIN = self

module Truffle
  module Interop

    def self.import_method(name)
      method = import(name.to_s)

      Object.define_method(name.to_sym) do |*args|
        Truffle::Interop.execute(method, *args)
      end
    end

    def self.export_method(name)
      export(name.to_s, Object.method(name.to_sym))
    end

    def self.export(name, value)
      export_without_conversion name, to_java_string(value)
      value
    end

    def self.import(name)
      from_java_string(import_without_conversion(name))
    end

    def self.keys(object, internal = false)
      keys = keys_without_conversion(object, internal)
      enumerable(keys).map { |key| from_java_string(key) }
    end

    def self.object_keys(object, internal)
      # TODO (pitr-ch 23-May-2019): add assert that called methods are in members list
      if object.is_a?(Hash)
        keys = object.keys # FIXME (pitr-ch 11-May-2019): no return methods
      elsif object.respond_to?(:[]) && !object.is_a?(Array)
        # FIXME (pitr-ch 11-May-2019): remove the branch
        keys = []
      else
        keys = object.methods.map(&:to_s)
        if internal
          keys += object.instance_variables
            .map(&:to_s)
            .select { |ivar| ivar.start_with?('@') }
          keys += object.private_methods.map(&:to_s)
        end
      end
      keys.map { |s| Truffle::Interop.to_java_string(s) }
    end

    # FIXME (pitr-ch 01-Apr-2019): remove
    def self.key_info(object, name)
      flags = []

      [:existing, :readable, :writable, :removable, :modifiable, :insertable].each do |info_name|
        add = begin
          send "is_member_#{info_name}?", object, name
        rescue TypeError # rubocop:disable Lint/HandleExceptions
          # ignore
        end

        add ||= begin
          send "is_array_element_#{info_name}?", object, name
        rescue TypeError # rubocop:disable Lint/HandleExceptions
          # ignore
        end

        flags << info_name if add
      end

      [:invocable, :internal].each do |info_name|
        add = begin
          send "is_member_#{info_name}?", object, name
        rescue TypeError # rubocop:disable Lint/HandleExceptions
          # ignore
        end

        flags << info_name if add
      end

      flags
    end

    HASH_PUBLIC_METHODS = Hash.public_instance_methods.map(&:to_s)
    private_constant :HASH_PUBLIC_METHODS

    # FIXME (pitr-ch 11-May-2019): breakdown
    def self.object_key_info(object, name)
      readable, invocable, internal, insertable, modifiable, removable = false, false, false, false, false, false

      if object.is_a?(Hash)
        if HASH_PUBLIC_METHODS.include? name
          # all the methods have to be readable and invocable otherwise they cannot be invoked from C
          readable = true
          invocable = true
        else
          frozen = object.frozen?
          has_key = object.has_key?(name)
          readable = true # has_key # TODO (pitr-ch 13-May-2019): do not allow to read non existing keys
          modifiable = has_key && !frozen
          removable = !frozen # modifiable # TODO (pitr-ch 22-May-2019): do not have removal of non-existing key as a noop
          insertable = !frozen
        end
      elsif object.is_a?(Array) && name.is_a?(Integer)
        in_bounds = name >= 0 && name < object.size
        frozen = object.frozen?
        readable = true # in_bounds # FIXME (pitr-ch 13-May-2019): should be readable only when in bounds
        modifiable = in_bounds && !frozen
        removable = !frozen # TODO (pitr-ch 22-May-2019): should not allow to remove non existing elements as noop
        insertable = !frozen
      elsif name.is_a?(String) && name.start_with?('@')
        frozen = object.frozen?
        exists = object.instance_variable_defined?(name)
        readable = true # exists # FIXME (pitr-ch 13-May-2019): should be readable only when defined
        insertable = !exists && !frozen
        modifiable = exists && !frozen
        removable = modifiable
        internal = true
      else
        # TODO (pitr-ch 11-May-2019): method should be removable?
        if name.is_a?(String) && object.respond_to?(name)
          invocable = readable = true
          modifiable = insertable = false
        elsif name.is_a?(String) && object.private_methods.include?(name.to_sym)
          invocable = readable = true
          modifiable = insertable = false
          internal = true
        else
          unless object.is_a?(Array)
            # FIXME (pitr-ch 11-May-2019): remove [] mapping to members
            readable = object.respond_to?(:[])
            modifiable = insertable = object.respond_to?(:[]=)
            invocable = false
          end
        end
      end

      # p object: object, name: name, readable: readable, invocable: invocable, internal: internal, insertable: insertable, modifiable: modifiable, removable: removable

      key_info_flags_to_bits(readable, invocable, internal, insertable, modifiable, removable)
    end

    def self.lookup_symbol(name)
      if MAIN.respond_to?(name, true)
        MAIN.method(name)
      elsif Truffle::SymbolOperations.is_constant?(name) && Object.const_defined?(name)
        Object.const_get(name)
      else
        undefined
      end
    end

    class ForeignEnumerable
      include Enumerable

      attr_reader :foreign

      def initialize(foreign)
        @foreign = foreign
      end

      def each
        (0...size).each do |n|
          yield foreign[n]
        end
      end

      def size
        Truffle::Interop.size(foreign)
      end

    end

    def self.enumerable(foreign)
      ForeignEnumerable.new(foreign)
    end

    class Foreign

      # Currently you cannot add methods here, as method calls on this class
      # (when the object is indeed foreign) are sent as interop messages,
      # rather than looking them up in the class. See #special_form, however.

    end

    def self.java_array(*array)
      to_java_array(array)
    end

    def self.to_java_array(array)
      Truffle.primitive :to_java_array
      to_java_array(Truffle::Type.coerce_to(array, Array, :to_a))
    end

    def self.to_java_list(array)
      Truffle.primitive :to_java_list
      to_java_list(Truffle::Type.coerce_to(array, Array, :to_a))
    end

    def self.special_form(receiver, name, *args)
      case name.to_sym
      when :delete
        Truffle::Interop.remove(receiver, *args)
      when :size
        Truffle::Interop.size(receiver)
      when :keys
        Truffle::Interop.keys(receiver)
      when :class
        if Truffle::Interop.java_class?(receiver)
          Truffle::Interop.read(receiver, :class)
        else
          Truffle::Interop.invoke(receiver, :class, *args)
        end
      when :inspect
        inspect_foreign(receiver)
      when :to_s
        receiver = Truffle::Interop.unbox_if_needed(receiver)
        if receiver.is_a?(String)
          receiver
        else
          receiver.inspect
        end
      when :to_str
        receiver = Truffle::Interop.unbox_if_needed(receiver)
        raise NameError, 'no method to_str' unless receiver.is_a?(String)
        receiver
      when :is_a?, :kind_of?
        receiver = Truffle::Interop.unbox_if_needed(receiver)
        check_class = args.first
        if Truffle::Interop.foreign?(receiver)
          if Truffle::Interop.java_class?(check_class)
            # Checking against a Java class
            Truffle::Interop.java_instanceof?(receiver, check_class)
          elsif Truffle::Interop.foreign?(check_class)
            # Checking a foreign (not Java) object against a foreign (not Java) class
            raise TypeError, 'cannot check if a foreign object is an instance of a foreign class'
          else
            # Checking a foreign or Java object against a Ruby class
            false
          end
        else
          # The receiver unboxed to a Ruby object or a primitive
          receiver.is_a?(check_class)
        end
      else
        raise
      end
    end

    def self.inspect_foreign(object)
      object = Truffle::Interop.unbox_if_needed(object)
      hash_code = "0x#{Truffle::Interop.identity_hash_code(object).to_s(16)}"
      if object.is_a?(String)
        object.inspect
      elsif Truffle::Interop.java?(object)
        if object.nil?
          +'#<Java null>'
        elsif Truffle::Interop.java_class?(object)
          "#<Java class #{object.class.getName}>"
        elsif object.respond_to?(:size)
          "#<Java:#{hash_code} #{to_array(object).inspect}>"
        elsif is_java_map?(object)
          "#<Java:#{hash_code} {#{pairs_from_java_map(object).map { |k, v| "#{k.inspect}=>#{v.inspect}" }.join(', ')}}>"
        else
          "#<Java:#{hash_code} object #{object.getClass.getName}>"
        end
      else
        if Truffle::Interop.null?(object)
          +'#<Foreign null>'
        elsif Truffle::Interop.pointer?(object)
          "#<Foreign pointer 0x#{Truffle::Interop.as_pointer(object).to_s(16)}>"
        elsif Truffle::Interop.size?(object)
          "#<Foreign:#{hash_code} #{to_array(object).inspect}>"
        elsif Truffle::Interop.executable?(object)
          "#<Foreign:#{hash_code} proc>"
        elsif Truffle::Interop.keys?(object)
          "#<Foreign:#{hash_code} #{pairs_from_object(object).map { |k, v| "#{k.inspect}=#{v.inspect}" }.join(', ')}>"
        else
          "#<Foreign:#{hash_code}>"
        end
      end
    end

    def self.respond_to?(object, name)
      case name.to_sym
      when :to_a, :to_ary
        Truffle::Interop.size?(object)
      when :new
        Truffle::Interop.instantiable?(object)
      when :size
        Truffle::Interop.size?(object)
      when :keys
        Truffle::Interop.keys?(object)
      when :call
        Truffle::Interop.executable?(object)
      when :class
        Truffle::Interop.java_class?(object)
      when :to_str
        object = Truffle::Interop.unbox_if_needed(object)
        !Truffle::Interop.foreign?(object) && object.is_a?(String)
      when :inspect, :to_s, :is_a?, :kind_of?
        true
      else
        false
      end
    end

    def self.to_array(object)
      unless Truffle::Interop.size?(object)
        raise 'foreign object does not have a size to turn it into an array'
      end

      Array.new(Truffle::Interop.size(object)) do |n|
        Truffle::Interop.read(object, n)
      end
    end

    def self.from_java_array(array)
      to_array(array)
    end

    def self.is_java_map?(object)
      object.is_a?(::Java.type('java.util.Map'))
    rescue RuntimeError
      false
    end
    private_class_method :is_java_map?

    def self.pairs_from_java_map(map)
      enumerable(map.entrySet.toArray).map do |key_value|
        [key_value.getKey, key_value.getValue]
      end
    end

    def self.to_hash(object)
      Hash[*pairs_from_object(object)]
    end

    def self.pairs_from_object(object)
      keys(object).map { |key| [key, object[key]] }
    end

    def self.unbox_if_needed(object)
      if Truffle::Interop.foreign?(object) && Truffle::Interop.boxed?(object)
        Truffle::Interop.unbox(object)
      else
        object
      end
    end

    # TODO (pitr-ch 01-Apr-2019): remove
    def self.boxed?(object)
      is_boolean?(object) || is_string?(object) || is_number?(object)
    end

    # TODO (pitr-ch 01-Apr-2019): remove
    def self.unbox(object)
      return as_boolean object if is_boolean? object
      return as_string object if is_string? object

      if is_number?(object)
        return as_int object if fits_in_int? object
        return as_long object if fits_in_long? object
        return as_double object if fits_in_double? object
      end

      raise ArgumentError, "not boxed: #{object}"
    end

    # TODO (pitr-ch 01-Apr-2019): remove
    def self.unbox_without_conversion(object)
      return as_boolean object if is_boolean? object
      return as_string_without_conversion object if is_string? object

      if is_number?(object)
        return as_int object if fits_in_int? object
        return as_long object if fits_in_long? object
        return as_double object if fits_in_double? object
      end

      raise ArgumentError, "not boxed: #{object.inspect}"
    end

    def self.to_java_map(hash)
      map = ::Java.type('java.util.HashMap').new
      hash.each do |key, value|
        map.put key, value
      end
      map
    end

  end

end
