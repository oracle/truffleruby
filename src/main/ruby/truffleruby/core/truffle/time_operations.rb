# frozen_string_literal: true

# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module TimeOperations
    MonthValue = {
      'JAN' => 1, 'FEB' => 2, 'MAR' => 3, 'APR' => 4, 'MAY' => 5, 'JUN' => 6,
      'JUL' => 7, 'AUG' => 8, 'SEP' => 9, 'OCT' =>10, 'NOV' =>11, 'DEC' =>12
    }

    # Handles Time methods .utc, .gm. .local, .mktime dual signature:
    # - year, month, day, hour, min, sec, usec
    # - sec, min, hour, day, month, year
    def self.compose(time_class, utc_offset, p1, p2 = nil, p3 = nil, p4 = nil, p5 = nil, p6 = nil, p7 = nil,
                yday = undefined, is_dst = undefined, tz = undefined)
      if Primitive.undefined?(tz)
        unless Primitive.undefined?(is_dst)
          raise ArgumentError, 'wrong number of arguments (9 for 1..8)'
        end

        year, month, mday, hour, min, sec, usec, is_dst = p1, p2, p3, p4, p5, p6, p7, -1
      else
        year, month, mday, hour, min, sec, usec, is_dst = p6, p5, p4, p3, p2, p1, 0, is_dst ? 1 : 0
      end

      if Primitive.is_a?(month, String) or month.respond_to?(:to_str)
        month = StringValue(month)
        month = MonthValue[month.upcase] || month.to_i

        raise ArgumentError, 'month argument out of range' unless month
      end

      year = year.to_i if Primitive.is_a?(year, String)
      mday = mday.to_i if Primitive.is_a?(mday, String)
      hour = hour.to_i if Primitive.is_a?(hour, String)
      min  = min.to_i  if Primitive.is_a?(min,  String)

      # Ensure all the user provided numeric values fit into the Java Integer type.
      # sec and nsec are handled separately.
      year  = Primitive.rb_num2int(year      )
      month = Primitive.rb_num2int(month || 1)
      mday  = Primitive.rb_num2int(mday  || 1)
      hour  = Primitive.rb_num2int(hour  || 0)
      min   = Primitive.rb_num2int(min   || 0)

      nsec = nil
      if Primitive.is_a?(usec, String)
        nsec = usec.to_i * 1000
      elsif usec
        nsec = (usec * 1000).to_i
      end

      case utc_offset
      when :utc
        is_dst = -1
        is_utc = true
        utc_offset = nil
      when :local
        is_utc = false
        utc_offset = nil
      else
        is_dst = -1
        is_utc = false
      end

      if Primitive.is_a?(sec, String)
        sec = sec.to_i
      elsif nsec
        sec = Truffle::Type.coerce_to(sec || 0, Integer, :to_int)
      else
        s = Truffle::Type.coerce_to_exact_num(sec || 0)

        sec       = s.to_i
        nsec_frac = s % 1.0

        if s < 0 && nsec_frac > 0
          sec -= 1
        end

        nsec = (nsec_frac * 1_000_000_000 + 0.5).to_i
      end

      nsec ||= 0

      Primitive.time_s_from_array(time_class, sec, min, hour, mday, month, year, nsec, is_dst, is_utc, utc_offset)
    end
  end
end
