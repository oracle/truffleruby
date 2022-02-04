# Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby_string")

describe "TruffleRuby RSTRING_PTR" do
  before :each do
    @s = CApiTruffleStringSpecs.new
  end

  it "stores the String to native memory even if not needed for efficiency" do
    str = "foobar"
    @s.string_ptr(str).should == "f"
    Truffle::CExt.native_string?(str).should == true
  end

  it "stores the String to native memory if the address is returned" do
    str = "foobar"
    @s.string_ptr_return_address(str).should be_kind_of(Integer)
    Truffle::CExt.native_string?(str).should == true
  end
end

describe "TruffleRuby NATIVE_RSTRING_PTR" do
  before :each do
    @s = CApiTruffleStringSpecs.new
  end

  it "ensures the String is stored in native memory" do
    str = "foobar"
    Truffle::CExt.native_string?(str).should == false
    @s.NATIVE_RSTRING_PTR(str)
    Truffle::CExt.native_string?(str).should == true
  end
end
