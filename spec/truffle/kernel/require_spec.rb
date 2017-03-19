# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../../ruby/spec_helper'

describe "Kernel.require" do

  it "requires provided features" do
    lambda { require("rational") }.should_not raise_error
    lambda { require("enumerator") }.should_not raise_error
    lambda { require("thread") }.should_not raise_error
    lambda { require("complex") }.should_not raise_error
  end

end
