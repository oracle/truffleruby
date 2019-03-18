# Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.size" do

  it "returns the size of an array" do
    Truffle::Interop.size([1, 2, 3]).should == 3
  end

  it "returns the size of a hash" do
    Truffle::Interop.size({a: 1, b: 2, c: 3}).should == 3
  end

  it "returns the size of an string" do
    Truffle::Interop.size('123').should == 3
  end

  it "returns the size of any object with a size method" do
    obj = Object.new
    def obj.size
      14
    end
    Truffle::Interop.size(obj).should == 14
  end

end
