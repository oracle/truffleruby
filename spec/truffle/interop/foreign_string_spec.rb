# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Foreign strings" do
  
  it "are boxed" do
    Truffle::Interop.boxed?(Truffle::Debug.foreign_string('test')).should be_true
  end
  
  it "unbox to Ruby strings with Truffle::Interop.unbox" do
    Truffle::Interop.unbox(Truffle::Debug.foreign_string('test')).should == 'test'
  end
  
  it "are unboxed and converted to Ruby automatically on the LHS of string concatenation" do
    a = Truffle::Debug.foreign_string('a')
    b = 'b'
    (a + b).should == 'ab'
  end
  
  it "are unboxed and converted to Ruby automatically on the RHS of string concatenation" do
    a = 'a'
    b = Truffle::Debug.foreign_string('b')
    (a + b).should == 'ab'
  end

end
