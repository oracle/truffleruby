# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.unbox_if_needed" do

  it "passes through fixnums" do
    Truffle::Interop.unbox_if_needed(14).should == 14
  end

  it "passes through floats" do
    Truffle::Interop.unbox_if_needed(14.2).should == 14.2
  end

  it "unboxes a foreign boxed number" do
    Truffle::Interop.unbox_if_needed(Truffle::Debug.foreign_boxed_number(2)).should == 2
    Truffle::Interop.unbox_if_needed(Truffle::Debug.foreign_boxed_number(14.2)).should == 14.2
  end

end
