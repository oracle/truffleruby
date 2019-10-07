# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Symbol" do
  describe "#all_symbols" do
    it "contains method name for alive method" do
      object = Object.new
      class << object
        def long_name_unlikely_to_be_alive_elsewhere
        end
      end
      GC.start

      Symbol.all_symbols.inspect.should include("long_name_unlikely_to_be_alive_elsewhere")
    end
  end
end