# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.as_pointer" do
  
  class InteropAsPointerClass
    
    def address
      0x123
    end
    
  end
  
  it "is not supported for nil" do
    lambda { Truffle::Interop.as_pointer(nil) }.should raise_error(ArgumentError)
  end

  it "is not supported for objects which cannot be converted to a pointer" do
    lambda { Truffle::Interop.as_pointer(Object.new) }.should raise_error(ArgumentError)
  end

  it "works on Rubinius::FFI::Pointer" do
    Truffle::Interop.as_pointer(Rubinius::FFI::Pointer.new(0x123)).should == 0x123
  end

  it "calls #address" do
    Truffle::Interop.as_pointer(InteropAsPointerClass.new).should == 0x123
  end

end
