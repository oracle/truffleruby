# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../ruby/spec_helper'

# Require something that uses code from RubySL
require 'stringio'

describe "Identifying features such as" do

    describe "the Rubinius module" do

      it "is not defined" do
        defined?(Rubinius).should be_nil
      end
      
    end

    describe "the RubySL module" do

      it "is not defined" do
        defined?(RubySL).should be_nil
      end
      
    end

end
