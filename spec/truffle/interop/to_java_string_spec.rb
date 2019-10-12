# Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.to_java_string" do

  it "can be round-tripped with from_java_string with a string" do
    Truffle::Interop.from_java_string(Truffle::Interop.to_java_string("foo")).should == "foo"
  end

  it "can be round-tripped with from_java_string with a symbol" do
    Truffle::Interop.from_java_string(Truffle::Interop.to_java_string(:foo)).should == "foo"
  end

  it "passes through primitives with from_java_string" do
    Truffle::Interop.from_java_string(Truffle::Interop.to_java_string(14)).should == 14
  end

  it "passes through non-string objects with from_java_string" do
    object = Object.new
    Truffle::Interop.from_java_string(Truffle::Interop.to_java_string(object)).equal?(object).should be_true
  end

end
