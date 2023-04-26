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

    def self.compose(time_class, utc_offset, p1, p2 = nil, p3 = nil, p4 = nil, p5 = nil, p6 = nil, p7 = nil,
                yday = undefined, is_dst = undefined, tz = undefined)
      if Primitive.undefined?(tz)
        unless Primitive.undefined?(is_dst)
          raise ArgumentError, 'wrong number of arguments (9 for 1..8)'
        end

        year = p1
        month = p2
        mday = p3
        hour = p4
        min = p5
        sec = p6
        usec = p7
        is_dst = -1
      else
        year = p6
        month = p5
        mday = p4
        hour = p3
        min = p2
        sec = p1
        usec = 0
        is_dst = is_dst ? 1 : 0
      end

      if Primitive.is_a?(month, String) or month.respond_to?(:to_str)
        month = StringValue(month)
        month = MonthValue[month.upcase] || month.to_i

        raise ArgumentError, 'month argument out of range' unless month
      else
        month = Truffle::Type.coerce_to(month || 1, Integer, :to_int)
      end

      year = Primitive.is_a?(year, String)  ? year.to_i : Truffle::Type.coerce_to(year,       Integer, :to_int)
      mday = Primitive.is_a?(mday, String)  ? mday.to_i : Truffle::Type.coerce_to(mday  || 1, Integer, :to_int)
      hour = Primitive.is_a?(hour, String)  ? hour.to_i : Truffle::Type.coerce_to(hour  || 0, Integer, :to_int)
      min  = Primitive.is_a?(min,  String)  ? min.to_i  : Truffle::Type.coerce_to(min   || 0, Integer, :to_int)

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

      # Ensure all the user provided numeric values fit into the Java Integer type.
      # sec and nsec are handled separately.
      Primitive.rb_num2int(min)
      Primitive.rb_num2int(hour)
      Primitive.rb_num2int(mday)
      Primitive.rb_num2int(month)
      Primitive.rb_num2int(year)

      # handle sec and nsec
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
