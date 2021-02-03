# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

# This spec can also run on CRuby, for comparison
describe "Always-inlined core methods" do
  describe "are part of the backtrace if the error happens inside that core method" do
    it "for #public_send" do
      -> {
        public_send()
      }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == 'public_send' }
      -> {
        public_send(Object.new)
      }.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == 'public_send' }
    end

    guard -> { RUBY_ENGINE != "ruby" } do
      it "for #send" do
        -> {
          send()
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == '__send__' }
        -> {
          send(Object.new)
        }.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == '__send__' }
      end

      it "for #__send__" do
        -> {
          __send__()
        }.should raise_error(ArgumentError) { |e| e.backtrace_locations[0].label.should == '__send__' }
        -> {
          __send__(Object.new)
        }.should raise_error(TypeError) { |e| e.backtrace_locations[0].label.should == '__send__' }
      end
    end
  end

  describe "are not part of the backtrace if the error happens in a different method" do
    it "for #public_send" do
      -> {
        public_send(:yield_self) { raise "foo" }
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[1].label.should == 'yield_self'
        if RUBY_ENGINE == 'ruby'
          e.backtrace_locations[2].label.should == 'public_send'
        else
          e.backtrace_locations[2].label.should.start_with?('block (4 levels)')
        end
      }
    end

    it "for #send" do
      -> {
        send(:yield_self) { raise "foo" }
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[1].label.should == 'yield_self'
        e.backtrace_locations[2].label.should.start_with?('block (4 levels)')
      }
    end

    it "for #__send__" do
      -> {
        __send__(:yield_self) { raise "foo" }
      }.should raise_error(RuntimeError, "foo") { |e|
        e.backtrace_locations[0].label.should.start_with?('block (5 levels)')
        e.backtrace_locations[1].label.should == 'yield_self'
        e.backtrace_locations[2].label.should.start_with?('block (4 levels)')
      }
    end
  end
end
