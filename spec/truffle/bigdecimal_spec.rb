# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

require 'bigdecimal'

describe "BigDecimal" do

  it "pre-coerces long integers" do
    BigDecimal(3).add(1 << 50, 3).should == BigDecimal('0.113e16')
  end

  describe "bug GR-16506" do

    before :each do
      @a = BigDecimal('166.666666666')
      @b = Rational(500, 3)
      @c = @a - @b
    end

    # Check the input is as we understand it

    it "has the LHS print as expected" do
      @a.to_s.should == "0.166666666666e3"
      @a.to_f.to_s.should == "166.666666666"
      Float(@a).to_s.should == "166.666666666"
    end

    it "has the RHS print as expected" do
      @b.to_s.should == "500/3"
      @b.to_f.to_s.should == "166.66666666666666"
      Float(@b).to_s.should == "166.66666666666666"
    end

    it "has the expected precision on the LHS" do
      @a.precs[0].should == 18
    end

    it "has the expected maximum precision on the LHS" do
      @a.precs[1].should == 27
    end

    it "produces the expected result when done via Float" do
      (Float(@a) - Float(@b)).to_s.should == "-6.666596163995564e-10"
    end

    it "produces the expected result when done via to_f" do
      (@a.to_f - @b.to_f).to_s.should == "-6.666596163995564e-10"
    end

    # Check underlying methods work as we understand

    it "BigDecimal precision is the number of digits rounded up to a multiple of nine" do
      1.upto(100) do |n|
        b = BigDecimal('4' * n)
        precs, _ = b.precs
        (precs >= 9).should be_true
        (precs >= n).should be_true
        (precs % 9).should == 0
      end
      BigDecimal('NaN').precs[0].should == 9
    end

    it "BigDecimal maximum precision is nine more than precision except for abnormals" do
      1.upto(100) do |n|
        b = BigDecimal('4' * n)
        precs, max = b.precs
        max.should == precs + 9
      end
      BigDecimal('NaN').precs[1].should == 9
    end

    it "BigDecimal(Rational, 18) produces the result we expect" do
      BigDecimal(@b, 18).to_s.should == "0.166666666666666667e3"
    end

    it "BigDecimal(Rational, BigDecimal.precs[0]) produces the result we expect" do
      BigDecimal(@b, @a.precs[0]).to_s.should == "0.166666666666666667e3"
    end

    it "BigDecimal(Rational) with bigger-than-double numerator" do
      rational = 99999999999999999999/100r
      rational.numerator.should > 2**64
      BigDecimal(rational, 100).to_s.should == "0.99999999999999999999e18"
    end

    # Check the top-level expression works as we expect

    it "produces a BigDecimal" do
      @c.class.should == BigDecimal
    end

    it "produces the expected result" do
      @c.should == BigDecimal("-0.666667e-9")
      @c.to_s.should == "-0.666667e-9"
    end

    it "produces the correct class for other artihmetic operators" do
      (@a + @b).class.should == BigDecimal
      (@a * @b).class.should == BigDecimal
      (@a / @b).class.should == BigDecimal
      (@a % @b).class.should == BigDecimal
    end

  end

end
