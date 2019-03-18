# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Interop special form" do

  before :each do
    @object = Truffle::Interop.logging_foreign_object
  end

  it "#[] sends readMember(*)" do
    @object[:foo] rescue nil
    @object['bar'] rescue nil
    @object.to_s.should include("readMember(foo)")
    @object.to_s.should include("readMember(bar)")
  end

  it "#[]= sends writeMember(*)" do
    (@object[:foo] = 1) rescue nil
    (@object['bar'] = 2) rescue nil
    @object.to_s.should include("writeMember(foo, 1)")
    @object.to_s.should include("writeMember(bar, 2)")
  end

  # FIXME (pitr-ch 18-Mar-2019): break down
  it "#delete(name) sends removeMember(*) or removeArrayElement(*)" do
    @object.delete :foo rescue nil
    @object.delete 14 rescue nil
    @object.to_s.should include("removeMember(foo)")
    @object.to_s.should include("removeArrayElement(14)")
  end

  it "#call sends execute(*)" do
    @object.call(1, 2, 3) rescue nil
    @object.to_s.should include("execute(1, 2, 3)")
  end

  it "#nil? sends isNull()" do
    @object.nil?
    @object.to_s.should include("isNull()")
  end

  it "#size sends getArraySize()" do
    @object.size rescue nil
    @object.to_s.should include("getArraySize()")
  end

  it "#keys sends getMembers(false)" do
    @object.keys rescue nil
    @object.to_s.should include("getMembers(false)")
  end

  guard -> { !TruffleRuby.native? } do
    it "#class sends readMember('class') on Java class objects" do
      Java.type('java.math.BigInteger').class.getName.should == 'java.math.BigInteger'
    end
  end

  it "#name sends invokeMember(*)" do
    @object.foo rescue nil
    @object.bar(1, 2, 3) rescue nil
    @object.to_s.should include("invokeMember(foo)")
    @object.to_s.should include("invokeMember(bar, 1, 2, 3)")
  end

  it "#new sends instantiate()" do
    @object.new rescue nil
    @object.to_s.should include("instantiate()")
  end

  describe "#is_a?" do

    guard -> { !TruffleRuby.native? } do

      it "returns false for Java null" do
        big_integer_class = Truffle::Interop.java_type("java.math.BigInteger")
        Truffle::Debug.java_null.is_a?(big_integer_class).should be_false
      end

      it "returns true for a directly matching Java object and class" do
        big_integer_class = Truffle::Interop.java_type("java.math.BigInteger")
        big_integer = big_integer_class.new("14")
        big_integer.is_a?(big_integer_class).should be_true
      end

      it "returns true for a directly matching Java object and superclass" do
        big_integer_class = Truffle::Interop.java_type("java.math.BigInteger")
        big_integer = big_integer_class.new("14")
        number_class = Truffle::Interop.java_type("java.lang.Number")
        big_integer.is_a?(number_class).should be_true
      end

      it "returns true for a directly matching Java object and interface" do
        big_integer_class = Truffle::Interop.java_type("java.math.BigInteger")
        big_integer = big_integer_class.new("14")
        serializable_interface = Truffle::Interop.java_type("java.io.Serializable")
        big_integer.is_a?(serializable_interface).should be_true
      end

      it "returns false for an unrelated Java object and Java class" do
        big_integer_class = Truffle::Interop.java_type("java.math.BigInteger")
        big_integer = big_integer_class.new("14")
        big_decimal_class = Truffle::Interop.java_type("java.math.BigDecimal")
        big_integer.is_a?(big_decimal_class).should be_false
      end

      it "returns false for a Java object and a Ruby class" do
        java_hash = Truffle::Interop.java_type("java.util.HashMap").new
        java_hash.is_a?(Hash).should be_false
      end

      it "raises a type error for a non-Java foreign object and a non-Java foreign class" do
        -> {
          Truffle::Debug.foreign_object.is_a?(Truffle::Debug.foreign_object)
        }.should raise_error(TypeError, /cannot check if a foreign object is an instance of a foreign class/)
      end

      it "works with boxed primitives" do
        boxed_integer = Truffle::Debug.foreign_boxed_number(14)
        boxed_integer.is_a?(Integer).should be_true
        boxed_double = Truffle::Debug.foreign_boxed_number(14.2)
        boxed_double.is_a?(Float).should be_true
      end

    end

    it "returns false for a non-Java foreign object and a Ruby class" do
      Truffle::Debug.foreign_object.is_a?(Hash).should be_false
    end

  end

  it "#respond_to?(:to_a) sends hasArrayElements()" do
    @object.respond_to?(:to_a)
    @object.to_s.should include("hasArrayElements()")
  end

  it "#respond_to?(:to_ary) sends hasArrayElements()" do
    @object.respond_to?(:to_ary)
    @object.to_s.should include("hasArrayElements()")
  end

  it "#respond_to?(:new) sends isInstantiable()" do
    @object.respond_to?(:new)
    @object.to_s.should include("isInstantiable()")
  end

  it "#respond_to?(:size) sends hasArrayElements()" do
    @object.respond_to?(:size)
    @object.to_s.should include("hasArrayElements()")
  end

  it "#respond_to?(:keys) sends hasMembers()" do
    @object.respond_to?(:keys)
    @object.to_s.should include("hasMembers()")
  end

  it "#respond_to?(:inspect) is true" do
    @object.respond_to?(:inspect).should be_true
    @object.to_s.should include("isString()")
  end

  it "#respond_to?(:to_s) is true" do
    @object.respond_to?(:to_s).should be_true
    @object.to_s.should include("isString()")
  end

  # FIXME (pitr-ch 18-Mar-2019): break down to new messages, test isNumber and isBoolean separately
  it "#respond_to?(:to_str) sends IS_BOXED" do
    @object.respond_to?(:to_str)
    @object.to_s.should include("isString()")
  end

  it "#respond_to?(:call) sends isExecutable()" do
    @object.respond_to?(:call)
    @object.to_s.should include("isExecutable()")
  end

end
