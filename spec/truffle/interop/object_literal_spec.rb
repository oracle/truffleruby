# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.object_literal" do
  
  it "creates an object with fields set" do
    o = Truffle::Interop.object_literal(a: 1, b: 2, c: 3)
    o.a.should == 1
    o.b.should == 2
    o.c.should == 3
  end
  
  it "returns nil for fields not set" do
    o = Truffle::Interop.object_literal(a: 1, b: 2)
    o.c.should be_nil
  end
  
  it "allows new fields to be set" do
    o = Truffle::Interop.object_literal(a: 1, b: 2)
    o.c = 3
    o.c.should == 3
  end
  
  it "allows fields to be modified" do
    o = Truffle::Interop.object_literal(a: 1, b: 2, c: 3)
    o.c.should == 3
    o.c = 4
    o.c.should == 4
  end
  
  it "responds properly to KEYS" do
    o = Truffle::Interop.object_literal(a: 1, b: 2, c: 3)
    Truffle::Interop.keys(o).sort.should == ['a', 'b', 'c']
  end
  
  it "responds properly to READ" do
    o = Truffle::Interop.object_literal(a: 1, b: 2, c: 3)
    Truffle::Interop.read(o, 'a').should == 1
    Truffle::Interop.read(o, 'b').should == 2
    Truffle::Interop.read(o, 'c').should == 3
  end
  
  it "responds properly to WRITE for a new field" do
    o = Truffle::Interop.object_literal(a: 1, b: 2)
    Truffle::Interop.write(o, 'c', 3)
    o.c.should == 3
  end
  
  it "responds properly to WRITE for a new value for an existing field" do
    o = Truffle::Interop.object_literal(a: 1, b: 2, c: 3)
    o.c.should == 3
    Truffle::Interop.write(o, 'c', 4)
    o.c.should == 4
  end
  
end
