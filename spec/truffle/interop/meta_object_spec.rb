# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.meta_object" do

  it "returns Integer class for an integer" do
    Truffle::Interop.meta_object(14).should == Integer
  end

  it "returns TrueClass class for a true boolean" do
    Truffle::Interop.meta_object(true).should == TrueClass
  end

  it "returns Object class for an object" do
    Truffle::Interop.meta_object(Object.new).should == Object
  end

  it "returns Hash class for a hash" do
    Truffle::Interop.meta_object({}).should == Hash
  end

  it "returns Array class for an array" do
    Truffle::Interop.meta_object([1, 2, 3]).should == Array
  end

end
