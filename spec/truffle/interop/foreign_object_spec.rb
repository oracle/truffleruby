# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Foreign objects" do

  it "implement #to_s with #inspect" do
    foreign = Truffle::Debug.foreign_object
    foreign.to_s.should == foreign.inspect
  end

  it "can be printed with #puts" do
    -> {
      puts Truffle::Debug.foreign_object
    }.should output(/#<Foreign:0x\h+>\n/)
  end

  it "can be printed with #p" do
    -> {
      p Truffle::Debug.foreign_object
    }.should output(/#<Foreign:0x\h+>\n/)
  end

end
