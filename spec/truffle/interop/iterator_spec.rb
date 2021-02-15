# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/spec_helper'
require_relative 'fixtures/classes'

describe "Truffle::Interop iterator messages" do
  it "allow to iterate an array" do
    obj = [1, 2]
    Truffle::Interop.should.has_iterator?(obj)
    iterator = Truffle::Interop.iterator(obj)
    Truffle::Interop.should.iterator?(iterator)

    Truffle::Interop.has_iterator_next_element?(iterator).should == true
    Truffle::Interop.iterator_next_element(iterator).should == 1
    Truffle::Interop.has_iterator_next_element?(iterator).should == true
    Truffle::Interop.iterator_next_element(iterator).should == 2
    Truffle::Interop.has_iterator_next_element?(iterator).should == false
    -> { Truffle::Interop.iterator_next_element(iterator) }.should raise_error(StopIteration)
  end
end
