# frozen_string_literal: true

# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Polyglot

  # stub defined in CoreLibrary
  class UnsupportedMessageError < StandardError
  end

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

module Java
  def self.type(name)
    Truffle::Interop.java_type(name)
  end

  def self.import(name)
    name = name.to_s
    simple_name = name.split('.').last
    type = Java.type(name)
    if Object.const_defined?(simple_name)
      current = Object.const_get(simple_name)
      if current.equal?(type)
        # Ignore - it's already set
      else
        raise NameError, "constant #{simple_name} already set"
      end
    else
      Object.const_set simple_name, type
    end
    type
  end

  def self.synchronized(object)
    Truffle::System.synchronized(object) do
      yield
    end
  end

  # test-unit expects `Java::JavaLang::Throwable` to be resolvable if `::Java` is defined (see assertions.rb).
  # When doing JRuby-style interop, that's a fine assumption. However, we have `::Java` defined for Truffle-style
  # interop and in that case, the assumption does not hold. In order to allow the gem to work properly, we define
  # a dummy `Throwable` class here.
  module JavaLang
    class Throwable; end
  end
end
