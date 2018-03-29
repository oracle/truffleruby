# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle

  module Interop

    def self.import_method(name)
      method = import(name.to_s)

      Object.send(:define_method, name.to_sym) do |*args|
        Truffle::Interop.execute(method, *args)
      end
    end

    def self.export_method(name)
      export(name.to_s, Object.method(name.to_sym))
    end

    def self.keys(object, internal = false)
      keys = Truffle.invoke_primitive :interop_send_keys, object, internal
      enumerable(keys).map { |key| from_java_string(key) }
    end

    def self.object_keys(object, internal)
      if object.is_a?(::Array)
        (0...object.size).to_a
      elsif object.is_a?(Hash)
        object.keys.map(&:to_s).map { |s| Truffle::Interop.to_java_string(s) }
      else
        keys = object.methods.map(&:to_s)
        if internal
          keys += object.instance_variables
            .map(&:to_s)
            .select { |ivar| ivar.start_with?('@') }
        end
        keys.map { |s| Truffle::Interop.to_java_string(s) }
      end
    end
    
    def self.key_info(object, name)
      key_info_flags_from_bits(key_info_bits(object, name))
    end
    
    def self.object_key_info(object, name)
      readable, invocable, internal, insertable, modifiable, removable = false, false, false, false, false, false
      
      if object.is_a?(::Array)
        in_bounds = name.is_a?(Integer) && name >= 0 && name < object.size
        readable = in_bounds
        insertable = in_bounds && !object.frozen?
        modifiable = insertable
      elsif object.is_a?(Hash)
        frozen = object.frozen?
        has_key = object.has_key?(name)
        readable = has_key
        modifiable = has_key && !frozen
        removable = modifiable
        insertable = !frozen
      elsif name.start_with?('@')
        frozen = object.frozen?
        exists = object.instance_variable_defined?(name)
        readable = exists
        insertable = !frozen
        modifiable = exists && !frozen
        removable = modifiable
        internal = true
      else
        method = object.respond_to?(name)
        readable = method || object.respond_to?(:[])
        insertable = object.respond_to?(:[]=)
        modifiable = insertable
        invocable = method
      end
      
      key_info_flags_to_bits(readable, invocable, internal, insertable, modifiable, removable)
    end
    
    def self.key_info_flags_from_bits(bits)
      flags = []
      flags << :existing    if existing_bit?(bits)
      flags << :readable    if readable_bit?(bits)
      flags << :writable    if writable_bit?(bits)
      flags << :invocable   if invocable_bit?(bits)
      flags << :internal    if internal_bit?(bits)
      flags << :removable   if removable_bit?(bits)
      flags << :modifiable  if modifiable_bit?(bits)
      flags << :insertable  if insertable_bit?(bits)
      flags
    end
    
    def self.lookup_symbol(name)
      if MAIN.respond_to?(name, true)
        MAIN.method(name)
      elsif Truffle::SymbolOperations.is_constant?(name) && Object.const_defined?(name)
        Object.const_get(name)
      else
        nil
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
      to_java_array(Truffle::Type.coerce_to(array, ::Array, :to_a))
    end
    
    def self.special_form(receiver, name, *args)
      case name.to_sym
      when :delete
        Truffle::Interop.remove(receiver, *args)
      when :size
        Truffle::Interop.size(receiver)
      when :keys
        Truffle::Interop.keys(receiver)
      else
        raise
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
      else
        false
      end
    end
    
    def self.to_array(object)
      unless Truffle::Interop.size?(object)
        raise 'foreign object does not have a size to turn it into an array'
      end
      
      ::Array.new(Truffle::Interop.size(object)) do |n|
        Truffle::Interop.read(object, n)
      end
    end
    
    def self.from_java_array(array)
      to_array(array)
    end
    
  end

end
