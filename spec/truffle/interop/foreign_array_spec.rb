# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Foreign arrays" do

end

describe "Non empty foreign arrays" do
  before do
    @foreign = Truffle::Interop.to_java_array([1, 2, 3])
  end

  it "can be printed with #print" do
    -> {
      print @foreign
    }.should output_to_fd(@foreign.to_s)
  end

  it "can be printed with #puts" do
    -> {
      puts @foreign
    }.should output_to_fd("1\n2\n3\n")
  end

  it "can be printed with #p" do
    -> {
      p @foreign
    }.should output_to_fd("#{@foreign.inspect}\n")
  end

  it "can call #size" do
    @foreign.size.should == 3
  end

  it "can call #length" do
    @foreign.length.should == 3
  end

  it "can access elements by indexing with #[]" do
    @foreign[0].should == 1
    @foreign[2].should == 3
    @foreign[4].should == nil
  end

  it "can access elements by indexing with #[] and negative indices" do
    @foreign[-1].should == 3
    @foreign[-2].should == 2
    @foreign[-3].should == 1
    @foreign[-4].should == nil
  end

  it "can access elements by indexing #at" do
    @foreign.at(0).should == 1
    @foreign.at(2).should == 3
    @foreign.at(5).should == nil
  end

  it "can access elements by indexing #at with negative indices" do
    @foreign.at(-1).should == 3
    @foreign.at(-2).should == 2
    @foreign.at(-3).should == 1
    @foreign.at(-5).should == nil
  end

  it "can access elements by indexing #fetch" do
    @foreign.fetch(0).should == 1
    @foreign.fetch(2).should == 3
  end

  it "can access elements by indexing #fetch with negative indices" do
    @foreign.fetch(-1).should == 3
    @foreign.fetch(-2).should == 2
    @foreign.fetch(-3).should == 1
  end

  it "should raise IndexError when indexing #fetch out of bounds" do
    -> {
      @foreign.fetch(-5)
    }.should raise_error(IndexError) { |e|
      e.message.should.include?("invalid array index -2")
    }
  end

  it "should access array with #first" do
    @foreign.first.should == 1
  end

  it "should access array with #last" do
    @foreign.last.should == 3
  end
end

describe "Empty foreign arrays" do
  before do
    @foreign = Truffle::Interop.to_java_array([])
  end

  it "can create empty foreign array" do
    @foreign.length.should == 0
  end

  it "should return nil when indexing #at out of bound" do
    @foreign.at(0).should == nil
  end

  it "should raise IndexError when using #fetch to access an empty array" do
    -> {
      @foreign.fetch(0)
    }.should raise_error(IndexError) { |e|
      e.message.should.include?("invalid array index 0")
    }
  end
end

describe "Foreign arrays that are also pointers" do
  before do
    @foreign = Truffle::Debug.foreign_pointer_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
  end

  it "can be printed with #print" do
    -> {
      print @foreign
    }.should output_to_fd(@foreign.to_s)
  end

  it "can be printed with #puts" do
    -> {
      puts Truffle::Debug.foreign_pointer_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
    }.should output_to_fd("1\n2\n3\n")
  end

  it "can be printed with #p" do
    -> {
      p @foreign
    }.should output_to_fd("#{@foreign.inspect}\n")
  end
end
