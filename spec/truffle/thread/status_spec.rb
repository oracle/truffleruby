# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Thread#status" do

  it "is not changed by regexp matching" do
    # A regexp taking a long time,
    # from https://swtch.com/~rsc/regexp/regexp1.html
    # Takes around 100 ms so the main thread can observe the status
    # while this thread is matching.
    n = 23
    regexp = /#{'a?' * n}#{'a' * n}/
    string = 'a' * n

    t = Thread.new do
      regexp =~ string
    end

    while status = t.status
      status.should == "run"
      Thread.pass
    end
    t.join
  end

end
