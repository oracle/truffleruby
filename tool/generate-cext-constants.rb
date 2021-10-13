#!/usr/bin/env ruby

# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

constants = [
    # classes and modules
    Array,
    BasicObject,
    Binding,
    Class,
    Comparable,
    Complex,
    Dir,
    Encoding,
    Enumerable,
    Enumerator,
    FalseClass,
    File,
    FileTest,
    File::Stat,
    Float,
    GC,
    Hash,
    Integer,
    IO,
    Kernel,
    [MatchData, 'Match'],
    Math,
    Method,
    Module,
    NilClass,
    Numeric,
    Object,
    Proc,
    Process,
    Random,
    Range,
    Rational,
    Regexp,
    String,
    Struct,
    Symbol,
    Time,
    Thread,
    TrueClass,
    UnboundMethod,
    # exception classes
    [ArgumentError, 'ArgError'],
    EncodingError,
    EOFError,
    Errno,
    Exception,
    FloatDomainError,
    FrozenError,
    IndexError,
    Interrupt,
    IOError,
    KeyError,
    LoadError,
    LocalJumpError,
    [Math::DomainError, 'MathDomainError'],
    [Encoding::CompatibilityError, 'EncCompatError'],
    NameError,
    [NoMemoryError, 'NoMemError'],
    NoMethodError,
    [NotImplementedError, 'NotImpError'],
    RangeError,
    RegexpError,
    RuntimeError,
    ScriptError,
    SecurityError,
    [SignalException, 'Signal'],
    StandardError,
    StopIteration,
    SyntaxError,
    SystemCallError,
    SystemExit,
    [SystemStackError, 'SysStackError'],
    TypeError,
    ThreadError,
    IO::WaitReadable,
    IO::WaitWritable,
    [ZeroDivisionError, 'ZeroDivError'],
    ['Truffle::CExt.rb_const_get(Object, \'fatal\')', 'Fatal'],
].map do |const|
  if const.is_a?(Array)
    value, name = const
  else
    value = const

    if value.is_a?(Module)
      name = value.name.split('::').last
    else
      name = value.to_s
    end
  end

  expr = value.to_s

  if (value.is_a?(Class) && value <= Exception) or name == 'Fatal'
    tag = 'rb_e'
  elsif value.is_a?(Class)
    tag = 'rb_c'
  elsif value.is_a?(Module)
    tag = 'rb_m'
  else
    raise value.inspect
  end

  name = "#{tag}#{name}"

  [name, expr]
end

File.open("src/main/c/cext/cext_constants.c", "w") do |f|
  f.puts <<COPYRIGHT
/*
 * Copyright (c) #{Time.now.year} Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
COPYRIGHT
  f.puts
  f.puts "// From #{__FILE__}"
  f.puts
  f.puts '#include <ruby.h>'
  f.puts

  constants.each do |name, expr|
    f.puts "VALUE #{name};"
  end

  f.puts
  f.puts "void rb_tr_init_global_constants(void) {"
  constants.each do |name, expr|
    f.puts "  #{name} = RUBY_CEXT_INVOKE(\"#{name}\");"
  end
  f.puts "}"
end

File.open("lib/truffle/truffle/cext_constants.rb", "w") do |f|
  f.puts <<COPYRIGHT
# Copyright (c) #{Time.now.year} Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
COPYRIGHT
  f.puts
  f.puts "# From #{__FILE__}"
  f.puts

  f.puts "module Truffle::CExt"
  constants.each do |name, expr|
    f.puts "  def #{name}"
    f.puts "    #{expr}"
    f.puts "  end"
    f.puts
  end
  f.puts "end"
end
