# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.boxed?" do
  
  it "returns true for strings" do
    Truffle::Interop.boxed?('tests').should be_true
  end
  
  it "returns true for Rubinius::FFI::Pointer objects" do
    Truffle::Interop.boxed?(Rubinius::FFI::Pointer.new(0)).should be_true
  end

  it "returns true for objects that respond to #unbox" do
    unboxable = Object.new
    def unboxable.unbox
    end
    Truffle::Interop.boxed?(unboxable).should be_true
  end
  
  it "returns false for other objects" do
    Truffle::Interop.boxed?(Object.new).should be_false
  end

end
