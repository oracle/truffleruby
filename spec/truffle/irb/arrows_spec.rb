# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "IRB" do
  it "supports using arrow keys first" do
    # --readline is needed otherwise Readline is not used when stdin is not a TTY.
    IO.popen([*ruby_exe, "-S", "irb", "-f", "--prompt=simple", "--readline"], "r+") do |io|
      io.gets.should == "Switch to inspect mode.\n"

      io.puts "\e[A" # up arrow
      # JLine seems to add a bell character
      [">> \n", ">> \a\n"].should include(io.gets)

      io.puts "22 + 33"
      # The extra bell character causes a continuation line
      [">> 22 + 33\n", "?> 22 + 33\n"].should include(io.gets)
      io.gets.should == "=> 55\n"

      io.puts "exit"
      io.gets.should == ">> exit\n"
    end
  end
end
