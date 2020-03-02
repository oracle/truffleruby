# frozen_string_literal: true

# Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Used by Truffle::Interop.lookup_symbol
Truffle::Interop::MAIN = self

module Truffle
  module Interop

    # FIXME (pitr-ch 02-Mar-2020): interop methods should call regular Ruby conversions like to_s (which should try asString if foreign.isString)

    -> do # stubs, defined in CoreLibrary
      UnsupportedMessageException = Class.new Exception
      InvalidArrayIndexException = Class.new Exception
      UnknownIdentifierException = Class.new Exception
      UnsupportedTypeException = Class.new Exception
    end

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

    def self.get_members_implementation(object, internal)
      keys = []

      if object.respond_to? :polyglot_members
        keys = object.polyglot_members internal
      else
        add_method_key = proc do |method|
          # do not list methods which cannot be read using interop
          keys << method.to_s if Primitive.object_respond_to? object, method, true
        end

        object.public_methods.each(&add_method_key)

        if internal
          object.instance_variables.each do |ivar|
            ivar_string = ivar.to_s
            keys << ivar_string if ivar_string.start_with?('@')
          end

          object.protected_methods.each(&add_method_key)
          object.private_methods.each(&add_method_key)
        end
      end

      keys.map { |s| Truffle::Interop.to_java_string(s) }
    end

    private_class_method :get_members_implementation

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
        Truffle::Interop.array_size(foreign)
      end

    end

    def self.enumerable(foreign)
      ForeignEnumerable.new(foreign)
    end

    class Foreign

      # Currently you cannot add methods here, as method calls on this class
      # (when the object is indeed foreign) are sent as interop messages,
      # rather than looking them up in the class.

    end

    def self.java_array(*array)
      to_java_array(array)
    end

    def self.to_java_array(array)
      java_array = Primitive.interop_to_java_array(array)
      if !Primitive.undefined?(java_array)
        java_array
      else
        to_java_array(Truffle::Type.coerce_to(array, Array, :to_a))
      end
    end

    def self.to_java_list(array)
      list = Primitive.interop_to_java_list(array)
      if !Primitive.undefined?(list)
        list
      else
        to_java_list(Truffle::Type.coerce_to(array, Array, :to_a))
      end
    end

    def self.foreign_is_a?(receiver, klass)
      receiver = Truffle::Interop.unbox_if_needed(receiver)
      if Truffle::Interop.foreign?(receiver)
        if Truffle::Interop.java_class?(klass)
          # Checking against a Java class
          Truffle::Interop.java_instanceof?(receiver, klass)
        elsif Truffle::Interop.foreign?(klass)
          # Checking a foreign (not Java) object against a foreign (not Java) class
          raise TypeError, 'cannot check if a foreign object is an instance of a foreign class'
        else
          # Checking a foreign or Java object against a Ruby class
          false
        end
      else
        # The receiver unboxed to a Ruby object or a primitive
        receiver.is_a?(klass)
      end
    end

    def self.foreign_inspect(object)
      object = Truffle::Interop.unbox_if_needed(object)
      hash_code = "0x#{Truffle::Interop.identity_hash_code(object).to_s(16)}"
      if object.is_a?(String)
        object.inspect
      elsif Truffle::Interop.java?(object)
        if object.nil?
          '#<Java null>'
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
        return +'#<Foreign null>' if Truffle::Interop.null?(object)

        string = +'#<Foreign'
        if Truffle::Interop.pointer?(object)
          string << " pointer 0x#{Truffle::Interop.as_pointer(object).to_s(16)}"
        else
          string << ":#{hash_code}"
        end

        if Truffle::Interop.size?(object)
          string << " #{to_array(object).inspect}"
        end
        if Truffle::Interop.keys?(object)
          string << " #{pairs_from_object(object).map { |k, v| "#{k.inspect}=#{v.inspect}" }.join(', ')}"
        end
        if Truffle::Interop.executable?(object)
          string << ' proc'
        end
        string << '>'
      end

    end

    def self.foreign_class(receiver, *args)
      if Truffle::Interop.java_class?(receiver)
        Truffle::Interop.read_member(receiver, :class)
      else
        Truffle::Interop.invoke(receiver, :class, *args)
      end
    end

    def self.foreign_to_s(receiver)
      receiver = Truffle::Interop.unbox_if_needed(receiver)
      receiver.is_a?(String) ? receiver : receiver.inspect
    end

    def self.foreign_to_str(object)
      # object = as_string object if is_string? object
      object = Truffle::Interop.unbox_if_needed(object)
      raise NameError, 'no method to_str' unless object.is_a?(String)
      object
    end

    def self.foreign_respond_to?(object, name)
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

      Array.new(Truffle::Interop.array_size(object)) do |n|
        Truffle::Interop.read_array_element(object, n)
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
      boolean?(object) || is_string?(object) || is_number?(object)
    end

    # TODO (pitr-ch 01-Apr-2019): remove
    def self.unbox(object)
      return as_boolean object if boolean? object
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
      return as_boolean object if boolean? object
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
