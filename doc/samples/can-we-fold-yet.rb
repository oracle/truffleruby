# Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Use -J-Dgraal.TruffleIterativePartialEscape=true

require 'readline'

unless Truffle::Graal.graal?
  puts 'You need Graal to run this'
  exit
end

puts 'Can Truffle constant fold yet?'

loop do
  code = Readline.readline('> ', true)

  test_thread = Thread.new do
    begin
      eval "loop { Truffle::Graal.assert_constant #{code}; Truffle::Graal.assert_not_compiled; Thread.pass }"
    rescue RubyTruffleError => e
      if e.message.include? 'Truffle::Graal.assert_not_compiled'
        puts "Yes! Truffle can constant fold this to #{eval(code).inspect}"
      elsif e.message.include? 'Truffle::Graal.assert_constant'
        puts "No :( Truffle can't constant fold that"
      else
        puts 'There was an error executing that :('
      end
    end
  end

  unless test_thread.join(5)
    puts 'That timed out :( either it takes too long to execute or to compile'
  end
end
