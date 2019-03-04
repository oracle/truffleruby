# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Options" do

  describe "can correctly parse" do

    it "booleans" do
      ruby_exe("p Truffle::Boot.get_option('frozen_string_literals')").should_not == "true\n"
      ruby_exe("p Truffle::Boot.get_option('frozen_string_literals')", options: "--frozen_string_literals=true").should == "true\n"
      ruby_exe("p Truffle::Boot.get_option('frozen_string_literals')", options: "--frozen_string_literals").should == "true\n"
    end

    it "integers" do
      ruby_exe("p Truffle::Boot.get_option('default_cache')").should_not == "99\n"
      ruby_exe("p Truffle::Boot.get_option('default_cache')", options: "--default_cache=99").should == "99\n"
    end

    it "enum values" do
      ruby_exe("p Truffle::Boot.get_option('verbose')").should_not == ":NIL\n"
      ruby_exe("p Truffle::Boot.get_option('verbose')", options: "--verbose=NIL").should == ":NIL\n"
      ruby_exe("p Truffle::Boot.get_option('verbose')", options: "--verbose").should == ":TRUE\n"
    end

    it "strings" do
      ruby_exe("p Truffle::Boot.get_option('launcher')", options: "--launcher=ruby_spec_test_value").should == "\"ruby_spec_test_value\"\n"
      ruby_exe("p Truffle::Boot.get_option('launcher')", options: "'--launcher=ruby spec test value with spaces'").should == "\"ruby spec test value with spaces\"\n"
    end

    it "arrays of strings" do
      ruby_exe("p Truffle::Boot.get_option('load_paths')", options: "--load_paths=ruby_spec_test_value").should == "[\"ruby_spec_test_value\"]\n"
      ruby_exe("p Truffle::Boot.get_option('load_paths')", options: "--load_paths=a,b,c").should == "[\"a\", \"b\", \"c\"]\n"
      ruby_exe("p Truffle::Boot.get_option('load_paths')", options: "--load_paths=a\\\\,b,c").should == "[\"a,b\", \"c\"]\n"
    end

  end

  describe "handles parsing errors with" do

    it "booleans" do
      ruby_exe("14", options: "--frozen_string_literals=foo", args: "2>&1").should include("Invalid boolean option value 'foo'")
    end

    it "integers" do
      ruby_exe("14", options: "--default_cache=foo", args: "2>&1").should include("Invalid argument --default_cache=foo specified")
    end

    it "enum values" do
      ruby_exe("14", options: "--verbose=foo", args: "2>&1").should include("Invalid argument --verbose=foo specified. No enum constant org.truffleruby.shared.options.Verbosity.FOO'")
    end

  end

end
