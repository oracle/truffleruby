# Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Boot.ruby_launcher" do
  it "returns a String" do
    Truffle::Boot.ruby_launcher.should be_kind_of(String)
  end

  it "returns a path to an executable" do
    File.executable?(Truffle::Boot.ruby_launcher).should == true
  end

  it "can be used to run a TruffleRuby subprocess" do
    launcher = Truffle::Boot.ruby_launcher
    `#{launcher} -e "puts RUBY_ENGINE"`.chomp.should == RUBY_ENGINE
    $?.success?.should == true
  end
end
