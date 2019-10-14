# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "For backwards compatibility" do

  describe "Truffle::AtomicReference" do
    it "is a subclass of as TruffleRuby::AtomicReference" do
      Truffle::AtomicReference.should < TruffleRuby::AtomicReference
    end
  end

  describe "Truffle::Primitive.logical_processors" do
    it "gives the number of available processors" do
      Truffle::Primitive.logical_processors.should == Truffle::System.available_processors
    end
  end

  describe "Truffle::System.full_memory_barrier" do
    it "is the same as TruffleRuby.full_memory_barrier" do
      # Best we can do
      defined?(Truffle::System.full_memory_barrier).should == 'method'
    end
  end

end
