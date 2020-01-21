# Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

abort 'not running the GraalVM Compiler' unless TruffleRuby.jit?

def init_once
  $init_once ||= (TrufflePrimitive.compiler_bailout('init compiled'); true)
end

begin
  loop do
    init_once
    TrufflePrimitive.assert_not_compiled
  end
rescue Exception => e
  if e.message.include? 'assert_not_compiled'
    puts "correctly does not compile in the RHS of an init once expression"
  elsif e.message.include? 'init compiled'
    puts "incorrectly compiles in the RHS of an init once expression"
    exit 1
  else
    puts e.message, 'some other error'
    exit 1
  end
end

def init_many
  $init_many ||= (TrufflePrimitive.compiler_bailout('init compiled'); true)
end

begin
  loop do
    init_many
    $init_many = false
    TrufflePrimitive.assert_not_compiled
  end
rescue Exception => e
  if e.message.include? 'assert_not_compiled'
    puts "incorrectly does not compile in the RHS of an init many expression"
    exit 1
  elsif e.message.include? 'init compiled'
    puts "correctly compiles in the RHS of an init many expression"
  else
    puts e.message, 'some other error'
    exit 1
  end
end

def init_never
  $init_never ||= (TrufflePrimitive.compiler_bailout('init compiled'); true)
end

begin
  $init_never = true
  loop do
    init_never
    TrufflePrimitive.assert_not_compiled
  end
rescue Exception => e
  if e.message.include? 'assert_not_compiled'
    puts "correctly does not compile in the RHS of an init never expression"
  elsif e.message.include? 'init compiled'
    puts "incorrectly compiles in the RHS of an init never expression"
    exit 1
  else
    puts e.message, 'some other error'
    exit 1
  end
end

exit 0
