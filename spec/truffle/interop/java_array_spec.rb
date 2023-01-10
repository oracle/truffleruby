# Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.java_array" do

  it "creates a Object[] from the values" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(1, 2, 3))).should == "Object[]"
  end

  it "copies the correct values" do
    a = Truffle::Interop.java_array(1, 2, 3)
    a[0].should == 1
    a[1].should == 2
    a[2].should == 3
  end

  it "will use Object[] because splat does not specialise for the type" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(1, 2, 3))).should == "Object[]"
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(1.1, 2.2, 3.3))).should == "Object[]"
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.java_array(:a, :b, :c))).should == "Object[]"
  end

  it "creates a copy" do
    a1 = [1, 2, 3]
    a2 = Truffle::Interop.java_array(*a1)
    a2[0].should == 1
    a1[0] = 10
    a2[0].should == 1
  end

end
