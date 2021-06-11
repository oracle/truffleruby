# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby_foreign_caller")

describe "Calling a method needing the caller frame" do
  before :each do
    @s = CApiTruffleRubyForeignCallerSpecs.new
  end

  it "directly from C code raises a RuntimeError" do
    -> {
      @s.call_binding
    }.should raise_error(RuntimeError, 'Cannot call Ruby method which needs caller data directly in a foreign language')
  end

  it "using rb_funcall() yields the Binding of rb_funcall()" do
    caller_variable = nil
    binding = @s.call_binding_rb_funcall
    binding.should be_kind_of(Binding)

    # On CRuby it would instead return the Binding of the caller Ruby frame
    binding.local_variables.should.include?(:args)
    binding.local_variables.should_not.include?(:caller_variable)

    caller_variable.should == nil
  end
end
