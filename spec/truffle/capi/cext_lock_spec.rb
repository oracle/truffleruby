# Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension("truffleruby_lock")

describe "TruffleRuby C-ext lock" do
  before :each do
    @t = CApiTruffleRubyLockSpecs.new
  end

  it "is not acquired in Ruby code" do
    Truffle::CExt.cext_lock_owned?.should == false
  end

  guard -> { Truffle::Boot.get_option 'cexts-lock' } do
    it "is acquired in C ext code" do
      @t.has_lock?.should == true
    end
  end

  it "is released inside rb_thread_call_without_gvl" do
    @t.has_lock_in_call_without_gvl?.should == false
  end

  it "is released inside rb_funcall" do
    @t.has_lock_in_rb_funcall?(Truffle::CExt).should == false
  end
end
