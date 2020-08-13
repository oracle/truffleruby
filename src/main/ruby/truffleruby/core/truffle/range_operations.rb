# frozen_string_literal: true

# Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

module Truffle
  module RangeOperations

    def self.step_iterations_size(range, first, last, step_size)
      case first
      when Float
        Truffle::NumericOperations.float_step_size(first, last, step_size, range.exclude_end?)
      else
        Primitive.nil?(range.size) ? nil : (range.size.fdiv(step_size)).ceil
      end
    end

    def self.validate_step_size(first, last, step_size)
      if step_size.kind_of? Float or first.kind_of? Float or last.kind_of? Float
        # if any are floats they all must be
        begin
          step_size = Float(from = step_size)
          first     = Float(from = first)
          last      = Float(from = last) unless Primitive.nil? last
        rescue ArgumentError
          raise TypeError, "no implicit conversion to float from #{from.class}"
        end
      else
        step_size = Integer(from = step_size)

        unless step_size.kind_of? Integer
          raise TypeError, "can't convert #{from.class} to Integer (#{from.class}#to_int gives #{step_size.class})"
        end
      end

      if step_size <= 0
        raise ArgumentError, "step can't be negative" if step_size < 0
        raise ArgumentError, "step can't be 0"
      end

      [first, last, step_size]
    end

    # MRI: r_cover_range_p
    def self.range_cover?(range, other)
      e = range.end
      other_b = other.begin
      other_e = other.end

      return false if !Primitive.nil?(e) && Primitive.nil?(other_e)

      return false if !Primitive.nil?(other_e) &&
          range_less(other_b, other_e) > (other.exclude_end? ? -1 : 0)

      return false unless cover?(range, other_b)

      compare_end = range_less(e, other_e)

      if (range.exclude_end? && other.exclude_end?) ||
          (!range.exclude_end? && !other.exclude_end?)
        return compare_end >= 0
      elsif range.exclude_end?
        return compare_end > 0
      elsif compare_end >= 0
        return true
      end

      other_max = begin
                    other.max
                  rescue TypeError
                    nil
                  end
      return false if Primitive.nil?(other_max)

      range_less(e, other_max) >= 0
    end

    # MRI: r_cover_p
    def self.cover?(range, value)
      # MRI uses <=> to compare, so must we.
      beg_compare = (range.begin <=> value)
      return false unless beg_compare

      if Comparable.compare_int(beg_compare) <= 0
        return true if endless?(range)
        end_compare = (value <=> range.end)

        if range.exclude_end?
          return true if Comparable.compare_int(end_compare) < 0
        else
          return true if Comparable.compare_int(end_compare) <= 0
        end
      end

      false
    end

    def self.endless?(range)
      Primitive.nil? range.end
    end

    # MRI: r_less
    def self.range_less(a, b)
      compare = a <=> b
      if Primitive.nil?(compare)
        1
      else
        Comparable.compare_int(compare)
      end
    end
  end
end
