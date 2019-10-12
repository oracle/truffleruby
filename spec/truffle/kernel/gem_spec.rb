# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Kernel#gem" do
  it "returns true for included gems" do
    require "rubygems"
    gem("json").should == true
    gem("minitest").should == true
    gem("power_assert").should == true
    gem("psych").should == true
    gem("rake").should == true
    gem("rdoc").should == true
  end
end
