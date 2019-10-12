# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

describe "Truffle::Interop.foreign_respond_to?" do

  describe "for :to_a" do

    it "and a Java array returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Interop.java_array(1, 2, 3), :to_a).should be_true
    end

    it "and a Ruby object returns false" do
      Truffle::Interop.foreign_respond_to?(Object.new, :to_a).should be_false
    end

    describe "via a direct call" do

      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_a).should be_true
      end

    end

  end

  describe "for :to_ary" do

    it "and a Java array returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Interop.java_array(1, 2, 3), :to_ary).should be_true
    end

    it "and a Ruby object returns false" do
      Truffle::Interop.foreign_respond_to?(Object.new, :to_ary).should be_false
    end

    describe "via a direct call" do

      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_ary).should be_true
      end

    end

  end

  describe "for :to_s" do

    it "and a Java class returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_class, :to_s).should be_true
    end

    it "and a Java object returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_object, :to_s).should be_true
    end

    it "and a Ruby object returns true" do
      Truffle::Interop.foreign_respond_to?(Object.new, :to_s).should be_true
    end

    describe "via a direct call" do

      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_s).should be_true
      end

    end

  end

  describe "for :to_str" do

    it "and a Java class returns false" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_class, :to_str).should be_false
    end

    it "and a Java object returns false" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_object, :to_str).should be_false
    end

    it "and a boxed string returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.foreign_string('test'), :to_str).should be_true
    end

    it "and a Ruby object returns false" do
      Truffle::Interop.foreign_respond_to?(Object.new, :to_str).should be_false
    end

    describe "via a direct call" do

      it "and a Java array returns false" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_str).should be_false
      end

    end

  end

  describe "for :is_a?" do

    it "and a Java class returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_class, :is_a?).should be_true
    end

    it "and a Java object returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_object, :is_a?).should be_true
    end

    it "and a Ruby object returns true" do
      Truffle::Interop.foreign_respond_to?(Object.new, :is_a?).should be_true
    end

    describe "via a direct call" do

      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:is_a?).should be_true
      end

    end

  end

  describe "for :class" do

    it "and a Java class returns true" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_class, :class).should be_true
    end

    it "and a Java object returns false" do
      Truffle::Interop.foreign_respond_to?(Truffle::Debug.java_object, :class).should be_false
    end

    it "and a Ruby object returns false" do
      Truffle::Interop.foreign_respond_to?(Object.new, :to_ary).should be_false
    end

    describe "via a direct call" do

      it "and a Java array returns true" do
        Truffle::Interop.java_array(1, 2, 3).respond_to?(:to_ary).should be_true
      end

    end

  end

end
