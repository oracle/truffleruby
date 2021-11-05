# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "The --warn-truffle-regex-compile-fallback option" do
  it "works" do
    out = ruby_exe('2.times { /a*+/ =~ "a" }', options: "--experimental-options --warn-truffle-regex-compile-fallback", args: "2>&1")
    out = out.gsub(/^.+: warning/, 'warning')
    out.should == <<~OUT
      warning: Regexp /a*+/ at_start=false encoding=US-ASCII cannot be compiled to a Truffle regexp and fell back to Joni
    OUT
  end
end
