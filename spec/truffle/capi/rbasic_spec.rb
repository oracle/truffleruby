# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
#
require_relative '../../ruby/optional/capi/spec_helper'
load_extension("truffleruby_rbasic")

describe "RBasic support" do
  it "should raise an ArugmentError for unsupported flags" do
    specs = CApiTruffleRubyRBasicSpecs.new
    obj = Object.new

    caught = false
    begin
      specs.set_flags(obj, specs.finalize_flag)
    rescue => e
      caught = true
      e.class.should == ArgumentError
      e.to_s.should == 'unsupported remaining flags: RUBY_FL_FINALIZE (1<<7)'
    ensure
      caught.should === true
    end

    caught = false
    begin
      specs.set_flags(obj, specs.promoted_flag)
    rescue => e
      caught = true
      e.class.should == ArgumentError
      e.to_s.should == 'unsupported remaining flags: RUBY_FL_PROMOTED (1<<5 | 1<<6)'
    ensure
      caught.should === true
    end

    caught = false
    begin
      specs.set_flags(obj, 1 << 3)
    rescue => e
      caught = true
      e.class.should == ArgumentError
      e.to_s.should == 'unsupported remaining flags: unknown flag (8)'
    ensure
      caught.should === true
    end
  end

  it "should raise an ArgumentError when trying to unfreeze an object" do
    specs = CApiTruffleRubyRBasicSpecs.new
    obj = Object.new
    obj.freeze

    caught = false
    begin
      specs.set_flags(obj, 0)
    rescue => e
      caught = true
      e.class.should == ArgumentError
      e.to_s.should == 'can\'t unfreeze object'
    ensure
      caught.should === true
    end
  end
end
