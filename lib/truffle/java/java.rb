# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Java
  class JavaObject

    NULL_LOCK = Mutex.new

    attr_reader :value

    private :value

    private_constant :NULL_LOCK

    def initialize(a_value)
      if a_value != nil && a_value.class != Truffle::Interop::Foreign
        raise ArgumentError, "Can only wrap native java objects"
      end
      @value = a_value
    end

    def to_s
      return @value.java_send("toString") if @value != nil
      ""
    end

    def wrap obj
      return JavaObject.new(obj.value) if obj.is_kind_of?(JavaObject)
    end

    def hash
      @value.hashCode() if @value != nil
      0
    end

    def eql?(another)
      Truffle::Interop::Java.java_eql?(@value, @value)
    end

    alias_method :eql?, :==

    def equal?(another)
      nil != @value and JavaObject === another and @value.equals(another.value)
    end

    def java_type
      @value.getClass().getName()
    end

    def java_class
      JavaClass.get(@value.getClass())
    end

    def length
      raise TypeError, "Not a java array"
    end

    def java_proxy?
      true
    end

    def synchronized(&block)
      return @value.__synchronize__(block) if @value != nil
      NULL_LOCK.synchronize block
    end

    def marshal_dump
    end

    def marshal_load
    end
  end

  class JavaArray < JavaObject
    def length
      java.lang.reflect.Array.getLength(@value)
    end
  end

  class JavaClass
  end

  class JavaPackage < Module

    attr_reader :package_name
    attr_reader :children

    def self.new(*name_parts)
      const_name = name_parts.reduce("") { |memo, obj| memo + obj.capitalize }
      const_name = "Default" if const_name == ""
      return ::Java.const_get(const_name) if ::Java.const_defined?(const_name, false)
      super(*name_parts)
    end

    def initialize(*name_parts)
      ruby_name = name_parts.reduce("") do |memo, obj| memo + obj.capitalize end
      java_name = name_parts.join(".")
      @package_name = java_name
      @children = {}
      ::Java.const_set(ruby_name, self) if ruby_name != ""
      parent = JavaPackage.new(*name_parts.first(name_parts.size - 1)) unless name_parts.size <= 1
      parent.add_child(name_parts.last, self) unless parent == nil
      super()
    end

    def ===(another)
      self.equal?(another) || another.kind_of?(self)
    end

    def self.capitalised_name(name)
      name = ""
      package_name.split(".").each do |s|
        name += s.capitalize
      end
    end

    def to_s
      @package_name
    end

    def inspect
      super.to_s
    end

    def const_get(name, inherit=true)
      super.const_get(name, inherit) rescue method_missing(name)
    end

    def const_missing(name)
      method_missing(name)
    end

    def add_child(package, name)
      @children[name] = package
    end

    def method_missing(name, *args)
      if args.size != 0
        raise ArgumentError, "Java package '#{self}' does not have a method `#{name}' with #{args.size} argument"
      end

      val = @children[name]

      return val if val != nil

      val = JavaUtilities.get_relative_package_or_class(self, name)
      @children[name] = val

      if name[0] == name[0].upcase && !val.kind_of?(JavaPackage)
        const_set(name, val)
      end
      val
    end

  end

  class JavaProxy
  end

  @packages = {}

  def self.const_missing name
    JavaPackage.new(*name.to_s.split(/(?=[[:upper:]])/).map { |s| s.downcase } )
  end

  def self.method_missing(name, *args)
    if args.size != 0
      raise ArgumentError, "Java does not have a method #{name} with #{args.size} arguments."
    end

    val = @packages[name.capitalize]

    return val if val != nil

    val = JavaUtilities.get_package_module_dot_format(name.to_s)

    @packages[name.capitalize] = val
  end

  def self.java_to_ruby obj
    # TODO: DMM 2017-02-07
    # We'll do something here, not sure what yet.
  end

  def self.ruby_to_java obj
  end

  def self.java_to_primitive obj
  end

  def self.new_proxy_instance2 wrapper, interfaces
  end

  Default = JavaPackage.new()
end
