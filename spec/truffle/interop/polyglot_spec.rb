# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe Polyglot do

  describe ".eval(id, code)" do

    it "evals code in Ruby" do
      Polyglot.eval("ruby", "14 + 2").should == 16
    end

    it "doesn't work with MIME types" do
      -> {
        Polyglot.eval("application/x-ruby", "14 + 2")
      }.should raise_error(RuntimeError, /No language for id application\/x-ruby found/)
    end

    it "evals code in Ruby as UTF-8" do
      Polyglot.eval("ruby", "__ENCODING__.name").should == "UTF-8"
    end

    it "evals code in Ruby as UTF-8 even if it was a different encoding in the past" do
      Polyglot.eval("ruby", "__ENCODING__.name".encode("big5")).should == "UTF-8"
    end

    it "will allow code in Ruby to have a magic comment that does not change the encoding" do
      Polyglot.eval("ruby", "# encoding: utf-8\n__ENCODING__.name").should == "UTF-8"
    end

    it "will allow code in Ruby to have a magic comment that is a subset of UTF-8" do
      Polyglot.eval("ruby", "# encoding: us-ascii\n__ENCODING__.name").should == "US-ASCII"
    end

    it "will not allow code in Ruby to have a magic comment to change the encoding to something not a subset of UTF-8" do
      -> {
        Polyglot.eval("ruby", "# encoding: big5\n__ENCODING__.name")
      }.should raise_error(ArgumentError, /big5 cannot be used as an encoding for a Polyglot API Source/)
    end

  end

  describe ".eval_file(id, path)" do

    it "evals code in Ruby" do
      Polyglot.eval_file("ruby", fixture(__FILE__, "eval_file_id.rb"))
      $eval_file_id.should be_true
    end

    it "returns the returned result of the eval" do
      Polyglot.eval_file("ruby", fixture(__FILE__, "eval_file_return.rb")).should == 14
    end

    it "returns the implicit result of the eval" do
      Polyglot.eval_file("ruby", fixture(__FILE__, "eval_file_value.rb")).should == 14
    end

    it "doesn't work with MIME types" do
      -> {
        Polyglot.eval_file("application/x-ruby", fixture(__FILE__, "eval_file_id.rb"))
      }.should raise_error(RuntimeError, /No language for id application\/x-ruby found/)
    end

    it "evals code in Ruby as UTF-8" do
      Polyglot.eval_file("ruby", fixture(__FILE__, "no_magic.rb")).should == "UTF-8"
    end

    it "will allow code in Ruby to have a magic comment that does not change the encoding" do
      Polyglot.eval_file("ruby", fixture(__FILE__, "utf8_magic.rb")).should == "UTF-8"
    end

    it "will allow code in Ruby to have a magic comment that is a subset of UTF-8" do
      Polyglot.eval_file("ruby", fixture(__FILE__, "usascii_magic.rb")).should == "US-ASCII"
    end

    it "will not allow code in Ruby to have a magic comment to change the encoding" do
      -> {
        Polyglot.eval_file("ruby", fixture(__FILE__, "big5_magic.rb"))
      }.should raise_error(ArgumentError, /big5 cannot be used as an encoding for a Polyglot API Source/)
    end

  end

  describe ".eval_file(path)" do

    it "evals code in Ruby" do
      Polyglot.eval_file(fixture(__FILE__, "eval_file.rb"))
      $eval_file.should be_true
    end

  end

  describe ".as_enumerable" do

    it "evals code in Ruby" do
      enumerable = -> { Polyglot.as_enumerable([1, 2, 3]) }
      enumerable.call.min.should == 1
      enumerable.call.max.should == 3
    end

  end

end
