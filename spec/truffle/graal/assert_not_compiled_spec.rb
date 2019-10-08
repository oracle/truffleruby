# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Graal.assert_not_compiled" do

  it "raises a RuntimeError when called dynamically" do
    -> { Truffle::Graal.send(:assert_not_compiled) }.should raise_error(RuntimeError)
  end

  unless TruffleRuby.jit?
    it "returns nil" do
      Truffle::Graal.assert_not_compiled.should be_nil
    end
  end

end
