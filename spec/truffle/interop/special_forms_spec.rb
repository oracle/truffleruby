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
    Truffle::Debug.foreign_object.inspect.should =~ /#<Truffle::Interop::Foreign@\h+>/
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
    Truffle::Debug.foreign_object.__send__(:inspect).should =~ /#<Truffle::Interop::Foreign@\h+>/
  end

end
