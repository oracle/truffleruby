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

    def self.step_float_size(value, limit, step, asc)
      if (asc && value > limit) || (!asc && value < limit)
        return 0
      end

      if step.infinite?
        1
      else
        err = (value.abs + limit.abs + (limit - value).abs) / step.abs * Float::EPSILON
        if err.finite?
          err = 0.5 if err > 0.5
          ((limit - value) / step + err).floor + 1
        else
          0
        end
      end
    end

    def self.step_size(value, limit, step, by)
      values = step_fetch_args(value, limit, step, by)
      value = values[0]
      limit = values[1]
      step = values[2]
      asc = values[3]
      is_float = values[4]

      if stepping_forever?(limit, step, asc)
        Float::INFINITY
      elsif is_float
        # Ported from MRI
        step_float_size(value, limit, step, asc)
      else
        if (asc && value > limit) || (!asc && value < limit)
          0
        else
          ((value - limit).abs + 1).fdiv(step.abs).ceil
        end
      end
    end

    def self.stepping_forever?(limit, step, asc)
      return true if limit.nil? || step.zero?
      if asc
        limit == Float::INFINITY  && step != Float::INFINITY
      else
        limit == -Float::INFINITY && step != -Float::INFINITY
      end
    end

    def self.step_fetch_args(value, limit, step, by)
      raise ArgumentError, +'step cannot be 0' if undefined.equal?(by) && step == 0

      asc = step > 0
      if value.kind_of? Float or limit.kind_of? Float or step.kind_of? Float
        [Truffle::Type.rb_num2dbl(value), Truffle::Type.rb_num2dbl(limit),
         Truffle::Type.rb_num2dbl(step), asc, true]
      else
        [value, limit, step, asc, false]
      end
    end
    
  end
end
