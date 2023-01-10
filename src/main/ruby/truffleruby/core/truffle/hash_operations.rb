# frozen_string_literal: true

# Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module HashOperations
    def self.hash_merge(current, other)
      new_hash = current.dup
      other.each_pair do |k, v|
        new_hash[k] = v
      end
      new_hash
    end
  end
end
