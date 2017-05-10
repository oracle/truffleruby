# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.to_native" do
  
  class InteropToNativeClass
    
    def to_native
      Rubinius::FFI::Pointer.new(0x123)
    end
    
  end
  
  it "is not supported for nil" do
    lambda { Truffle::Interop.to_native(nil) }.should raise_error(ArgumentError)
  end

  it "is not supported for objects which cannot be converted to a pointer" do
    lambda { Truffle::Interop.to_native(Object.new) }.should raise_error(ArgumentError)
  end

  it "calls #to_native" do
    Truffle::Interop.to_native(InteropToNativeClass.new).should == Rubinius::FFI::Pointer.new(0x123)
  end

end
