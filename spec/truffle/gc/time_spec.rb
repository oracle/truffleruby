# truffleruby_primitives: true

# Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "GC.time" do

  it "returns an Integer" do
    GC.time.should be_kind_of(Integer)
  end

  it "increases as collections are run" do
    time_before = GC.time
    Primitive.gc_force
    GC.time.should > time_before
  end

end
