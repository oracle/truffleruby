# frozen_string_literal: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
#
module Truffle::ArrayOperations
  # Helper to "recurse" through flattening. Detects recursive structures.
  # Does not actually recurse, but uses a worklist instead.
  def self.flatten_helper(array, out, max_levels = -1)
    modified = false
    visited = {}.compare_by_identity
    worklist = [[array, 0]]

    until worklist.empty?
      array, i = worklist.pop

      if i == 0
        raise ArgumentError, 'tried to flatten recursive array' if visited.key?(array)
        if max_levels == worklist.size
          out.concat(array)
          next
        end
        visited[array] = true
      end

      size = array.size
      while i < size
        o = array.at i
        tmp = Truffle::Type.rb_check_convert_type(o, Array, :to_ary)
        if Primitive.nil? tmp
          out << o
        else
          modified = true
          worklist.push([array, i + 1], [tmp, 0])
          break
        end
        i += 1
      end

      visited.delete array if i == size
    end

    modified
  end
end
