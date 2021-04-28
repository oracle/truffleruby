# frozen_string_literal: true

# Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
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

    # stubs, defined in CoreLibrary
    # rubocop:disable Lint/InheritException
    class InteropException < Exception; end
    class UnsupportedMessageException < InteropException; end
    class InvalidArrayIndexException < InteropException; end
    class UnknownIdentifierException < InteropException; end
    class UnsupportedTypeException < InteropException; end
    class UnknownKeyException < InteropException; end
    class ArityException < InteropException
      attr_reader :expected

      def initialize(expected)
        @expected = expected
        raise ArgumentError unless expected.is_a? Integer
      end
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

    def self.members(object, internal = false)
      keys = members_without_conversion(object, internal)
      enumerable(keys).map { |key| from_java_string(key) }
    end

    def self.get_members_implementation(object, internal)
      keys = []

      if object.respond_to?(:polyglot_members, true)
        keys = object.__send__(:polyglot_members, internal)
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

    class HashKeysAsPolyglotMembers
      def initialize(hash)
        raise ArgumentError, 'expected a Hash' unless Hash === hash
        # Otherwise Symbol keys won't be seen from e.g., readMember()
        @hash = hash.transform_keys do |key|
          if Symbol === key
            key.to_s
          else
            key
          end
        end
      end

      private

      def polyglot_has_members?
        true
      end

      def polyglot_members(internal)
        @hash.keys
      end

      def polyglot_read_member(name)
        @hash.fetch(name) { raise Truffle::Interop::UnknownIdentifierException }
      end

      def polyglot_write_member(name, value)
        @hash[name] = value
      end

      def polyglot_remove_member(name)
        @hash.delete(name) { raise Truffle::Interop::UnknownIdentifierException }
      end

      def polyglot_invoke_member(name, *args)
        @hash.fetch(name) { raise Truffle::Interop::UnknownIdentifierException }.call(*args)
      end

      def polyglot_member_readable?(name)
        @hash.key? name
      end

      def polyglot_member_modifiable?(name)
        @hash.key? name
      end

      def polyglot_member_removable?(name)
        @hash.key? name
      end

      def polyglot_member_insertable?(name)
        !@hash.key? name
      end

      def polyglot_member_invocable?(name)
        value = @hash.fetch(name) { return false }
        Truffle::Interop.executable?(value)
      end

      def polyglot_member_internal?(name)
        false
      end

      def polyglot_has_member_read_side_effects?(name)
        false
      end

      def polyglot_has_member_write_side_effects?(name)
        false
      end
    end

    def self.hash_keys_as_members(hash)
      HashKeysAsPolyglotMembers.new(hash)
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

    private_class_method def self.basic_inspect_for(object)
      if string?(object)
        object.inspect
      elsif has_array_elements?(object)
        '[...]'
      elsif (java?(object) && java_map?(object)) || has_members?(object)
        '{...}'
      else
        object.inspect
      end
    end

    private_class_method def self.recursive_string_for(object)
      if has_array_elements?(object)
        '[...]'
      elsif java?(object) && java_map?(object) || has_members?(object)
        '{...}'
      else
        # This last case should not currently be hit, but could be if we extend inspect with new cases.
        hash_code = "0x#{Truffle::Interop.identity_hash_code(object).to_s(16)}"
        java?(object) ? "<Java:#{hash_code} ...>" : "<Foreign:#{hash_code} ...>"
      end
    end

    private_class_method def self.foreign_inspect_nonrecursive(object)
      object = Truffle::Interop.unbox_if_needed(object)

      hash_code = "0x#{Truffle::Interop.identity_hash_code(object).to_s(16)}"
      language = Truffle::Interop.language(object) || 'Foreign'

      if object.is_a?(String)
        object.inspect
      elsif Truffle::Interop.null?(object)
        "#<#{language} null>"
      elsif Truffle::Interop.java_class?(object)
        "#<#{language} class #{object.class.getName}>"
      else
        string = +"#<#{language}"
        meta_object = Truffle::Interop.meta_object(object)
        unless Truffle::Interop::Foreign.equal?(meta_object) # no meta object
          string << " #{Truffle::Interop.meta_qualified_name meta_object}"
        end

        if Truffle::Interop.pointer?(object)
          string << " pointer 0x#{Truffle::Interop.as_pointer(object).to_s(16)}"
        else
          string << ":#{hash_code}"
        end

        array_or_map = false
        if Truffle::Interop.has_array_elements?(object)
          array_or_map = true
          string << " [#{to_array(object).map { |e| basic_inspect_for e }.join(', ')}]"
        end
        if java_map?(object)
          array_or_map = true
          string << " {#{pairs_from_java_map(object).map { |k, v| "#{basic_inspect_for k}=>#{basic_inspect_for v}" }.join(', ')}}"
        end
        if Truffle::Interop.has_members?(object) and !array_or_map
          pairs = pairs_from_object(object)
          unless pairs.empty?
            string << " #{pairs.map { |k, v| "#{k}=#{basic_inspect_for v}" }.join(', ')}"
          end
        end
        if Truffle::Interop.executable?(object)
          string << ' proc'
        end

        string << '>'
      end
    end

    def self.foreign_inspect(object)
      return recursive_string_for(object) if Truffle::ThreadOperations.detect_recursion self do
        return foreign_inspect_nonrecursive(object)
      end
    end

    def self.foreign_to_s(object)
      object = Truffle::Interop.unbox_if_needed(object)
      if object.is_a?(String)
        object
      else
        language = Truffle::Interop.language(object) || 'Foreign'
        # Let InteropLibrary#toDisplayString show the class and identity hash code if relevant
        "#<#{language} #{Truffle::Interop.to_display_string(object)}>"
      end
    end

    def self.foreign_to_str(object)
      # object = as_string object if string? object
      object = Truffle::Interop.unbox_if_needed(object)
      if object.is_a?(String)
        object
      else
        raise NoMethodError, 'no method to_str'
      end
    end

    def self.foreign_class(receiver, *args)
      if args.empty?
        if Truffle::Interop.java_class?(receiver)
          Truffle::Interop.read_member(receiver, :class)
        else
          Truffle::Interop.meta_object(receiver)
        end
      else
        Truffle::Interop.invoke_member(receiver, :class, *args)
      end
    end

    def self.foreign_respond_to?(object, name)
      case name.to_sym
      when :to_a, :to_ary
        Truffle::Interop.has_array_elements?(object)
      when :to_f
        Truffle::Interop.fits_in_double?(object) || Truffle::Interop.fits_in_long?(object)
      when :to_i
        Truffle::Interop.fits_in_int?(object) || Truffle::Interop.fits_in_long?(object)
      when :new
        Truffle::Interop.instantiable?(object)
      when :size
        Truffle::Interop.has_array_elements?(object)
      when :keys
        Truffle::Interop.has_members?(object)
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
      unless Truffle::Interop.has_array_elements?(object)
        raise 'foreign object returns false for hasArrayElements() and cannot be converted into an array'
      end

      Array.new(Truffle::Interop.array_size(object)) do |n|
        Truffle::Interop.read_array_element(object, n)
      end
    end

    def self.from_java_array(array)
      to_array(array)
    end

    def self.pairs_from_java_map(map)
      enumerable(map.entrySet.toArray).map do |key_value|
        [key_value.getKey, key_value.getValue]
      end
    end

    def self.to_hash(object)
      Hash[*pairs_from_object(object)]
    end

    def self.pairs_from_object(object)
      readable_members = members(object).select { |member| Truffle::Interop.member_readable?(object, member) }
      readable_members.map { |key| [key, object[key]] }
    end

    def self.unbox_if_needed(object)
      if Truffle::Interop.foreign?(object) && Truffle::Interop.boxed?(object)
        Truffle::Interop.unbox(object)
      else
        object
      end
    end

    def self.boxed?(object)
      boolean?(object) || string?(object) || number?(object)
    end

    def self.unbox(object)
      return as_boolean object if boolean? object
      return as_string object if string? object

      if number?(object)
        return as_int object if fits_in_int? object
        return as_long object if fits_in_long? object
        return as_double object if fits_in_double? object
      end

      raise ArgumentError, "not boxed: #{object}"
    end

    def self.unbox_without_conversion(object)
      return as_boolean object if boolean? object
      return as_string_without_conversion object if string? object

      if number?(object)
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
