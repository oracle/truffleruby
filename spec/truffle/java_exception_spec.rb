# Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Internal errors reaching the top level" do
  it "show both the Java stacktrace and Ruby backtrace" do
    file = fixture(__FILE__, 'throw_java_exception.rb')
    out = ruby_exe(file, args: "2>&1", exit_status: 1)
    out = out.gsub(/\.java:\d+/, '.java:LINE')
    out.should.include? <<-EOS
truffleruby: an internal exception escaped out of the interpreter,
please report it to https://github.com/oracle/truffleruby/issues.
    EOS
    out.should.include? "custom message (java.lang.RuntimeException)\n"

    out.should.include? "\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwingMethod(TruffleDebugNodes.java:LINE)\n"
    out.should.include? "\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.callingMethod(TruffleDebugNodes.java:LINE)\n"
    out.should.include? "\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwJavaException(TruffleDebugNodes.java:LINE)\n"

    out.should.include? "#{file}:10:in `throw_java_exception'\n"
    out.should.include? "\tfrom #{file}:10:in `foo'\n"
    out.should.include? "\tfrom #{file}:13:in `<main>'\n"
  end

  it "show the cause" do
    out = ruby_exe("Truffle::Debug.throw_java_exception_with_cause 'message'", args: "2>&1", exit_status: 1)
    message = out.gsub(/:\d+/, ':LINE')

    message.should.include? <<-EOS
message (java.lang.RuntimeException)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:LINE)
    EOS
    message.should.include? "Caused by:\ncause 1 (java.lang.RuntimeException)\n"
    message.should.include? "Caused by:\ncause 2 (java.lang.RuntimeException)\n"
  end

  it "AssertionError in another Thread is rethrown on the main Ruby Thread" do
    code = <<-RUBY
    Thread.new do
      Truffle::Debug.throw_assertion_error('custom_assertion_error_message')
    end
    sleep
    RUBY
    out = ruby_exe(code, args: "2>&1", exit_status: 1)
    $?.exitstatus.should == 1
    out.should.include? "```\nRuby Thread"
    out.should.include? "terminated with internal error: (java.lang.RuntimeException)"
    out.should.include? "Caused by:\ncustom_assertion_error_message (java.lang.AssertionError)"
  end
end
