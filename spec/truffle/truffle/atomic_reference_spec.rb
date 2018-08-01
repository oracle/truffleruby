# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'
require 'bigdecimal'

describe "Truffle::AtomicReference" do

  it "#new creates new instance with a value and get reads it" do
    r = Truffle::AtomicReference.new :value
    r.get.should equal :value
    r.value.should equal :value
  end

  it "#set and #value= changes the value" do
    r = Truffle::AtomicReference.new :v1

    r.value = :v2
    r.get.should equal :v2
    r.value.should equal :v2

    r.set :v3
    r.get.should equal :v3
    r.value.should equal :v3
  end

  it "can be marshaled" do
    r = Truffle::AtomicReference.new :value
    copy = Marshal.load Marshal.dump(r)
    copy.get.should equal :value
  end

  describe "#compare_and_set" do
    it "compares regular objects with identity" do
      always_equal = Class.new { define_method :==, &-> _ { true } }

      v1 = always_equal.new
      bad = always_equal.new
      v2 = always_equal.new
      r = Truffle::AtomicReference.new v1

      r.compare_and_set(bad, v2).should == false
      r.get.should equal v1

      r.compare_and_set(v1, v2).should == true
      r.get.should equal v2
    end

    it "compares numeric objects with equality " do
      [-> { 1 },
       -> { 1.1 },
       -> { 1000 },
       -> { Rational(313124, 576434138) },
       -> { BigDecimal('64643467448446548974.285454687534') }
      ].each do |numeric_source|
        v1 = numeric_source.call
        bad = v1 + 1
        v2 = v1 + 2
        r = Truffle::AtomicReference.new v1

        r.compare_and_set(bad, v2).should == false
        r.get.should equal v1

        r.compare_and_set(numeric_source.call, v2).should == true
        r.get.should equal v2
      end
    end
  end

  it "#get_and_set" do
    r = Truffle::AtomicReference.new :v1

    r.get_and_set(:v2).should == :v1
    r.get.should == :v2
  end

end

