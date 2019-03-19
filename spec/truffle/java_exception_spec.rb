# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Java exceptions" do

  it "formats to include the source information" do
    -> { Truffle::Debug.throw_java_exception 'message' }.should raise_error { |e|
      message = e.message.gsub(/:\d+/, ':LINE')
      message.lines[0].should == "message (RuntimeException)\n"
      message.lines[1].should == "\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwingMethod(TruffleDebugNodes.java:LINE)\n"
    }
  end

  it "includes the first lines of the Java stacktrace for uncaught Java exceptions" do
    -> { Truffle::Debug.throw_java_exception 'message' }.should raise_error { |e|
      message = e.message.gsub(/:\d+/, ':LINE')
      message.lines[0...4].join.should == <<EOS
message (RuntimeException)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwingMethod(TruffleDebugNodes.java:LINE)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.callingMethod(TruffleDebugNodes.java:LINE)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwJavaException(TruffleDebugNodes.java:LINE)
EOS
    }
  end

  it "formats to include the source information including cause" do
    -> { Truffle::Debug.throw_java_exception_with_cause 'message' }.should raise_error { |e|
      message = e.message.gsub(/:\d+/, ':LINE')

      message.should include <<EOS
message (RuntimeException)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:LINE)
EOS

      message.should include <<EOS
Caused by:
cause 1 (RuntimeException)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:LINE)
EOS

      message.should include <<EOS
Caused by:
cause 2 (RuntimeException)
\tfrom org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:LINE)
EOS
    }
  end

end
