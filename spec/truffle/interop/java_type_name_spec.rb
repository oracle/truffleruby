# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

guard -> { !Truffle.native? } do
  describe "Truffle::Interop.java_type_name" do

    it "gives a name for a primitive type" do
      Truffle::Interop.java_type_name(Truffle::Interop.java_type("int")).should == "int"
    end

    it "gives a name for a primitive type array" do
      Truffle::Interop.java_type_name(Truffle::Interop.java_type("int[]")).should == "[I"
    end

    it "gives a name for a class" do
      Truffle::Interop.java_type_name(Truffle::Interop.java_type("java.math.BigInteger")).should == "java.math.BigInteger"
    end

    it "gives a name for a class array" do
      Truffle::Interop.java_type_name(Truffle::Interop.java_type("java.math.BigInteger[]")).should == "[Ljava.math.BigInteger;"
    end

    it "raises a type error for something that isn't a Java class" do
      lambda { Truffle::Interop.java_type_name(14) }.should raise_error(TypeError)
    end

  end
end
