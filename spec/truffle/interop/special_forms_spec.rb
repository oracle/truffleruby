# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

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

  it "#call sends EXECUTE" do
    @object.call(1, 2, 3)
    @object.log.should include("EXECUTE(...)")
  end

  it "#nil? sends IS_NULL" do
    @object.nil?
    @object.log.should include("IS_NULL")
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

  it "#__send__ can call special forms like outgoing #inspect" do
    Truffle::Debug.foreign_object.__send__(:inspect).should =~ /#<Truffle::Interop::Foreign@\h+>/
  end

end
