# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby_newobj")

describe "TruffleRuby handling of the NEWOBJ API" do
  before :each do
    @s = CApiTruffleRubyNewobjSpecs.new
  end

  it "does not support NEWOBJ since we cannot create an object without a specified class" do
    @s.RB_NEWOBJ_defined?.should be_false
  end

  it "does not support NEWOBJ since we cannot create an object without a specified class" do
    @s.NEWOBJ_defined?.should be_false
  end

  it "does not support OBJSETUP since we use a different object layout than MRI" do
    @s.OBJSETUP_defined?.should be_false
  end

  it "does support NEWOBJ_OF since we can create an object with a specified class" do
    @s.NEWOBJ_OF_defined?.should be_true
  end

  it "does support RB_NEWOBJ_OF since we can create an object with a specified class" do
    @s.RB_NEWOBJ_OF_defined?.should be_true
  end
end
