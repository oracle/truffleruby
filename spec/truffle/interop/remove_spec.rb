# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.remove" do

  describe "with Array" do
    before :each do
      @array = [:a, :b, :c, :d]
    end

    it "removes the element at the specified index" do
      Truffle::Interop.remove(@array, 2).should == true
      @array.should == [:a, :b, :d]
    end

    it "no-ops when the index is out of bounds" do
      Truffle::Interop.remove(@array, 10).should == true
      @array.should == [:a, :b, :c, :d]
    end

    it "raises a NameError when the index is not an integer" do
      lambda {
        Truffle::Interop.remove(@array, :b)
      }.should raise_error(NameError, /Unknown identifier: b/) { |e|
        e.receiver.should equal @array
        e.name.should == :b
      }
    end
  end

  describe "with Hash" do
    before :each do
      @hash = { 'a' => 1, 'b' => 2, 'c' => 3 }
    end

    it "removes the element with the specified key" do
      Truffle::Interop.remove(@hash, 'b').should == true
      @hash.keys.should == ['a', 'c']
    end

    it "no-ops when the key doesn't exist" do
      Truffle::Interop.remove(@hash, 'bad_key').should == true
      @hash.keys.should == ['a', 'b', 'c']
    end
  end

  describe "with any other type" do
    it "raises an unsupported message error" do
      lambda {
        Truffle::Interop.remove("abc", 1)
      }.should raise_error(RubyTruffleError, /Message not supported: REMOVE/)
    end
  end

end
