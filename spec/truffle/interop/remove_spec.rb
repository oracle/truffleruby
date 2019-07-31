# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/classes'

describe "Truffle::Interop.remove" do

  describe "with Array" do
    before :each do
      @array = [:a, :b, :c, :d]
    end

    it "removes the element at the specified index" do
      Truffle::Interop.remove(@array, 2).should == true
      @array.should == [:a, :b, :d]
    end

    it "raises when the index is out of bounds" do
      -> { Truffle::Interop.remove(@array, 10) }.should raise_error NameError
      @array.should == [:a, :b, :c, :d]
    end

    it "raises a NameError when the index is not an integer" do
      -> {
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

    it "raises when the key doesn't exist" do
      -> { Truffle::Interop.remove(@hash, 'bad_key') }.should raise_error NameError
      @hash.keys.should == ['a', 'b', 'c']
    end
  end

  describe "with any other type" do
    describe "with a name that starts with @" do
      before :each do
        @object = TruffleInteropSpecs::InteropKeysClass.new
      end

      it "removes an instance variable that exists" do
        Truffle::Interop.remove(@object, :@a).should == true
        @object.instance_variable_defined?(:@a).should be_false
      end

      it "raises an error when the instance variable doesn't exist" do
        -> {
          Truffle::Interop.remove(@object, :@foo)
        }.should raise_error(NameError)
      end
    end

    describe "with a name that doesn't start with @" do
      it "raises an unsupported message error" do
        -> { Truffle::Interop.remove("abc", 1) }.
            # TODO (pitr-ch 13-Mar-2019): when on Interop 2 keep the second match
            should raise_error(RuntimeError, /Message (not supported|unsupported)/)
      end
    end
  end

end
