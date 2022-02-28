# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../../ruby/spec_helper'

describe "Polyglot" do
  it "gives a Ruby Class to foreign objects" do
    Truffle::Debug.foreign_object.class.should == Polyglot::ForeignObject

    Truffle::Debug.foreign_hash.class.should == Polyglot::ForeignHash
    Truffle::Debug.foreign_array.class.should == Polyglot::ForeignArray
    Truffle::Debug.foreign_exception("").class.should == Polyglot::ForeignExceptionClass
    Truffle::Debug.foreign_executable(14).class.should == Polyglot::ForeignExecutable
    # ForeignInstantiable
    Truffle::Debug.foreign_iterable.class.should == Polyglot::ForeignIterable
    Truffle::Debug.foreign_iterator.class.should == Polyglot::ForeignIterator
    Truffle::Debug.java_null.class.should == Polyglot::ForeignNull
    Truffle::Debug.foreign_boxed_value(42).class.should == Polyglot::ForeignNumber
    Truffle::Debug.foreign_boxed_value(3.14).class.should == Polyglot::ForeignNumber
    Truffle::Debug.foreign_pointer(0x1234).class.should == Polyglot::ForeignPointer
    Truffle::Debug.foreign_string("foo").class.should == Polyglot::ForeignString

    Truffle::Debug.foreign_pointer_array.class.should == Polyglot::ForeignArrayPointer
  end

  it "gives a Ruby Class to Ruby objects behind a foreign proxy" do
    Truffle::Interop.proxy_foreign_object({}).class.should == Polyglot::ForeignHashIterable
    Truffle::Interop.proxy_foreign_object([1, 2, 3]).class.should == Polyglot::ForeignArray
    Truffle::Interop.proxy_foreign_object(Exception.new("")).class.should == Polyglot::ForeignExceptionClass
    Truffle::Interop.proxy_foreign_object(-> { nil }).class.should == Polyglot::ForeignExecutable
    Truffle::Interop.proxy_foreign_object(String).class.should == Polyglot::ForeignInstantiable
    Truffle::Interop.proxy_foreign_object((1..3)).class.should == Polyglot::ForeignIterable
    Truffle::Interop.proxy_foreign_object((1..3).each).class.should == Polyglot::ForeignIterableIterator
    Truffle::Interop.proxy_foreign_object(nil).class.should == Polyglot::ForeignNull
    Truffle::Interop.proxy_foreign_object(42).class.should == Polyglot::ForeignNumber
    Truffle::Interop.proxy_foreign_object(3.14).class.should == Polyglot::ForeignNumber
    Truffle::Interop.proxy_foreign_object(Truffle::FFI::Pointer::NULL).class.should == Polyglot::ForeignPointer
    Truffle::Interop.proxy_foreign_object("foo").class.should == Polyglot::ForeignString
  end

  guard -> { !TruffleRuby.native? } do
    it "gives a Ruby Class to host objects" do
      Java.type('java.lang.Object').new.class.should == Polyglot::ForeignObject

      Truffle::Interop.to_java_map({ a: 1 }).class.should == Polyglot::ForeignHash
      Truffle::Interop.to_java_array([1, 2, 3]).class.should == Polyglot::ForeignArray
      Truffle::Interop.to_java_list([1, 2, 3]).class.should == Polyglot::ForeignArray
      Java.type('java.lang.RuntimeException').new.class.should == Polyglot::ForeignExceptionClass
      Java.type('java.math.BigInteger').class.should == Polyglot::ForeignInstantiable
      Java.type('java.math.BigInteger')[:class].class.should == Polyglot::ForeignInstantiable
      # ForeignIterable
      Truffle::Interop.to_java_list([1, 2, 3]).iterator.class.should == Polyglot::ForeignIterator
      Truffle::Debug.java_null.class.should == Polyglot::ForeignNull
      # ForeignNumber
      # ForeignPointer
      Java.type('java.lang.String').new("foo").class.should == String # Should be Polyglot::ForeignString
    end
  end
end
