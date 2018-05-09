# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Interop coercion" do
  
  describe "in a numerical operation" do
    
    it "unboxes a LHS operand" do
      (Truffle::Debug.foreign_boxed_number(2) + 14).should == 16
    end
    
    it "unboxes a RHS operand" do
      (2 + Truffle::Debug.foreign_boxed_number(14)).should == 16
    end
    
  end

end
