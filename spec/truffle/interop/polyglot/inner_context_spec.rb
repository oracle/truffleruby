# Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../ruby/spec_helper'
require_relative '../fixtures/classes'

describe "Polyglot::InnerContext" do
  it "creates an isolated inner context" do
    TruffleInteropSpecs::OuterContextConstant = :outer

    Polyglot::InnerContext.new do |context|
      context.eval('ruby', "InnerContextConstant = :inner")
      context.eval('ruby', "!!defined?(TruffleInteropSpecs::OuterContextConstant)").should == false
      context.eval('ruby', "!!defined?(InnerContextConstant)").should == true

      defined?(TruffleInteropSpecs::OuterContextConstant).should == "constant"
      defined?(InnerContextConstant).should == nil
    end
  end

  it "treats Ruby objects from the inner context as foreign" do
    Polyglot::InnerContext.new do |context|
      obj = context.eval('ruby', "Object.new")
      Truffle::Interop.should.foreign?(obj)
      obj.to_s.should.start_with?("#<Polyglot::ForeignObject[Ruby] #<Object:")
      obj.inspect.should.start_with?("#<Polyglot::ForeignObject[Ruby] Object:")
    end
  end

  it "raise RuntimeError for if the InnerContext is closed" do
    context = nil
    obj = nil
    Polyglot::InnerContext.new do |c|
      context = c
      obj = context.eval('ruby', 'Object.new')
    end

    -> { context.eval('ruby', '42') }.should raise_error(RuntimeError, 'This Polyglot::InnerContext is closed')

    # obj.object_id # throws a host exception, but those currently cannot be caught due to the workaround for GR-22071
  end

  it "raises ArgumentError for an unknown language" do
    Polyglot::InnerContext.new do |context|
      -> { context.eval('does_not_exist', '') }.should raise_error(ArgumentError, 'Unknown language: does_not_exist')
    end
  end
end
