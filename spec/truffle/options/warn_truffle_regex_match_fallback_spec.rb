# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "The --warn-truffle-regex-match-fallback option" do
  it "works" do
    out = ruby_exe('2.times { /a*+/ =~ "a" }', options: "--experimental-options --warn-truffle-regex-match-fallback", args: "2>&1")
    out = out.gsub(/^.+: warning/, 'warning')
    out.should == <<~OUT
      warning: match_in_region(/a*+/, "a"@UTF-8, 0, 1, false, 0) enc=US-ASCII cannot be run as a Truffle regexp and fell back to Joni
      warning: match_in_region(/a*+/, "a"@UTF-8, 0, 1, false, 0) enc=US-ASCII cannot be run as a Truffle regexp and fell back to Joni
    OUT
  end
end
