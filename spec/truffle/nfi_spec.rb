# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

describe "Strings going through NFI" do
  it "the filesystem encoding is UTF-8" do
    Encoding.find('filesystem').should equal Encoding::UTF_8
  end

  describe "via strdup() are the same byte by byte" do
    before(:all) do
      Truffle::POSIX.attach_function :strdup, [:string], :string
    end

    def compare_strings(s, r)
      r.encoding.should == s.encoding
      r.bytes.should == s.bytes
      r.should == s
    end

    it "for a 7-bit String" do
      s = "abc"
      r = Truffle::POSIX.strdup(s).force_encoding("utf-8")
      compare_strings(s, r)
    end

    it "for a String with Unicode characters" do
      s = "déf"
      r = Truffle::POSIX.strdup(s).force_encoding("utf-8")
      compare_strings(s, r)
    end

    it "for a String with Japanese characters" do
      s = "こんにちは.txt"
      r = Truffle::POSIX.strdup(s).force_encoding("utf-8")
      compare_strings(s, r)
    end

    it "for a invalid UTF-8 String" do
      s = "\xa0\xa1"
      r = Truffle::POSIX.strdup(s).force_encoding("utf-8")
      compare_strings(s, r)
    end
  end
end
