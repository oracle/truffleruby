# Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.to_java_array" do

  it "creates a int[] from Fixnum values" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.to_java_array([1, 2, 3]))).should == "int[]"
  end

  it "copies the correct values" do
    a = Truffle::Interop.to_java_array([1, 2, 3])
    a[0].should == 1
    a[1].should == 2
    a[2].should == 3
  end

  it "picks a sensible Java array type" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.to_java_array([1, 2, 3]))).should == "int[]"
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.to_java_array([1.1, 2.2, 3.3]))).should == "double[]"
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.to_java_array([:a, :b, :c]))).should == "Object[]"
  end

  it "calls #to_a" do
    mock_array = Object.new

    def mock_array.to_a
      [1, 2, 3]
    end

    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.to_java_array(mock_array))).should == "int[]"
  end

  it "creates a copy" do
    a1 = [1, 2, 3]
    a2 = Truffle::Interop.to_java_array(a1)
    a2[0].should == 1
    a1[0] = 10
    a2[0].should == 1
  end

end
