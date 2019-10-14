# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

guard -> { !TruffleRuby.native? } do
  describe "Java.import" do

    it "imports a class to the top-level" do
      ruby_exe(%{
        Java.import 'java.math.BigInteger'
        puts BigInteger.new('1234').toString
      }).should == "1234\n"
    end

    it "returns the imported class" do
      ruby_exe(%{
        puts Java.import('java.math.BigInteger').class.getName
      }).should == "java.math.BigInteger\n"
    end

    it "can import classes twice" do
      ruby_exe(%{
        Java.import 'java.math.BigInteger'
        Java.import 'java.math.BigInteger'
        puts BigInteger.new('1234').toString
      }).should == "1234\n"
    end

    it "raises an error if the constant is already defined" do
      ruby_exe(%{
        BigInteger = '14'
        begin
          Java.import 'java.math.BigInteger'
        rescue NameError => e
          puts e
        end
      }).should == "constant BigInteger already set\n"
    end

  end
end
