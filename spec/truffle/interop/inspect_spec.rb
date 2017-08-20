# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Interop #inspect" do

  it "returns a useful string" do
    Truffle::Debug.foreign_object.inspect.should =~ /#<Truffle::Interop::Foreign:0x\h+>/
  end

end
