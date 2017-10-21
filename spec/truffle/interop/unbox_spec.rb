# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.unbox" do
  
  class InteropUnboxClass
    
    def unbox
      14
    end
    
  end
  
  it "passes through fixnums" do
    Truffle::Interop.unbox(14).should == 14
  end
  
  it "passes through floats" do
    Truffle::Interop.unbox(14.2).should == 14.2
  end
  
  it "passes through true" do
    Truffle::Interop.unbox(true).should == true
  end
  
  it "passes through false" do
    Truffle::Interop.unbox(false).should == false
  end
    
  it "unboxes a Ruby string to a Java string" do
    unboxed = Truffle::Interop.unbox('test')
    Truffle::Interop.java_string?(unboxed).should be_true
    Truffle::Interop.from_java_string(unboxed).should == 'test'
  end
    
  it "unboxes a Ruby symbol to a Java string" do
    unboxed = Truffle::Interop.unbox(:test)
    Truffle::Interop.java_string?(unboxed).should be_true
    Truffle::Interop.from_java_string(unboxed).should == 'test'
  end
    
  it "unboxes a pointer to the address" do
    Truffle::Interop.unbox(Rubinius::FFI::Pointer.new(1024)).should == 1024
  end
    
  it "is not supported for nil" do
    lambda { Truffle::Interop.unbox(nil) }.should raise_error(ArgumentError)
  end

  it "is not supported for objects which cannot be unboxed" do
    lambda { Truffle::Interop.unbox(Object.new) }.should raise_error(ArgumentError)
  end

  it "calls #unbox" do
    Truffle::Interop.unbox(InteropUnboxClass.new).should == 14
  end

end
