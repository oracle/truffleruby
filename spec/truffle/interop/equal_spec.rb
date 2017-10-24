# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Calling #equal? on a foreign object" do
  it "tests reference equality" do
    a = Truffle::Debug.foreign_object
    a.equal?(a).should == true
    b = Truffle::Debug.foreign_object
    a.equal?(b).should == false
  end

  it "returns false for other objects" do
    foreign = Truffle::Debug.foreign_object
    foreign.equal?(Object.new).should == false
    foreign.equal?(42).should == false
  end
end
