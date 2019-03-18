# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.keys?" do

  it "returns false for an array" do
    Truffle::Interop.keys?([1, 2, 3]).should be_false
  end

  it "returns true for a hash" do
    Truffle::Interop.keys?({a: 1, b: 2, c: 3}).should be_true
  end

  it "returns true for general objects" do
    Truffle::Interop.keys?(Object.new).should be_true
  end

  it "returns true for frozen objects" do
    Truffle::Interop.keys?(Object.new.freeze).should be_true
  end

  it "returns true for nil" do
    Truffle::Interop.keys?(nil).should be_true
  end

  it "returns true for true" do
    Truffle::Interop.keys?(true).should be_true
  end

  it "returns true for false" do
    Truffle::Interop.keys?(false).should be_true
  end

  it "returns true for Fixnum" do
    Truffle::Interop.keys?(14).should be_true
  end

  it "returns true for Bignum" do
    Truffle::Interop.keys?(bignum_value).should be_true
  end

  it "returns true for Float" do
    Truffle::Interop.keys?(14.2).should be_true
  end

  it "returns true for Symbol" do
    Truffle::Interop.keys?(:foo).should be_true
  end

end
