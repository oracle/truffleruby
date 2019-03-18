# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "The $SAFE variable" do
  it "does not warn when set to 0 and remembers the value" do
    ruby_exe("$SAFE = 0; puts $SAFE; puts Thread.current.safe_level", args: "2>&1").should == "0\n0\n"
  end

  it "raises an error when set to 1" do
    lambda {
      $SAFE = 1
    }.should raise_error(SecurityError, /Setting \$SAFE is no longer supported/)
  end
end
