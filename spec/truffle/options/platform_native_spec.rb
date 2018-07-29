# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "The -Xplatform.native option" do
  it "when disabled enables polyglot stdio" do
    ruby_exe("p Truffle::Boot.get_option('polyglot.stdio')", options: "-Xplatform.native=false").should == "true\n"
  end
  
  it "when disabled can run basic expressions" do
    ruby_exe("p [1, 2, 3].map(&:succ)", options: "-Xplatform.native=false").should == "[2, 3, 4]\n"
  end
  
  it "when disabled can use reasonable parts of the stdlib" do
    ruby_exe("require 'yaml'; p YAML.load('--- foo')", options: "-Xplatform.native=false").should == "\"foo\"\n"
  end
end
