# Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../../ruby/optional/capi/spec_helper'

load_extension('handle_conversion')

guard -> { Truffle::Boot.get_option('cexts-to-native-count') == true } do
  describe "Native handle conversion" do
    it "converts no handles when comparing a VALUE with a constant" do
      CAPIHandleConversionTest.new.value_comparison_with_nil(Object.new)
    end

    it "converts no handles when accessing array elements via an RARRAY_PTR" do
      ary = Array.new(1000) { Object.new }
      CAPIHandleConversionTest.new.value_array_ptr_access([ary])
    end

    it "converts all elements to native handles when memcpying an RARRAY_PTR" do
      ary = Array.new(1000) { Object.new }
      CAPIHandleConversionTest.new.value_array_ptr_memcpy(ary)
    end

    it "converts no handles when storing a VALUE in a static variable" do
      CAPIHandleConversionTest.new.value_store_in_static(Object.new)
    end
  end
end
