# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.respond_to?" do
  
  describe "for :to_a" do
    
    it "and a Java array returns true" do
      Truffle::Interop.respond_to?(Truffle::Interop.java_array(1, 2, 3), :to_a).should be_true
    end
    
    it "and a Ruby object returns false" do
      Truffle::Interop.respond_to?(Object.new, :to_a).should be_false
    end
    
    describe "via a direct call" do
  
      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_a).should be_true
      end
  
    end

  end
  
  describe "for :to_ary" do
    
    it "and a Java array returns true" do
      Truffle::Interop.respond_to?(Truffle::Interop.java_array(1, 2, 3), :to_ary).should be_true
    end
    
    it "and a Ruby object returns false" do
      Truffle::Interop.respond_to?(Object.new, :to_ary).should be_false
    end
    
    describe "via a direct call" do
  
      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_ary).should be_true
      end
  
    end

  end

end
