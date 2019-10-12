# Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/classes'

describe "Truffle::Interop.write" do

  describe "with an Array" do

    before :each do
      @array = [1, 2, 3]
    end

    it "writes a value to the array" do
      Truffle::Interop.write(@array, 1, 14)
      @array[1].should == 14
    end

    it "can extend the array" do
      Truffle::Interop.write(@array, 3, 14)
      @array[3].should == 14
    end

  end

  describe "with a Hash" do

    before :each do
      @hash = {'a' => 1, 'b' => 2, 'c' => 3}
    end

    it "can overwrite a value" do
      Truffle::Interop.write(@hash, 'b', 14)
      @hash['b'].should == 14
    end

    it "can add a new a value" do
      Truffle::Interop.write(@hash, 'x', 14)
      @hash['x'].should == 14
    end

  end

  describe "with a name that starts with @" do

    before :each do
      @object = TruffleInteropSpecs::InteropKeysClass.new
    end

    it "can overwrite an existing instance variable" do
      Truffle::Interop.write(@object, :@b, 14)
      @object.instance_variable_get(:@b).should == 14
    end

    it "can add a new a instance variable" do
      Truffle::Interop.write(@object, :@x, 14)
      @object.instance_variable_get(:@x).should == 14
    end

  end

  describe "with an object with an index set method" do

    it "calls the index set method" do
      object = TruffleInteropSpecs::WriteHasIndexSet.new
      Truffle::Interop.write(object, :foo, 14)
      object.called.should be_true
      object.key.should == 'foo'
      object.value.should == 14
    end

  end

  describe "with an object without an index set method" do

    it "raises UnknownIdentifierException" do
      object = Object.new
      -> {
        Truffle::Interop.write(object, :foo, 14)
      }.should raise_error(NameError, /Unknown identifier: foo/)
    end

  end

end
