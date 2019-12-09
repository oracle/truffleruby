# Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

abort 'not running the GraalVM Compiler' unless TruffleRuby.jit?

timeout = Time.now + 30

begin
  while Time.now < timeout
    TrufflePrimitive.assert_not_compiled
  end

  puts 'while loop optimisation timed out'
  exit 1
rescue Truffle::GraalError => e
  if e.message.include? 'TrufflePrimitive.assert_not_compiled'
    puts 'while loop optimising'
    exit 0
  else
    p e.message
    puts 'some other error'
    exit 1
  end
end

exit 1
