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

  it "#[] sends READ" do
    @object[:foo]
    @object['bar']
    @object.log.should include("READ(foo)")
    @object.log.should include("READ(bar)")
  end

  it "#[]= sends WRITE" do
    @object[:foo] = 1
    @object['bar'] = 2
    @object.log.should include("WRITE(foo, 1)")
    @object.log.should include("WRITE(bar, 2)")
  end

  it "#delete(name) sends REMOVE" do
    @object.delete :foo
    @object.delete 14
    @object.log.should include("REMOVE(foo)")
    @object.log.should include("REMOVE(14)")
  end

  it "#call sends EXECUTE" do
    @object.call(1, 2, 3)
    @object.log.should include("EXECUTE(...)")
  end

  it "#nil? sends IS_NULL" do
    @object.nil?
    @object.log.should include("IS_NULL")
  end

  it "#size sends GET_SIZE" do
    @object.size
    @object.log.should include("GET_SIZE")
  end

  it "#keys sends KEYS" do
    @object.keys
    @object.log.should include("KEYS")
  end

  guard -> { !TruffleRuby.native? } do
    it "#class sends READ('class') on Java class objects" do
      Java.type('java.math.BigInteger').class.getName.should == 'java.math.BigInteger'
    end
  end

  it "#name sends INVOKE" do
    @object.foo
    @object.bar(1, 2, 3)
    @object.log.should include("INVOKE(foo, ...)")
    @object.log.should include("INVOKE(bar, ...)")
  end

  it "#new sends NEW" do
    @object.new
    @object.log.should include("NEW(...)")
  end

  it "#inspect returns a useful string" do
    Truffle::Debug.foreign_object.inspect.should =~ /#<Foreign:0x\h+>/
  end

  describe "#is_a?" do
      
    guard -> { !TruffleRuby.native? } do
      
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
        lambda {
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

  it "#respond_to?(:to_a) sends HAS_SIZE" do
    @object.respond_to?(:to_a)
    @object.log.should include("HAS_SIZE")
  end

  it "#respond_to?(:to_ary) sends HAS_SIZE" do
    @object.respond_to?(:to_ary)
    @object.log.should include("HAS_SIZE")
  end

  it "#respond_to?(:new) sends IS_INSTANTIABLE" do
    @object.respond_to?(:new)
    @object.log.should include("IS_INSTANTIABLE")
  end

  it "#respond_to?(:size) sends HAS_SIZE" do
    @object.respond_to?(:size)
    @object.log.should include("HAS_SIZE")
  end

  it "#respond_to?(:keys) sends HAS_KEYS" do
    @object.respond_to?(:keys)
    @object.log.should include("HAS_KEYS")
  end

  it "#respond_to?(:inspect) is true" do
    @object.respond_to?(:inspect).should be_true
  end

  it "#respond_to?(:to_s) is true" do
    @object.respond_to?(:to_s).should be_true
  end

  it "#respond_to?(:to_str) sends IS_BOXED" do
    @object.respond_to?(:to_str)
    @object.log.should include("IS_BOXED")
  end

  it "#respond_to?(:call) sends IS_EXECUTABLE" do
    @object.respond_to?(:call)
    @object.log.should include("IS_EXECUTABLE")
  end

  it "#__send__ can call special forms like outgoing #inspect" do
    Truffle::Debug.foreign_object.__send__(:inspect).should =~ /#<Foreign:0x\h+>/
  end

end
