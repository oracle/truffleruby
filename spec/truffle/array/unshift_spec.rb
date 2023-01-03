# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Array#unshift" do
  it "doesn't rely on Array#[]= so it can be overridden" do
    subclass = Class.new(Array) do
      def []=(*)
        raise "[]= is called"
      end
    end

    array = subclass.new
    array.unshift(1)
    array.to_a.should == [1]
  end
end