# Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/classes'

describe "Truffle::Interop.read" do

  describe "with an Array" do

    before :each do
      @array = [1, 2, 3]
    end

    it "reads a value of an index that exists" do
      Truffle::Interop.read(@array, 1).should == 2
    end

    it "reads a method of given name that exists" do
      Truffle::Interop.read(@array, "[]").should == @array.method(:[])
    end

    it "raises for an index that doesn't exist" do
      -> { Truffle::Interop.read(@array, 100) }.should raise_error(NameError)
    end

  end

  describe "with a Hash" do

    before :each do
      @hash = {'a' => 1, 'b' => 2, 'c' => 3}
    end

    it "reads a value of a key that exists" do
      Truffle::Interop.read(@hash, 'b').should == 2
    end

    it "raises for a key that doesn't exist" do
      -> { Truffle::Interop.read(@hash, 'foo') }.should raise_error NameError
    end

  end

  describe "with a name that starts with @" do

    before :each do
      @object = TruffleInteropSpecs::InteropKeysClass.new
    end

    it "that exists as an instance variable reads it" do
      Truffle::Interop.read(@object, :@b).should == 2
    end

    it "that does not exist as an instance variable raises" do
      -> { Truffle::Interop.read(@object, :@foo) }.should raise_error NameError
    end

  end

  describe "with an object with a method of the same name" do

    it "produces a bound Method" do
      object = TruffleInteropSpecs::InteropKeysClass.new
      Truffle::Interop.read(object, :foo).call.should == 14
    end

  end

  describe "with an object with an index method" do

    it "calls the index method" do
      object = TruffleInteropSpecs::ReadHasIndex.new
      Truffle::Interop.read(object, 2).should == 14
      object.key.should == 2
    end

  end

  describe "with both an object with a method of the same name and an index method" do

    it "calls the index method" do
      object = TruffleInteropSpecs::ReadHasIndex.new
      Truffle::Interop.read(object, :bob).should == 14
      object.key.should == 'bob'
    end

  end

  describe "with an object without a method of the same name or an index method" do

    it "raises UnknownIdentifierException" do
      object = Object.new
      -> {
        Truffle::Interop.read(object, :foo)
      }.should raise_error(NameError, /Unknown identifier: foo/)
    end

  end

  describe "with a Proc" do

    it "does not call the proc" do
      proc = -> { raise 'called' }
      -> { Truffle::Interop.read(proc, :key) }.should raise_error NameError
      -> { Truffle::Interop.read(proc, :@var) }.should raise_error NameError
      Truffle::Interop.read(proc, 'call').should == proc.method(:call)
    end

  end

end
