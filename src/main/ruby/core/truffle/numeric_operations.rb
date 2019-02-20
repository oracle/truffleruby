# frozen_string_literal: true

# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
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
  module NumericOperations

    def self.float_step_size(value, limit, step, exclude_end)
      if step.infinite?
        return step > 0 ? (value <= limit ? 1 : 0) : (value >= limit ? 1 : 0)
      end

      if step == 0
        return Float::INFINITY
      end

      n = (limit - value) / step
      return 0 if n < 0

      err = (value.abs + limit.abs + (limit - value).abs) / step.abs * Float::EPSILON
      err = 0.5 if err > 0.5

      if exclude_end
        return 0 if n <= 0

        if n < 1
          n = 0
        else
          n = n.finite? && err.finite? ? (n - err).floor : n
        end
      else
        return 0 if n < 0

        n = n.finite? && err.finite? ? (n + err).floor : n
      end

      n + 1
    end

    def self.step_size(value, limit, step, uses_kwargs)
      values = step_fetch_args(value, limit, step, uses_kwargs)
      value = values[0]
      limit = values[1]
      step = values[2]
      desc = values[3]
      is_float = values[4]

      if stepping_forever?(limit, step, desc)
        Float::INFINITY
      elsif is_float
        float_step_size(value, limit, step, false)
      else
        if (desc && value < limit) || (!desc && value > limit)
          0
        else
          ((value - limit).abs + 1).fdiv(step.abs).ceil
        end
      end
    end

    def self.stepping_forever?(limit, step, desc)
      return true if limit.nil? || step.zero?
      if desc
        limit == -Float::INFINITY && step != -Float::INFINITY
      else
        limit == Float::INFINITY  && step != Float::INFINITY
      end
    end

    def self.step_fetch_args(value, limit, step, uses_kwargs)
      if uses_kwargs
        step ||= 1
      else
        # Step can't be `nil` or `0` if passed via positional arguments,
        # but it can be either value if passed via keyword arguments.

        raise TypeError, 'step must be numeric' if step.nil?
        raise ArgumentError, "step can't be 0" if step == 0
      end

      step = Truffle::Type.coerce_to_int(step) unless Numeric === step
      desc = step < 0
      default_limit = desc ? -Float::INFINITY : Float::INFINITY

      if value.kind_of? Float or limit.kind_of? Float or step.kind_of? Float
        [Truffle::Type.rb_num2dbl(value), Truffle::Type.rb_num2dbl(limit || default_limit),
         Truffle::Type.rb_num2dbl(step), desc, true]
      else
        [value, limit || default_limit, step, desc, false]
      end
    end

  end
end
