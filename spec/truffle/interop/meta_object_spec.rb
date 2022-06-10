# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
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

  it "returns a Ruby class implementing all meta objects methods" do
    meta = Truffle::Interop.meta_object("string")
    Truffle::Interop.meta_simple_name(meta).should == 'String'
    Truffle::Interop.meta_qualified_name(meta).should == 'String'
    Truffle::Interop.meta_parents(meta).to_a.should == [Object]

    Truffle::Interop.meta_simple_name(Enumerator::Lazy).should == 'Lazy'
    Truffle::Interop.meta_qualified_name(Enumerator::Lazy).should == 'Enumerator::Lazy'

    Truffle::Interop.should_not.has_meta_parents?(Enumerable)
  end
end
