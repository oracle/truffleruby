# Copyright (c) 2017, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.pointer?" do

  it "returns false for nil" do
    Truffle::Interop.pointer?(nil).should be_false
  end

  it "returns true for Truffle::FFI::Pointer" do
    Truffle::Interop.pointer?(Truffle::FFI::Pointer.new(0x123)).should be_true
  end

end
