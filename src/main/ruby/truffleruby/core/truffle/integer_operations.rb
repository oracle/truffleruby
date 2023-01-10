# frozen_string_literal: true

# Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module IntegerOperations
    def self.bits_reference_range(n, range)
      raise FloatDomainError , 'Infinity' if range.begin == Float::INFINITY || range.end == Float::INFINITY
      raise FloatDomainError , '-Infinity' if range.begin == -Float::INFINITY || range.end == -Float::INFINITY

      if !Primitive.nil?(range.begin) && !Primitive.nil?(range.end)
        len = range.end - range.begin
        len += 1 unless range.exclude_end?
        num = n >> range.begin
        mask = (1 << len) - 1

        range.end < range.begin ? num : num & mask
      elsif Primitive.nil?(range.end)
        n >> range.begin
      else
        len = range.end
        len += 1 unless range.exclude_end?
        mask = (1 << len) - 1

        if n & mask != 0 && range.end >= 0
          raise ArgumentError, 'The beginless range for Integer#[] results in infinity'
        end

        0
      end
    end
  end
end
