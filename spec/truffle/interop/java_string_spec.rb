# Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Java strings" do

  it "is String" do
    Truffle::Interop.string?(Truffle::Interop.to_java_string('test')).should be_true
  end

  it "return the same object if attempted to be unboxed" do
    unboxed = Truffle::Interop.unbox_without_conversion(Truffle::Interop.to_java_string('test'))
    Truffle::Interop.java_string?(unboxed).should be_true
    Truffle::Interop.from_java_string(unboxed).should == 'test'
  end

  it "are converted to Ruby automatically on the LHS of string concatenation" do
    a = Truffle::Interop.to_java_string('a')
    b = 'b'
    (a + b).should == 'ab'
  end

  it "are converted to Ruby automatically on the RHS of string concatenation" do
    a = 'a'
    b = Truffle::Interop.to_java_string('a')
    (a + b).should == 'ab'
  end

  it "respond to #to_s" do
    Truffle::Interop.to_java_string('a').to_s.should == 'a'
  end

  it "respond to #to_str" do
    Truffle::Interop.to_java_string('a').to_str.should == 'a'
  end

  it "can be printed with #puts" do
    -> {
      puts Truffle::Interop.to_java_string('a')
    }.should output_to_fd("a\n")
  end

  it "can be printed as if a Ruby string with #p" do
    -> {
      p Truffle::Interop.to_java_string('a')
    }.should output_to_fd("\"a\"\n")
  end

  it "respond to Java methods" do
    Truffle::Interop.to_java_string('abc').subSequence(1, 2).should == 'b'
  end

end
