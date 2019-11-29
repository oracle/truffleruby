# Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

abort 'not running the GraalVM Compiler' unless TruffleRuby.jit?

def foo
  var = 14
  var * 2
end

tp = TracePoint.new(:line) do |tp|
  if tp.path == __FILE__ && tp.lineno == 13
    tp.binding.local_variable_set(:var, 100)
  end
end

tp.enable

begin
  loop do
    x = foo
    raise 'value not correct' unless x == 200
    TrufflePrimitive.assert_compilation_constant x
    Truffle::Graal.assert_not_compiled
  end
rescue Truffle::GraalError => e
  if e.message.include? 'Truffle::Graal.assert_not_compiled'
    puts 'TP optimising'
    exit 0
  elsif e.message.include? 'TrufflePrimitive.assert_compilation_constant'
    puts 'TP not optimising'
    exit 1
  else
    p e.message
    puts 'some other error'
    exit 1
  end
end

exit 1
