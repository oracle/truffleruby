# Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Calling #equal? on a foreign object" do

  it "tests reference equality" do
    a = Truffle::Debug.foreign_object
    a.equal?(a).should be_true
    b = Truffle::Debug.foreign_object
    a.equal?(b).should be_false
  end

  guard -> { !TruffleRuby.native? } do
    it "looks at the underlying object for Java interop" do
      big_integer = Truffle::Interop.java_type("java.math.BigInteger")
      a = big_integer[:ONE]
      b = big_integer[:ONE]
      c = big_integer[:TEN]
      a.equal?(a).should be_true
      a.equal?(b).should be_true
      a.equal?(c).should be_false
    end
  end

end
