# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Java strings" do
  
  it "are not boxed" do
    Truffle::Interop.boxed?(Truffle::Interop.to_java_string('test')).should be_false
  end
  
  it "return the same object if attempted to be unboxed" do
    unboxed = Truffle::Interop.unbox_without_conversion(Truffle::Interop.to_java_string('test'))
    Truffle::Interop.java_string?(unboxed).should be_true
    Truffle::Interop.from_java_string(unboxed).should == 'test'
  end
  
  it "are converted to Ruby automatically on the LHS of string concatenation" do
    a = Truffle::Interop.to_java_string('a')
    b = 'b'
    (a + b).should == 'ab'
  end
  
  it "are converted to Ruby automatically on the RHS of string concatenation" do
    a = 'a'
    b = Truffle::Interop.to_java_string('a')
    (a + b).should == 'ab'
  end

end
