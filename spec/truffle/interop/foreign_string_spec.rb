# Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Foreign strings" do
  it "are boxed" do
    Truffle::Interop.boxed?(Truffle::Debug.foreign_string('test')).should be_true
  end

  it "unbox to Ruby strings with Truffle::Interop.unbox" do
    Truffle::Interop.unbox(Truffle::Debug.foreign_string('test')).should == 'test'
  end

  it "are unboxed and converted to Ruby automatically on the LHS of string concatenation" do
    a = Truffle::Debug.foreign_string('a')
    b = 'b'
    (a + b).should == 'ab'
  end

  it "are unboxed and converted to Ruby automatically on the RHS of string concatenation" do
    a = 'a'
    b = Truffle::Debug.foreign_string('b')
    (a + b).should == 'ab'
  end

  it "respond to #to_s" do
    Truffle::Debug.foreign_string('a').to_s.should == 'a'
  end

  it "respond to #to_str" do
    Truffle::Debug.foreign_string('a').to_str.should == 'a'
  end

  it "can be printed with #puts" do
    -> {
      puts Truffle::Debug.foreign_string('a')
    }.should output_to_fd("a\n")
  end

  it "can be printed as if a Ruby string with #p" do
    -> {
      p Truffle::Debug.foreign_string('a')
    }.should output_to_fd("\"a\"\n")
  end
end
