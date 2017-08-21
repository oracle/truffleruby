# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.meta_object" do
  
  it "has a type property" do
    Truffle::Interop.meta_object(14).type.should == "Fixnum"
    Truffle::Interop.meta_object(Object.new).type.should == "Object"
    Truffle::Interop.meta_object({}).type.should == "Hash"
  end
  
  it "has a className property" do
    Truffle::Interop.meta_object(14).className.should == "Fixnum"
    Truffle::Interop.meta_object(Object.new).className.should == "Object"
    Truffle::Interop.meta_object({}).className.should == "Hash"
  end
  
  it "has a description property" do
    Truffle::Interop.meta_object(14).description.should == "14"
    Truffle::Interop.meta_object(Object.new).description.should =~ /#<Object:0x\h+>/
    Truffle::Interop.meta_object({}).description.should == "{}"
  end
  
end
