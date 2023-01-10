# Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.deproxy" do

  it "passes through primitives" do
    Truffle::Interop.deproxy(14).should == 14
  end

  it "passes through Ruby objects" do
    o = Object.new
    Truffle::Interop.deproxy(o).should == o
  end

  it "deproxies a proxied array" do
    Truffle::Debug.java_class_of(Truffle::Interop.deproxy(Truffle::Interop.to_java_array([1, 2, 3]))).should == "int[]"
  end

end
