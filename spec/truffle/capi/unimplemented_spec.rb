# Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension("unimplemented")

describe "Unimplemented functions in the C-API" do
  before :each do
    @s = CApiRbTrErrorSpecs.new
  end

  it "raise a useful RuntimeError including the function name" do
    -> {
      @s.not_implemented_function("foo")
    }.should raise_error(RuntimeError, /rb_str_shared_replace cannot be found/)
  end
end
