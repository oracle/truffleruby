# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Polyglot

  def self.export(name, value)
    Truffle::Interop.export name, value
  end
  
  def self.export_method(name)
    Truffle::Interop.export_method name
  end
  
  def self.import(name)
    Truffle::Interop.import(name)
  end
  
  def self.import_method(name)
    Truffle::Interop.import_method name
  end
  
  def self.as_enumerable(object)
    Truffle::Interop.enumerable(object)
  end

end

unless Truffle.native?
  
  module Java

    def self.type(name)
      Truffle::Interop.java_type(name)
    end
    
    class ImportToken
      
      def initialize(name)
        @name = name.to_s
      end
      
      def method_missing(name)
        ImportToken.new("#{self}.#{name}")
      end
      
      def to_s
        @name
      end
      
    end
    
    class ImportScope
      
      def import(token)
        Java.import(token.to_s)
      end
      
      def method_missing(name)
        ImportToken.new(name)
      end
  
    end
    
    def self.import(*names, &block)
      last = nil
      
      names.each do |name|
        simple_name = name.split('.').last
        type = Java.type(name)
        if Object.send(:const_defined?, simple_name)
          current = Object.send(:const_get, simple_name)
          if current.equal?(type)
            # Ignore - it's already set
            last = type
          else
            raise NameError, "constant #{simple_name} already set"
          end
        else
          Object.send :const_set, simple_name, type
          last = type
        end
      end
      
      if block
        last = ImportScope.new.instance_eval(&block)
        unless last.nil? || last.respond_to?(:new)
          last = Java.import(last.to_s)
        end
      end
      
      last
    end

  end
  
end
