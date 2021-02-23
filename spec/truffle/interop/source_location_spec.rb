# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/classes'

describe "Truffle::Interop source messages" do
  it "returns correct source location" do
    method = Array.instance_method(:each_index)
    Truffle::Interop.has_source_location?(method).should == true
    Truffle::Interop.to_display_string(Truffle::Interop.source_location(method)).should include("array.rb")

    Truffle::Interop.has_source_location?(Array).should == true
    Truffle::Interop.to_display_string(Truffle::Interop.source_location(Array)).should include("(unavailable)")

    Truffle::Interop.has_source_location?(ObjectSpace).should == true
    Truffle::Interop.to_display_string(Truffle::Interop.source_location(ObjectSpace)).should include("(unavailable)")
  end
end
