# frozen_string_literal: true

# Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module ObjectSpace
  # WeakMap uses Ruby identity semantics to compare and hash keys.
  class WeakMap
    include Enumerable

    def []=(key, value)
      Primitive.weakmap_aset(self, key, value)
    end

    def inspect
      str = "#<ObjectSpace::WeakMap:0x#{Primitive.kernel_to_hex(object_id)}"
      entries = self.entries
      str += ': ' if entries.length > 0
      entries.each do |k, v|
        str += ', ' unless str[-2] == ':'
        str += "#{Primitive.rb_any_to_s(k)} => #{Primitive.rb_any_to_s(v)}"
      end
      str + '>'
    end
  end
end
