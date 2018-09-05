# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Inspect on a foreign" do

  guard -> { !TruffleRuby.native? } do

    describe "Java list" do
      
      it "gives a similar representation to Ruby" do
        Truffle::Interop.to_java_list([1, 2, 3]).inspect.should =~ /#<Java:0x\h+ \[1, 2, 3\]>/
      end
      
    end

    describe "Java array" do
      
      it "gives a similar representation to Ruby" do
        Truffle::Interop.to_java_array([1, 2, 3]).inspect.should =~ /#<Java:0x\h+ \[1, 2, 3\]>/
      end
      
    end

    describe "Java map" do
      
      it "gives a similar representation to Ruby" do
        Truffle::Interop.to_java_map({a: 1, b: 2, c: 3}).inspect.should =~ /#<Java:0x\h+ {a=>1, b=>2, c=>3}>/
      end
      
    end
    
    describe "Java class" do
      
      it "gives a similar representation to Ruby" do
        Truffle::Interop.java_type("java.math.BigInteger")
          .inspect.should == "#<Java class java.math.BigInteger>"
      end
      
    end
    
    describe "Java object" do
      
      it "gives a similar representation to Ruby" do
        Truffle::Interop.java_type("java.math.BigInteger").new('14')
          .inspect.should =~ /#<Java:0x\h+ object java.math.BigInteger>/
      end
      
    end

    describe "null" do

      it "gives a similar representation to Ruby" do
        Truffle::Debug.foreign_null.inspect.should =~ /#<Foreign null>/
      end
      
    end
    
  end

  describe "executable" do
    
    it "gives a similar representation to Ruby" do
      Truffle::Debug.foreign_executable(14).inspect.should =~ /#<Foreign:0x\h+ proc>/
    end
    
  end
  
  describe "pointer" do
    
    it "gives a similar representation to Ruby" do
      Truffle::Debug.foreign_pointer(0x1234).inspect.should == "#<Foreign pointer 0x1234>"
    end
    
  end
  
  guard -> { !TruffleRuby.native? } do
    
    describe "array" do
      
      it "gives a similar representation to Ruby" do
        array = Truffle::Debug.foreign_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
        array.inspect.should =~ /#<Foreign:0x\h+ \[1, 2, 3\]>/
      end
      
    end
    
  end

  describe "executable" do
    
    it "gives a similar representation to Ruby" do
      Truffle::Debug.foreign_executable(14).inspect.should =~ /#<Foreign:0x\h+ proc>/
    end
    
  end
  
  guard -> { !TruffleRuby.native? } do

    describe "object" do

      it "gives a similar representation to Ruby" do
        object = Truffle::Debug.foreign_object_from_map(Truffle::Interop.to_java_map({a: 1, b: 2, c: 3}))
        object.inspect.should =~ /#<Foreign:0x\h+ a=1, b=2, c=3>/
      end
      
    end
    
  end

end
