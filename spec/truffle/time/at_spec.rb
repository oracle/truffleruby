# Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Time.at" do
  it "raises RangeError for times too far in the future" do
    -> { Time.at(fixnum_max) }.should raise_error(RangeError)
    -> { Time.at(fixnum_min) }.should raise_error(RangeError)

    -> { Time.at(bignum_value) }.should raise_error(RangeError)
    -> { Time.at(-bignum_value) }.should raise_error(RangeError)
  end
end
