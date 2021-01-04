# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "#inspect and #to_s on a foreign" do
  guard -> { !TruffleRuby.native? } do
    describe "Java null" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Debug.java_null
        foreign.inspect.should == "#<Java null>"
        foreign.to_s.should == "#<Java null>"
      end
    end

    describe "Java list" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Interop.to_java_list([1, 2, 3])
        foreign.inspect.should =~ /\A#<Java java\.util\.Arrays\$ArrayList:0x\h+ \[1, 2, 3\]>\z/
        foreign.to_s.should == "#<Java [1, 2, 3]>"
      end
    end

    describe "Java array" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Interop.to_java_array([1, 2, 3])
        foreign.inspect.should =~ /\A#<Java int\[\]:0x\h+ \[1, 2, 3\]>\z/
        foreign.to_s.should == "#<Java [1, 2, 3]>"
      end
    end

    describe "Java map" do
      it "gives a similar representation to Ruby" do
        hash = {a: 1, b: 2, c: 3}
        foreign = Truffle::Interop.to_java_map(hash)

        hash.to_s.should == '{:a=>1, :b=>2, :c=>3}' # for comparison
        foreign.inspect.should =~ /\A#<Java java\.util\.HashMap:0x\h+ {"a"=>1, "b"=>2, "c"=>3}>\z/
        foreign.to_s.should == "#<Java {a=1, b=2, c=3}>"
      end
    end

    describe "Java class" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Interop.java_type("java.math.BigInteger")
        foreign.inspect.should == "#<Java class java.math.BigInteger>"
        foreign.to_s.should == "#<Java java.math.BigInteger>"
      end
    end

    describe "Java object" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Interop.java_type("java.math.BigInteger").new('14')
        foreign.inspect.should =~ /\A#<Java java\.math\.BigInteger:0x\h+ .+>\z/
        foreign.to_s.should == "#<Java 14>"
      end
    end
  end

  describe "recursive array" do
    it "gives a similar representation to Ruby" do
      x = [1, 2, 3]
      foreign = Truffle::Debug.foreign_array_from_java(Truffle::Interop.to_java_array(x))
      foreign[0] = foreign
      foreign.inspect.should =~ /\A#<Foreign:0x\h+ \[\[...\], 2, 3\]>\z/
      foreign.to_s.should == "#<Foreign [foreign array]>"
    end
  end

  describe "null" do
    it "gives a similar representation to Ruby" do
      foreign = Truffle::Debug.foreign_null
      foreign.inspect.should == "#<Foreign null>"
      foreign.to_s.should == "#<Foreign [foreign null]>"
    end
  end

  describe "executable" do
    it "gives a similar representation to Ruby" do
      foreign = Truffle::Debug.foreign_executable(14)
      foreign.inspect.should =~ /\A#<Foreign:0x\h+ proc>\z/
      foreign.to_s.should == "#<Foreign [foreign executable]>"
    end
  end

  describe "pointer" do
    it "gives a similar representation to Ruby" do
      foreign = Truffle::Debug.foreign_pointer(0x1234)
      foreign.inspect.should == "#<Foreign pointer 0x1234>"
      foreign.to_s.should == "#<Foreign [foreign pointer]>"
    end
  end

  guard -> { !TruffleRuby.native? } do
    describe "array" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Debug.foreign_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
        foreign.inspect.should =~ /\A#<Foreign:0x\h+ \[1, 2, 3\]>\z/
        foreign.to_s.should == "#<Foreign [foreign array]>"
      end

      it "gives a similar representation to Ruby, even if it is also a pointer" do
        foreign = Truffle::Debug.foreign_pointer_array_from_java(Truffle::Interop.to_java_array([1, 2, 3]))
        foreign.inspect.should =~ /\A#<Foreign pointer 0x\h+ \[1, 2, 3\]>\z/
        foreign.to_s.should == "#<Foreign [foreign pointer array]>"
      end
    end
  end

  guard -> { !TruffleRuby.native? } do
    describe "object without members" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Debug.foreign_object
        foreign.inspect.should =~ /\A#<Foreign:0x\h+>\z/
        foreign.to_s.should == "#<Foreign [foreign object]>"
      end
    end

    describe "object with members" do
      it "gives a similar representation to Ruby" do
        foreign = Truffle::Debug.foreign_object_from_map(Truffle::Interop.to_java_map({a: 1, b: 2, c: 3}))
        foreign.inspect.should =~ /\A#<Foreign:0x\h+ a=1, b=2, c=3>\z/
        foreign.to_s.should == "#<Foreign [foreign object with members]>"
      end
    end
  end
end
