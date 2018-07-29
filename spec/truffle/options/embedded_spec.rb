# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "The -Xembedded option" do
  it "is disabled by default by the launcher" do
    ruby_exe("p Truffle::Boot.get_option('embedded')").should == "false\n"
  end
  
  it "can be set manually, even though it's set by the launcher" do
    ruby_exe("p Truffle::Boot.get_option('embedded')", options: "-Xembedded").should == "true\n"
  end
  
  it "sets dependent options when set manually" do
    ruby_exe("p Truffle::Boot.get_option('single_threaded')", options: "-Xembedded").should == "true\n"
  end
end
