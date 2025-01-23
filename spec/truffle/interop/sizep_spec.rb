# Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.has_array_elements?" do

  it "returns true for arrays" do
    Truffle::Interop.has_array_elements?([]).should be_true
  end

  it "returns false for hashes" do
    Truffle::Interop.has_array_elements?({}).should be_false
  end

  it "returns false for strings" do
    Truffle::Interop.has_array_elements?('').should be_false
  end

  it "returns false for nil" do
    Truffle::Interop.has_array_elements?(nil).should be_false
  end

end
