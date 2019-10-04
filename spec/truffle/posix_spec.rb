# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Truffle::POSIX returns the correct value for an identity function returning" do
  before :all do
    src = fixture __FILE__, "libtestnfi.c"
    lib = src[0...-1] + RbConfig::CONFIG['SOEXT']
    unless system RbConfig::CONFIG['CC'], "-shared", "-o", lib, src
      abort "Could not compile libtestnfi"
    end

    lazy_library = Truffle::POSIX::LazyLibrary.new do
      TrufflePrimitive.interop_eval_nfi "load '#{lib}'"
    end

    @libtestnfi = Module.new do
      Truffle::POSIX.attach_function :max_ushort, [], :ushort, lazy_library, false, :max_ushort, self
      Truffle::POSIX.attach_function :max_uint, [], :uint, lazy_library, false, :max_uint, self
    end
  end

  it "the maximum unsigned short" do
    @libtestnfi.max_ushort.should == 65535
  end

  it "the maximum unsigned int" do
    @libtestnfi.max_uint.should == 4294967295
  end
end
