# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../ruby/spec_helper'

describe "Polyglot" do
  it "gives a Ruby Class to foreign objects" do
    Truffle::Debug.foreign_object.class.should == Polyglot::ForeignObject
    Truffle::Debug.java_null.class.should == Polyglot::ForeignNull
    Truffle::Debug.foreign_executable(14).class.should == Polyglot::ForeignExecutable
    Truffle::Debug.foreign_pointer(0x1234).class.should == Polyglot::ForeignPointer
  end

  guard -> { !TruffleRuby.native? } do
    it "gives a Ruby Class to host objects" do
      Java.type('java.math.BigInteger').class.should == Polyglot::ForeignInstantiable
      Java.type('java.math.BigInteger')[:class].class.should == Polyglot::ForeignInstantiable
      Truffle::Interop.to_java_array([1, 2, 3]).class.should == Polyglot::ForeignArray
      Truffle::Interop.to_java_list([1, 2, 3]).class.should == Polyglot::ForeignArray
      Truffle::Interop.to_java_map({ a: 1 }).class.should == Polyglot::ForeignObject
    end
  end
end
