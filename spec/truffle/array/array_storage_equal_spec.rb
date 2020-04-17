# truffleruby_primitives: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.
#

require_relative '../../ruby/spec_helper'

describe "array_storage_equal primitive" do
  it "returns true for equal storage" do
    a = ["one", "two"]
    b = a.dup
    Primitive.array_storage_equal?(a, b).should == true
  end

  it "returns false when original is modified" do
    a = ["one", "two"]
    b = a.dup
    a << "three"
    Primitive.array_storage_equal?(a, b).should == false
  end
end
