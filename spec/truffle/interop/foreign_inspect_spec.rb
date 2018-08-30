# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Inspect on a foreign" do
  
  describe "array" do
    
    it "gives a similar representation to Ruby" do
      Truffle::Interop.to_java_array([1, 2, 3]).inspect.should == "#<Foreign [1, 2, 3]>"
    end
    
  end
  
  describe "object" do

    it "gives an identity code" do
      Truffle::Debug.foreign_object.inspect.should =~ /#<Foreign:0x\h+>/
    end
    
  end

end
