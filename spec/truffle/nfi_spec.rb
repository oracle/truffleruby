# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

describe "Strings going through NFI" do
  describe "via strdup() are the same byte by byte" do
    before(:all) do
      Truffle::POSIX.attach_function :strdup, [:string], :string
    end

    it "for a 7-bit String" do
      s = "abc"
      r = Truffle::POSIX.strdup(s)
      r.encoding.should equal Encoding::BINARY
      r.bytes.should == s.bytes
    end

    it "for a String with Unicode characters" do
      s = "déf"
      r = Truffle::POSIX.strdup(s)
      r.bytes.should == s.bytes
    end

    it "for a String with Japanese characters" do
      s = "こんにちは.txt"
      r = Truffle::POSIX.strdup(s)
      r.bytes.should == s.bytes
    end

    it "for a invalid UTF-8 String" do
      s = "\xa0\xa1"
      r = Truffle::POSIX.strdup(s)
      r.bytes.should == s.bytes
    end
  end
end
