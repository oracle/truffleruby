# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Java exceptions" do

  it "formats to include the source information" do
    out = ruby_exe("Truffle::Debug.throw_java_exception 'message'", args: "2>&1")
    lines = out.gsub(/:\d+/, ':LINE').lines
    lines[0].should == "truffleruby: an exception escaped out of the interpreter - this is an implementation bug\n"
    lines[1].should == "org.graalvm.polyglot.PolyglotException: java.lang.RuntimeException: message\n"
    lines[2].should == "\tat org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwingMethod(TruffleDebugNodes.java:LINE)\n"
    lines[3].should == "\tat org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.callingMethod(TruffleDebugNodes.java:LINE)\n"
    lines[4].should == "\tat org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwJavaException(TruffleDebugNodes.java:LINE)\n"
    lines.should include "\tat org.truffleruby.launcher.RubyLauncher.main(RubyLauncher.java:LINE)\n"
  end

  it "formats to include the source information including cause" do
    out = ruby_exe("Truffle::Debug.throw_java_exception_with_cause 'message'", args: "2>&1")
    message = out.gsub(/:\d+/, ':LINE')
    message.should include <<EOS
org.graalvm.polyglot.PolyglotException: java.lang.RuntimeException: message
\tat org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:LINE)
EOS
    message.should include "Caused by: java.lang.RuntimeException: cause 1"
    message.should include "Caused by: java.lang.RuntimeException: cause 2"
  end

  it "AssertionError in another Thread is rethrown on the main Ruby Thread" do
    code = <<-RUBY
    Thread.new do
      Truffle::Debug.throw_assertion_error('custom_assertion_error_message')
    end
    sleep
    RUBY
    out = ruby_exe(code, args: "2>&1")
    $?.exitstatus.should == 1
    out.should include "org.graalvm.polyglot.PolyglotException: java.lang.RuntimeException: Ruby Thread"
    out.should include "terminated with internal error:"
    out.should include "Caused by: java.lang.AssertionError: custom_assertion_error_message"
  end
end
