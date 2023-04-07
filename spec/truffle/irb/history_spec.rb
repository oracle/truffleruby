# Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "IRB" do
  it "ignores key sequences that would move past history in singleline mode" do
    # --readline is needed otherwise Readline is not used when stdin is not a TTY.
    IO.popen([*ruby_exe, "-S", "irb", "-f", "--prompt=simple", "--readline", "--singleline"], "r+") do |io|
      io.gets.should == "Switch to inspect mode.\n"

      io.puts "\C-n" # next-history (none)
      io.gets.should == ">> \n"

      # Prove that the session is still valid.
      io.puts "1+1"
      io.gets.should == ">> 1+1\n"
      io.gets.should == "=> 2\n"

      io.puts "exit"
      io.gets.should == ">> exit\n"
    end
  end
end
