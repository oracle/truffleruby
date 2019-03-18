# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Interop coercion" do

  describe "in a numerical operation" do

    it "does't interfere with normal coercion" do
      lambda { 14 + true }.should raise_error(TypeError, /can't be coerced into Integer/)
      lambda { 14 * true }.should raise_error(TypeError, /can't be coerced into Integer/)
      lambda { 14.2 + true }.should raise_error(TypeError, /can't be coerced into Float/)
      lambda { 14.2 * true }.should raise_error(TypeError, /can't be coerced into Float/)
      (14 + Truffle::Debug.float(2)).should == 16.0
      (14.0 + Truffle::Debug.float(2.0)).should == 16.0
    end

    it "unboxes a LHS operand" do
      (Truffle::Debug.foreign_boxed_number(2) + 14).should == 16
    end

    it "unboxes a RHS operand" do
      (2 + Truffle::Debug.foreign_boxed_number(14)).should == 16
    end

  end

end
