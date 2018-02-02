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
    
    def self.object_has_keys(object)
      case object
      when NilClass, TrueClass, FalseClass, Fixnum, Bignum, Float, Symbol
        false
      else
        true
      end
    end

    def self.keys(object, internal = false)
      keys = Truffle.invoke_primitive :interop_send_keys, object, internal
      enumerable(keys).map { |key| from_java_string(key) }
    end

    def self.object_keys(object, internal)
      if object.is_a?(Hash)
        keys = object.keys.map(&:to_s)
      else
        keys = object.instance_variables.map do |ivar|
          ivar = ivar.to_s
          ivar = ivar[1..-1] if ivar.start_with?('@')
          ivar
        end
      end
      if internal
        object.instance_variables.each do |ivar|
          ivar = ivar.to_s
          if ivar.start_with?('@')
            keys << ivar
          end
        end
      end
      keys
    end
    
    def self.key_info(object, name)
      key_info_flags_from_bits(key_info_bits(object, name.to_sym))
    end
    
    def self.object_key_info(object, name)
      readable, writable, invocable, internal = false, false, false, false
      if name.to_s[0] == '@' && object.instance_variable_defined?(name)
        writable = true unless object.frozen?
        readable = true
        internal = true
      elsif object.is_a?(Hash)
        if object.key?(name)
          readable = true
          writable = true unless object.frozen?
        end
      else
        name = name.to_sym
        readable = object.respond_to?(name)
        writable = object.respond_to?(:"#{name}=") && !object.frozen?
      end
      existing = readable || writable || invocable || internal
      key_info_flags_to_bits(existing, readable, writable, invocable, internal)
    end
    
    def self.key_info_flags_from_bits(bits)
      flags = []
      flags << :existing  if existing_bit?(bits)
      flags << :readable  if readable_bit?(bits)
      flags << :writable  if writable_bit?(bits)
      flags << :invocable if invocable_bit?(bits)
      flags << :internal  if internal_bit?(bits)
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
      # rather than looking them up in the class.

    end
    
    class ObjectLiteral
    
      def method_missing(sent_name, *args)
        sent_name_s = sent_name.to_s
        if sent_name_s.end_with?('=')
          name = sent_name_s[0..-2].to_sym
        else
          name = sent_name
        end
        Truffle.privately { singleton_class.attr_accessor name }
        send sent_name, *args
      end
      
      # These are called for READ and WRITE on fields that haven't been
      # accessed yet, because they won't have methods defined yet and
      # interop doesn't call method_missing.
      
      def [](key)
        key = Interop.from_java_string(key) if Interop.java_string?(key)
        send key
      end
      
      def []=(key, value)
        key = Interop.from_java_string(key) if Interop.java_string?(key)
        send "#{key}=", value
      end
    
    end
    
    def self.object_literal(**fields)
      o = ObjectLiteral.new
      fields.each_pair do |key, value|
        o[key] = value
      end
      o
    end
    
    def self.java_array(*array)
      to_java_array(array)
    end
    
    def self.to_java_array(array)
      Truffle.primitive :to_java_array
      to_java_array(Truffle::Type.coerce_to(array, ::Array, :to_a))
    end
    
    def self.respond_to?(object, name)
      case name.to_sym
      when :to_a, :to_ary
        Truffle::Interop.size?(object)
      when :new
        Truffle::Interop.instantiable?(object)
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
    
  end

end
