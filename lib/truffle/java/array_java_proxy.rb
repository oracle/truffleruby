# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

begin
  # Although we are outside the normal proxy creation process this
  # class does not represent a concrete Java type itself. Setting this
  # thread local will stop the JavaProxy meta-programming hooks from
  # attempting to generate a new Java class.
  Thread.current[:MAKING_JAVA_PROXY] = true
  class ArrayJavaProxy < java.lang.Object
    include Enumerable

    def each
      (0...size).each do |i|
        yield self[i]
      end
      self
    end

    def inspect
      s = '[' + self.map(&:to_s).join(', ') + ']'
    end

    def to_ary
      Array.new(size){ |i| self[i] }
    end

    def empty?
      size == 0
    end

    def length
      size
    end
  end

  class ArrayJavaProxyCreator
    attr_reader :type
    attr_reader :dims

    def initialize(type, *dimensions)
      @type = type
      @dims = []
      self[*dimensions]
    end

    def [](*dimensions)
      dimensions.each do |d|
        raise TypeError, 'Array dimension must be an integer' unless d.kind_of?(Fixnum)
        dims << d
      end
      self
    end

    def new
      java.lang.reflect.Array.new_instance(type, *dims)
    end

    alias :new_instance :new
  end
ensure
  Thread.current[:MAKING_JAVA_PROXY] = false
end
