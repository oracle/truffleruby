# frozen_string_literal: true

# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module QueueOperations
    def self.validate_and_prepare_timeout(non_block, timeout)
      if non_block
        raise ArgumentError, "can't set a timeout if non_block is enabled" unless Primitive.nil?(timeout)
      else
        unless Primitive.nil?(timeout)
          # Convert seconds to milliseconds.
          return (Truffle::Type.rb_num2dbl(timeout) * 1000).to_i
        end
      end

      timeout
    end
  end
end
