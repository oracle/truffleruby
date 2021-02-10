# frozen_string_literal: true

# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module RandomOperations

    # MRI: rand_random
    def self.random(randomizer, limit)
      if Primitive.undefined?(limit)
        return random_real(randomizer, true)
      end

      return nil if Primitive.nil?(limit)

      unless Primitive.object_kind_of?(limit, Float)
        if limit_int = Truffle::Type.rb_check_to_integer(limit, :to_int)
          return rand_int(randomizer, limit_int, true)
        end
      end

      limit_float = Truffle::Type.rb_check_to_float(limit)
      if !Primitive.nil?(limit_float)
        if limit_float < 0.0
          nil
        else
          check_float(limit_float)
          r = random_real(randomizer, true)
          r *= limit_float if limit_float > 0.0
          r
        end
      elsif Primitive.object_kind_of?(limit, Range)
        rand_range(randomizer, limit)
      else
        false
      end
    end

    def self.check_float(value)
      raise Errno::EDOM if value.infinite? || value.nan?
      value
    end

    def self.check_random_number(random, limit)
      case random
      when false
        Primitive.rb_num2long(limit)
      when nil
        invalid_argument(limit)
      end
      random
    end

    def self.invalid_argument(limit)
      raise ArgumentError, "invalid argument - #{limit}"
    end

    def self.random_real(randomizer, exclusive)
      if exclusive
        randomizer.random_float
      else
        randomizer.random_float * 1.0000000000000002
      end
    end

    def self.rand_int(randomizer, limit, restricted)
      return nil if limit == 0
      if limit < 0
        return nil if restricted
        limit = -limit
      end
      randomizer.random_integer(limit - 1)
    end

    def self.rand_range(randomizer, range)
      b, e, exclude_end = range.begin, range.end, range.exclude_end?
      raise Errno::EDOM if Primitive.nil?(b) || Primitive.nil?(e)

      diff = e - b
      if !Primitive.object_kind_of?(diff, Float) &&
        !Primitive.nil?(v = Truffle::Type.rb_check_to_integer(diff, :to_int))
        max = exclude_end ? v - 1 : v
        v = nil
        v = randomizer.random_integer(max) if max >= 0
      elsif !Primitive.nil?(v = Truffle::Type.rb_check_to_float(diff))
        scale = 1
        max = v
        mid = 0.5
        r = 0.0
        if v.infinite?
          min = check_float(Truffle::Type.rb_to_f(b)) / 2.0
          max = check_float(Truffle::Type.rb_to_f(e)) / 2.0
          scale = 2
          mid = max + min
          max -= min
        elsif v.nan?
          raise Errno::EDOM
        end

        if max > 0.0
          if scale > 1
            r = random_real(randomizer, exclude_end)
            return +(+(+(r - 0.5) * max) * scale) + mid
          end
          v = r * max
        elsif max == 0.0 && !exclude_end
          v = 0.0
        else
          v = nil
        end
      end

      if Primitive.object_kind_of?(b, Integer) && Primitive.object_kind_of?(v, Integer)
        return b + v
      end

      case v
      when NilClass
        v
      when Float
        f = Truffle::Type.rb_check_to_float(b)
        return v + f unless Primitive.nil?(f)
        b + v
      else
        b + v
      end
    end

    def self.obj_random_int(randomizer)
      obj_random_bytes(randomizer.value, 4).unpack1('l')
    end

    def self.obj_random_bytes(obj, len)
      bytes = obj.bytes(len)
      raise TypeError, 'type must by String' unless Primitive.object_kind_of?(bytes, String)
      strlen = bytes.bytesize
      raise RangeError, "random data too short #{strlen}" if strlen < len
      raise RangeError, "random data too long #{strlen}" if strlen > len
      bytes
    end
  end
end
