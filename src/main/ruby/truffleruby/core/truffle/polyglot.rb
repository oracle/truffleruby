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

  # region Trait modules for foreign objects
  # Specs for these methods are in spec/truffle/interop/special_forms_spec.rb
  # and in spec/truffle/interop/polyglot/*_spec.rb

  module ArrayTrait
    include Enumerable

    def each
      return to_enum(:each) { size } unless block_given?

      i = 0
      while i < length
        yield Truffle::Interop.read_array_element(self, i)
        i += 1
      end
    end

    def at(index)
      size = Truffle::Interop.array_size(self)
      index += size if index < 0
      if index < 0 || index >= size
        nil
      else
        Truffle::Interop.read_array_element(self, index)
      end
    end

    def [](index)
      if Primitive.object_kind_of?(index, Numeric)
        at(index)
      else
        super(index)
      end
    end

    def []=(member, value)
      if Primitive.object_kind_of?(member, Numeric)
        Truffle::Interop.write_array_element(self, member, value)
      else
        super(member, value)
      end
    end

    def delete(member)
      if Primitive.object_kind_of?(member, Numeric)
        Truffle::Interop.remove_array_element(self, member)
      else
        super(member)
      end
    end

    def empty?
      Truffle::Interop.array_size(self) == 0
    end

    def first
      self[0]
    end

    def last
      self[-1]
    end

    def size
      Truffle::Interop.array_size(self)
    end
    alias_method :length, :size

    def to_ary
      Truffle::Interop.to_array(self)
    end
    alias_method :to_a, :to_ary
  end

  module ExecutableTrait
    def call(*args)
      Truffle::Interop.execute(self, *args)
    end
  end

  module InstantiableTrait
    def new(*args)
      Truffle::Interop.instantiate(self, *args)
    end
  end

  module NullTrait
    def nil?
      true
    end

    def inspect
      klass = Truffle::InteropOperations.ruby_class_and_language(self)
      "#<#{klass} null>"
    end
  end

  module NumberTrait
    def to_i
      if Truffle::Interop.fits_in_int?(self)
        Truffle::Interop.as_int(self)
      elsif Truffle::Interop.fits_in_long?(self)
        Truffle::Interop.as_long(self)
      else
        Truffle::Interop.as_double(self).to_i
      end
    end

    def to_f
      if Truffle::Interop.fits_in_double?(self)
        Truffle::Interop.as_double(self)
      else
        Truffle::Interop.as_long(self).to_f
      end
    end

    def respond_to?(name, include_all = false)
      case symbol = name.to_sym
      when :to_f
        Truffle::Interop.fits_in_double?(self) || Truffle::Interop.fits_in_long?(self)
      when :to_i
        Truffle::Interop.fits_in_long?(self)
      else
        super(symbol, include_all)
      end
    end
  end

  module PointerTrait
  end

  module StringTrait
    def to_str
      Truffle::Interop.as_string(self)
    end
    alias_method :to_s, :to_str

    def inspect
      to_str.inspect
    end
  end

  class ForeignObject < Object
    def respond_to?(name, include_all = false)
      case symbol = name.to_sym
      when :keys
        Truffle::Interop.has_members?(self)
      when :class
        Truffle::Interop.java_class?(self)
      else
        super(symbol, include_all)
      end
    end

    def class
      if Truffle::Interop.java_class?(self)
        Truffle::Interop.read_member(self, :class)
      else
        Truffle::Interop.meta_object(self)
      end
    end

    def inspect
      recursive_string_for(self) if Truffle::ThreadOperations.detect_recursion self do
        return Truffle::InteropOperations.foreign_inspect_nonrecursive(self)
      end
    end

    def to_s
      klass = Truffle::InteropOperations.ruby_class_and_language(self)
      # Let InteropLibrary#toDisplayString show the class and identity hash code if relevant
      "#<#{klass} #{Truffle::Interop.to_display_string(self)}>"
    end

    def is_a?(klass)
      receiver = Truffle::Interop.unbox_if_needed(self)
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
    alias_method :kind_of?, :is_a?

    def keys
      Truffle::Interop.members(self)
    end

    def [](member)
      Truffle::Interop.read_member(self, member)
    end

    def []=(member, value)
      Truffle::Interop.write_member(self, member, value)
    end

    def delete(member)
      Truffle::Interop.remove_member(self, member)
    end
  end
  # endregion
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
