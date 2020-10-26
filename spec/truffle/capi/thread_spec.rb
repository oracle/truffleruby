# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby_thread")

describe "TruffleRuby C-API Thread function" do
  before :each do
    @t = CApiTruffleRubyThreadSpecs.new
  end

  describe "rb_thread_call_without_gvl" do
    it "runs a native function with the global lock unlocked" do
      @t.rb_thread_call_without_gvl_native_function.should == Process.pid
    end
  end
end
