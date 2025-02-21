# truffleruby_primitives: true
#   (to nil out references to make unreachable)

# Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "ObjectSpace::WeakKeyMap" do

  it "gets rid of unreferenced keys" do
    map = ObjectSpace::WeakKeyMap.new
    key = "a".upcase
    value = "x"
    map[key] = value
    key = nil

    Primitive.gc_force

    map[key].should == nil
    map.key?(key).should == false
    map.getkey(key).should == nil
  end

  it "has iterators methods that exclude unreferenced objects" do
    # This spec does not pass on MRI because the garbage collector is presumably too conservative and will not get rid
    # of the references eagerly enough.

    map = ObjectSpace::WeakKeyMap.new
    k1, k2 = %w[a b].map(&:upcase)
    v1, v2 = %w[x y].map(&:upcase)
    map[k1] = v1
    map[k2] = v2
    k2 = nil

    Primitive.gc_force

    map.key?(k2).should == false
    map[k2].should == nil

    map.key?(k1).should == true
    map[k1].should == v1
  end

  it "supports frozen objects" do
    map = ObjectSpace::WeakKeyMap.new
    k = "x".freeze
    v = "y".freeze
    map[k] = v
    Primitive.gc_force
    map[k].should == v
  end
end
