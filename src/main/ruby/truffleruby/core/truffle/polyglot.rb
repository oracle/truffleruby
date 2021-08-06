# frozen_string_literal: true

# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
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

  class InnerContext
    # Create a new isolated inner context to eval code in any available public language
    # (those languages can be listed with +Polyglot.languages+).
    # Automatically closes the context when given a block.
    def self.new
      inner_context = Primitive.inner_context_new(self)
      if block_given?
        begin
          yield inner_context
        ensure
          inner_context.close
        end
      else
        inner_context
      end
    end

    # Eval a String of code in the given language in the inner context.
    # The return value, unless it is a Java primitive or a java.lang.String, is wrapped in a generic interop wrapper,
    # so the returned object behaves like a foreign object (even if language is 'ruby').
    # That wrapper automatically enters and leaves the inner context for any access to that object.
    def eval(language, code)
      Primitive.inner_context_eval(self, language, code)
    end

    # Close the inner context and release the associated resources.
    # If the context is not closed explicitly, then it is automatically closed together with the parent context.
    def close
      Primitive.inner_context_close(self)
    end
  end

  # The list of all languages that are installed in GraalVM and publicly accessible.
  # Note that you need --polyglot on the command-line to enable access to other languages.
  # Typically --jvm is also passed as native launchers by default only contain one language.
  def self.languages
    Truffle::Interop.languages
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

  # test-unit expects `Java::JavaLang::Throwable` to be resolvable if `::Java` is defined (see assertions.rb).
  # When doing JRuby-style interop, that's a fine assumption. However, we have `::Java` defined for Truffle-style
  # interop and in that case, the assumption does not hold. In order to allow the gem to work properly, we define
  # a dummy `Throwable` class here.
  module JavaLang
    class Throwable; end
  end
end
