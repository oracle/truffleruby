# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

guard -> { !Truffle.native? } do
  describe "Java.import" do

    describe "imports a class to the top-level" do
      
      it "when given a string" do
        ruby_exe(%{
          Java.import 'java.math.BigInteger'
          puts BigInteger.new('1234').toString
        }).should == "1234\n"
      end

      it "when given a list of strings" do
        ruby_exe(%{
          Java.import(
            'java.math.BigInteger',
            'java.math.BigDecimal'
          )
          puts BigDecimal.new('1234').add(BigDecimal.new(BigInteger.new('1234'))).toString
        }).should == "2468\n"
      end

      it "when given a block with a single class name" do
        ruby_exe(%{
          Java.import { java.math.BigInteger }
          puts BigInteger.new('1234').toString
        }).should == "1234\n"
      end

      it "when given a block with a single class name as a string" do
        ruby_exe(%{
          Java.import { 'java.math.BigInteger' }
          puts BigInteger.new('1234').toString
        }).should == "1234\n"
      end

      it "when given a block with a list of class names" do
        ruby_exe(%{
          Java.import {
            import java.math.BigInteger
            import java.math.BigDecimal
          }
          puts BigDecimal.new('1234').add(BigDecimal.new(BigInteger.new('1234'))).toString
        }).should == "2468\n"
      end

      it "when given a block with a list of class names as strings" do
        ruby_exe(%{
          Java.import {
            import 'java.math.BigInteger'
            import 'java.math.BigDecimal'
          }
          puts BigDecimal.new('1234').add(BigDecimal.new(BigInteger.new('1234'))).toString
        }).should == "2468\n"
      end
      
    end
    
    it "returns the imported class" do
      ruby_exe(%{
        puts Java.import('java.math.BigInteger').class.getName
      }).should == "java.math.BigInteger\n"
    end
    
    it "returns nil if nothing is imported" do
      ruby_exe(%{
        p Java.import {}
      }).should == "nil\n"
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
