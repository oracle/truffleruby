# Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
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

      Object.class_eval do
        define_method(name.to_sym) do |*args|
          ret = Truffle::Interop.execute(method, *args)
          Truffle::Interop.from_java_string(ret)
        end
      end
    end

    def self.export_method(name)
      export(name.to_s, Object.method(name.to_sym))
    end

    def self.ruby_object_keys(object)
      if object.is_a?(Hash)
        object.keys.map do |key|
          Truffle::Interop.to_java_string(key)
        end
      else
        object.instance_variables.map do |key|
          Truffle::Interop.to_java_string(key[1..-1])
        end
      end
    end
    
    def self.lookup_symbol(name)
      # TODO CS 9-Aug-17 I can't see to check if the method is defined
      if method = Object.method(name) rescue nil
        method
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
    
  end

end
