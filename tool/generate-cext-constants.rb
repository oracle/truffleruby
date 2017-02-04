#!/usr/bin/env ruby

# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

constants = [
    Array,
    Bignum,
    Class,
    Comparable,
    Data,
    Enumerable,
    FalseClass,
    File,
    Fixnum,
    Float,
    Hash,
    Integer,
    IO,
    Kernel,
    MatchData,
    Module,
    NilClass,
    Numeric,
    Object,
    Range,
    Regexp,
    String,
    Struct,
    Symbol,
    Thread,
    TrueClass,
    Proc,
    Method,
    Dir,
    ArgumentError,
    EOFError,
    Errno,
    Exception,
    FloatDomainError,
    IndexError,
    Interrupt,
    IOError,
    LoadError,
    LocalJumpError,
    Math::DomainError,
    Encoding::CompatibilityError,
    NameError,
    NoMemoryError,
    NoMethodError,
    NotImplementedError,
    RangeError,
    RegexpError,
    RuntimeError,
    ScriptError,
    SecurityError,
    SignalException,
    StandardError,
    SyntaxError,
    SystemCallError,
    SystemExit,
    SystemStackError,
    TypeError,
    ThreadError,
    IO::WaitReadable,
    IO::WaitWritable,
    ZeroDivisionError
].map do |c|
  tag = if c < Exception || c == Exception
          'e'
        elsif c.is_a?(Class)
          'c'
        elsif c.is_a?(Module)
          'm'
        else
          raise 'unknown constant type'
        end

  name = case c.name
           when ArgumentError.name
             'ArgError'
           when MatchData.name
             'Match'
           when Math::DomainError.name
             'MathDomainError'
           when Encoding::CompatibilityError.name
             'EncCompatError'
           when NoMemoryError.name
             'NoMemError'
           when NotImplementedError.name
             'NotImpError'
           when SignalException.name
             'Signal'
           when SystemStackError.name
             'SysStackError'
           when ZeroDivisionError.name
             'ZeroDivError'
           else
             c.name.split('::').last
         end

  ["#{tag}#{name}", c]
end

constants.each do |cext_name, _|
  puts "VALUE rb_jt_get_#{cext_name}(void);"
end

puts

constants.each do |cext_name, _|
  puts "#define rb_#{cext_name} rb_jt_get_#{cext_name}()"
end

puts

constants.each do |cext_name, _|
  puts "VALUE rb_jt_get_#{cext_name}(void) {"
  puts "  return (VALUE) truffle_read(RUBY_CEXT, \"rb_#{cext_name}\");"
  puts "}"
  puts
end

puts

constants.each do |cext_name, ruby_name|
  puts "  def rb_#{cext_name}"
  puts "    #{ruby_name}"
  puts "  end"
  puts
end
