#!/usr/bin/env ruby

# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
# This code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

constants = [
    Array,
    Class,
    Comparable,
    Data,
    Encoding,
    EncodingError,
    Enumerable,
    Enumerator,
    FalseClass,
    File,
    Float,
    Hash,
    Integer,
    IO,
    Kernel,
    [MatchData, 'Match'],
    Module,
    NilClass,
    Numeric,
    Object,
    Process,
    Range,
    Regexp,
    String,
    Struct,
    Symbol,
    Time,
    Thread,
    TrueClass,
    Proc,
    Method,
    Dir,
    [ArgumentError, 'ArgError'],
    EOFError,
    Errno,
    Exception,
    FloatDomainError,
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
    SyntaxError,
    SystemCallError,
    SystemExit,
    [SystemStackError, 'SysStackError'],
    TypeError,
    ThreadError,
    IO::WaitReadable,
    IO::WaitWritable,
    [ZeroDivisionError, 'ZeroDivError'],
    ['Truffle::CExt.rb_const_get(Object, \'fatal\')', 'eFatal'],
    ['$stdin', 'stdin'],
    ['$stdout', 'stdout'],
    ['$stderr', 'stderr'],
    ['$,', 'output_fs'],
    ['$/', 'rs'],
    ['$\\', 'output_rs'],
    ['"\n"', 'default_rs']
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

  if value.nil?
    expr = 'nil'
  else
    expr = value.to_s
  end

  if value.is_a?(Class) && (value < Exception || value == Exception)
    tag = 'rb_e'
  elsif value.is_a?(Class)
    tag = 'rb_c'
  elsif value.is_a?(Module)
    tag = 'rb_m'
  else
    tag = 'rb_'
  end

  name = "#{tag}#{name}"

  [name, expr]
end

constants_except_ruby_gvars = constants.reject do |name, expr|
  expr.start_with?('$')
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

  constants_except_ruby_gvars.each do |name, expr|
    f.puts "VALUE #{name};"
  end

  f.puts
  f.puts "void rb_tr_init_global_constants(void) {"
  constants_except_ruby_gvars.each do |name, expr|
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
