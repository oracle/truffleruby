# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

describe "splat" do

    it "should allow array modification by arguments following splat" do
         o = Object.new
         def o.test(*arguments)
           arguments
         end
         args = [1,2,3]
         o.test(*args, args.pop).size.should == 3
    end

end
