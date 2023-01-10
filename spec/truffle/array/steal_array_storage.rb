# truffleruby_primitives: true

# Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../../ruby/spec_helper'

describe ":steal_array_storage primitive" do
  def storage(ary)
    Truffle::Debug.array_storage(ary)
  end

  before :each do
    @array = %i[first second third]
  end

  guard -> { !Truffle::Boot.get_option('chaos-data') } do
    it "should no-op when called on itself" do
      copy = @array.dup

      Primitive.steal_array_storage(@array, @array)

      storage(@array).should == "Object[]"
      @array.should == copy
    end

    it "should take ownership of the store" do
      other = [1, 2, 3, 4, 5]
      other_copy = other.dup

      Primitive.steal_array_storage(@array, other)

      storage(@array).should == "int[]"
      @array.should == other_copy

      storage(other).should == "null"
      other.empty?.should == true
    end
  end
end
