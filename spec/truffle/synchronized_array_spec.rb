# truffleruby_primitives: true

# Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Truffle::SynchronizedArray" do
  it "redefines all Array and Enumerable methods" do
    expected_methods = ruby_exe("puts Array.public_instance_methods(false) + Enumerable.public_instance_methods(false)")
    expected_methods = expected_methods.lines(chomp: true).map { |line| line.to_sym }.uniq!.sort!
    synchronized_methods = Truffle::SynchronizedArray.public_instance_methods(false).sort!

    if synchronized_methods == expected_methods
      synchronized_methods.should == synchronized_methods
    else
      # if the fast check fails, do a slower check to determine why
      expected_methods.each do |m|
        synchronized_methods.should include(m)
      end
      synchronized_methods.each do |m|
        expected_methods.should include(m)
      end
    end
  end
end
