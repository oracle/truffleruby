# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.foreign?" do

  it "returns false for a Ruby object" do
    Truffle::Interop.foreign?(Object.new).should be_false
  end

  it "returns true for a Java object" do
    Truffle::Interop.foreign?(Truffle::Debug.foreign_object).should be_true
  end

  it "returns false for primitives" do
    Truffle::Interop.foreign?(14).should be_false
  end

end
