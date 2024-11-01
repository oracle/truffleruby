# frozen_string_literal: true

# Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved. This
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

    def self.new_from_string(time_class, str, **options)
      raise ArgumentError, 'time string should have ASCII compatible encoding' unless str.encoding.ascii_compatible?

      # Fast path for well-formed strings.
      # Specify the acceptable range for each component in the regexp so that if
      # any value is out of range the match will fail and fall through to the
      # scanner code below.
      if match = str.match(/\A(\d{4,5})(?:-(0[0-9]|1[012])-(0[0-9]|[12][0-9]|3[01])[ T]([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]|60)(?:\.(\d+))?\s*(\S+)?)?\z/)
        year, month, mday, hour, min, sec, usec, offset = match.captures
        return self.compose(time_class, self.utc_offset_for_compose(offset || options[:in]), year, month, mday, hour, min, sec, usec)
      end

      # raise ArgumentError, "can't parse: #{str.inspect}"

      len = str.length
      state = [1, 0]
      year = str.match(/^(\d+)/)&.captures&.first
      raise ArgumentError, "can't parse: #{str.inspect}" if Primitive.nil?(year)
      raise ArgumentError, "year must be 4 or more digits: #{year}" if year.length < 4

      return self.compose(time_class, self.utc_offset_for_compose(options[:in]), year) if len == year.size

      state[1] += year.size
      month = self.scan_for_two_digits(state, str, 'mon', '-', 1..12)
      mday = self.scan_for_two_digits(state, str, 'mday', '-', 1..31)

      # Just focus on the time part now.
      state[0] = state[1] + 1
      hour = self.scan_for_two_digits(state, str, 'hour', /[ T]/, 0..23, 'no time information', true)
      min = self.scan_for_two_digits(state, str, 'min', ':', 0..59, true, true)
      sec = self.scan_for_two_digits(state, str, 'sec', ':', 0..60, true)

      if match = str[state[1]].match(/\.(\d*)/)
        usec = match.captures[0]
        raise ArgumentError, "subsecond expected after dot: #{str[state[0]..state[1]]} " if usec == ''
      end

      utc_offset = options[:in]
      unless len == state[1]
        if match = str[state[1]..].match(/\s*(\S+)/)
          # An offset provided in the string overrides any passed in via `in:`.
          utc_offset = match.captures[0]
          state[1] += match[0].size
        end
      end

      raise ArgumentError, "can't parse at:#{str[state[1]..]}" if str.length > state[1]

      self.compose(time_class, self.utc_offset_for_compose(utc_offset), year, month, mday, hour, min, sec, usec)
    end

    def self.scan_for_two_digits(state, str, name, separator, range = nil, not_found_msg = nil, check_fraction = false)
      index = state[1]
      if str.length > index && str[index].match?(separator) && str[(index + 1)].match?(/\d/)
        subindex = index + 1
        while str.length > subindex && str[subindex].match?(/\d/)
          subindex += 1
        end
        digits = str[(index + 1)...subindex]
      elsif Primitive.true?(not_found_msg)
        raise ArgumentError, "missing #{name} part: #{str[state[0]..index]}"
      elsif not_found_msg
        raise ArgumentError, not_found_msg
      end

      if digits.size != 2
        after = " after '#{separator}'" if separator == ':' || separator == '-'
        raise ArgumentError, "two digits #{name} is expected#{after}: #{ str[index..(index + 10)] }"
      end

      # Advance index.
      index = state[1] = state[1] + 3

      num = digits.to_i
      if range && !range.include?(num)
        raise ArgumentError, "#{name} out of range"
      end

      if check_fraction && str[index] == '.'
        raise ArgumentError, "fraction #{name} is not supported: #{ str[state[0]..index] }"
      end

      num
    end

    def self.utc_offset_for_compose(utc_offset)
      if Primitive.nil?(utc_offset)
        :local
      elsif Time.send(:utc_offset_in_utc?, utc_offset)
        :utc
      else
        Truffle::Type.coerce_to_utc_offset(utc_offset)
      end
    end
  end
end
