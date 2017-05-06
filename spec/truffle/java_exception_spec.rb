# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

describe "Java exceptions" do
  
  it "formats to include the source information" do
    message = <<~MESSAGE
    
    RuntimeException message org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionNode.throwJavaException(TruffleDebugNodes.java:316)
    MESSAGE
    
    message.chomp!
    
    lambda { Truffle::Debug.throw_java_exception 'message' }.should raise_error { |e|
      e.message.should == message
    }
  end
  
  it "formats to include the source information including cause" do
    message = <<~MESSAGE
    
    RuntimeException message org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:327)
    Caused by: RuntimeException cause 1 org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:327)
    Caused by: RuntimeException cause 2 org.truffleruby.debug.TruffleDebugNodes$ThrowJavaExceptionWithCauseNode.throwJavaExceptionWithCause(TruffleDebugNodes.java:327)
    MESSAGE
    
    message.chomp!
    
    lambda { Truffle::Debug.throw_java_exception_with_cause 'message' }.should raise_error { |e|
      e.message.should == message
    }
  end

end
