# frozen_string_literal: true

# Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
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
    def self.arithmetic_range?(from, to)
      if Primitive.object_kind_of?(from, Numeric)
        Primitive.object_kind_of?(to, Numeric) || Primitive.nil?(to)
      else
        Primitive.nil?(from) && Primitive.object_kind_of?(to, Numeric)
      end
    end

    def self.step_iterations_size(range, first, last, step_size)
      case first
      when Float
        Truffle::NumericOperations.float_step_size(first, last, step_size, range.exclude_end?)
      else
        Primitive.nil?(range.size) ? nil : (range.size.fdiv(step_size)).ceil
      end
    end

    def self.validate_step_size(first, last, step_size)
      if Primitive.object_kind_of?(step_size, Float) or Primitive.object_kind_of?(first, Float) or Primitive.object_kind_of?(last, Float)
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

        unless Primitive.object_kind_of?(step_size, Integer)
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
      b1 = range.begin
      b2 = other.begin
      e1 = range.end
      e2 = other.end

      return false if Primitive.nil?(b2) && !Primitive.nil?(b1)
      return false if Primitive.nil?(e2) && !Primitive.nil?(e1)

      return false unless (Primitive.nil?(b2) || cover?(range, b2))

      return true if Primitive.nil?(e2)

      if e1 == e2
        !(range.exclude_end? && !other.exclude_end?)
      else
        if Integer === e2 && other.exclude_end?
          cover?(range, e2 - 1)
        else
          cover?(range, e2)
        end
      end
    end

    # # MRI: r_cover_p
    def self.cover?(range, value)
      # Check lower bound.
      if !Primitive.nil?(range.begin)
        # MRI uses <=> to compare, so must we.
        cmp = (range.begin <=> value)
        return false unless cmp
        return false if Comparable.compare_int(cmp) > 0
      end

      # Check upper bound.
      if !Primitive.nil?(range.end)
        cmp = (value <=> range.end)
        if range.exclude_end?
          return false if Comparable.compare_int(cmp) >= 0
        else
          return false if Comparable.compare_int(cmp) > 0
        end
      end

      true
    end
  end
end
