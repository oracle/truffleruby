# Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'

guard -> { !TruffleRuby.native? } do
  describe "Java.synchronized" do

    it "yields to the block" do
      object = Object.new
      a = 1
      Java.synchronized(object) do
        a = 2
      end
      a.should == 2
    end

    it "is re-entrant" do
      object = Object.new
      a = 1
      Java.synchronized(object) do
        Java.synchronized(object) do
          a = 2
        end
      end
      a.should == 2
    end

    it "cannot be used with a primitive" do
      [true, 14, 14.2].each do |primitive|
        -> {
          Java.synchronized(primitive) { }
        }.should raise_error(TypeError)
      end
    end

  end
end
