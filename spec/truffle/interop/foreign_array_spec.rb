# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Foreign arrays" do
  it "can be printed with #print" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    -> {
      print foreign
    }.should output_to_fd(foreign.to_s)
  end

  it "can be printed with #puts" do
    -> {
      puts Truffle::Interop.to_java_array([1, 2, 3])
    }.should output_to_fd("1\n2\n3\n")
  end

  it "can be printed with #p" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    -> {
      p foreign
    }.should output_to_fd("#{foreign.inspect}\n")
  end

  it "can call #size" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.size.should == 3
  end

  it "can call #length" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.length.should == 3
  end

  it "can access elements by indexing #[]" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign[0].should == 1
    foreign[2].should == 3
  end
  
  it "can access elements with #at" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.at(0).should == 1
    foreign.at(2).should == 3
  end

  it "can access elements with #at with negative indices" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.at(-1).should == 3
    foreign.at(-2).should == 2
    foreign.at(-3).should == 1
  end

  it "can access elements with #fetch" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.fetch(0).should == 1
    foreign.fetch(2).should == 3
  end

  it "can access elements with #fetch with negative indices" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.fetch(-1).should == 3
    foreign.fetch(-2).should == 2
    foreign.fetch(-3).should == 1
  end
  
#=begin  
  it "should raise array out of bounds in #fetch" do
    foreign = Truffle::Interop.to_java_array([1, 2, 3])
    foreign.fetch(-5).should == 0
  end
#=end

end

describe "Foreign arrays that are also pointers" do
  it "can be printed with #print" do
    foreign = Truffle::Debug.foreign_pointer_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
    -> {
      print foreign
    }.should output_to_fd(foreign.to_s)
  end

  it "can be printed with #puts" do
    -> {
      puts Truffle::Debug.foreign_pointer_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
    }.should output_to_fd("1\n2\n3\n")
  end

  it "can be printed with #p" do
    foreign = Truffle::Debug.foreign_pointer_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
    -> {
      p foreign
    }.should output_to_fd("#{foreign.inspect}\n")
  end
end
