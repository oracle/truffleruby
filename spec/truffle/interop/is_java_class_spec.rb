# Copyright (c) 2018, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.java_class?" do

  it "returns false for a Ruby object" do
    Truffle::Interop.java_class?(Object.new).should be_false
  end

  it "returns true for a Java class" do
    Truffle::Interop.java_class?(Truffle::Debug.java_class).should be_true
  end

  it "returns false for a Java object" do
    Truffle::Interop.java_class?(Truffle::Debug.java_object).should be_false
  end

  it "returns false for some other foreign object" do
    Truffle::Interop.java_class?(Truffle::Debug.foreign_object).should be_false
  end

  it "returns false for primitives" do
    Truffle::Interop.java_class?(14).should be_false
  end

end
