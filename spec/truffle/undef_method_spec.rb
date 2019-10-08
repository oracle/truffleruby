# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative '../ruby/spec_helper'

describe "Module#undef_method" do
  describe "reports an error message as expected by minitest tests" do
    class Object

      def truffleruby_minitest_spec_stub
        metaclass = class << self; self; end
        metaclass.send :alias_method, :new_truffleruby_minitest_spec_stub_does_not_exist, :truffleruby_minitest_spec_stub_does_not_exist
      ensure
        metaclass.send :undef_method, :truffleruby_minitest_spec_stub_does_not_exist
      end

    end

    -> { Time.truffleruby_minitest_spec_stub }.should raise_error(NameError, /undefined method `truffleruby_minitest_spec_stub_does_not_exist' for class `Time'/)
  end
end
