# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.new" do
  
  class NewTestClass
    
    attr_reader :x
    
    def initialize(a, b)
      @x = a + b
    end
    
  end
  
  it "creates new instances of objects" do
    Truffle::Interop.new(NewTestClass, 14, 2).should be_an_instance_of NewTestClass
  end
  
  it "calls initialize" do
    Truffle::Interop.new(NewTestClass, 14, 2).x.should == 16
  end

end
