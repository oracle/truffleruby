# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Regexp" do
  it "raise a RegexpError and not a fatal error when it fails to parse in Joni" do
    # On MRI this actually does not raise, but we need to fix Joni first for that
    -> { Regexp.new("\\p{") }.should raise_error(RegexpError)
  end
end
