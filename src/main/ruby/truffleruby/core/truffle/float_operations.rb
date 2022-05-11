# frozen_string_literal: true

# Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module FloatOperations
    def self.round_to_n_place(f, ndigits, half)
      exp = Primitive.float_exp(f)
      # A double can have a maximum of 17 significant decimal
      # digits. If the number is large enough that rounding precision
      # would require more digits then we should simply return the
      # existing number. Also if the precision is small enough that
      # rounded to zero then just return 0.0.
      if ndigits > 17 || (exp >= 0 && ndigits >= (Float::DIG + 2) - exp / 4)
        f
      elsif exp < 0 && ndigits < -(exp / 4)
        0.0
      else
        case half
        when nil, :up
          Primitive.float_round_up_decimal(f, ndigits)
        when :even
          Primitive.float_round_even_decimal(f, ndigits)
        when :down
          Primitive.float_round_down_decimal(f, ndigits)
        else
          raise ArgumentError, "invalid rounding mode: #{half}"
        end
      end
    end
  end
end
