# frozen_string_literal: true

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

class String
  class Complexifier
    SPACE = Rationalizer::SPACE
    NUMERATOR = Rationalizer::NUMERATOR
    DENOMINATOR = Rationalizer::DENOMINATOR
    NUMBER = "[-+]?#{NUMERATOR}(?:\\/#{DENOMINATOR})?"
    NUMBERNOS = "#{NUMERATOR}(?:\\/#{DENOMINATOR})?"
    PATTERN0 = Regexp.new "\\A#{SPACE}(#{NUMBER})@(#{NUMBER})#{SPACE}"
    PATTERN1 = Regexp.new "\\A#{SPACE}([-+])?(#{NUMBER})?[iIjJ]#{SPACE}"
    PATTERN2 = Regexp.new "\\A#{SPACE}(#{NUMBER})(([-+])(#{NUMBERNOS})?[iIjJ])?#{SPACE}"

    PATTERN_STRICT0 = Regexp.new "\\A#{SPACE}(#{NUMBER})@(#{NUMBER})#{SPACE}\\Z"
    PATTERN_STRICT1 = Regexp.new "\\A#{SPACE}([-+])?(#{NUMBER})?[iIjJ]#{SPACE}\\Z"
    PATTERN_STRICT2 = Regexp.new "\\A#{SPACE}(#{NUMBER})(([-+])(#{NUMBERNOS})?[iIjJ])?#{SPACE}\\Z"

    def initialize(value)
      @value = value
    end

    def convert
      unless @value.encoding.ascii_compatible?
        raise Encoding::CompatibilityError, "ASCII incompatible encoding: #{@value.encoding.name}"
      end

      convert_internal(strict: false) || Complex.new(0, 0)
    end

    def strict_convert(exception:)
      unless @value.encoding.ascii_compatible?
        raise Encoding::CompatibilityError, "ASCII incompatible encoding: #{@value.encoding.name}"
      end

      if (@value.include?("\0".b))
        if exception
          raise ArgumentError, 'string contains null byte'
        else
          return nil
        end
      end

      complex_value = convert_internal(strict: true)

      if complex_value.nil? && exception
        raise ArgumentError, "invalid value for convert(): #{@value.inspect}"
      end

      complex_value
    end

    private

    # Return nil if String format is incorrect
    def convert_internal(strict:)
      pattern0 = strict ? PATTERN_STRICT0 : PATTERN0
      pattern1 = strict ? PATTERN_STRICT1 : PATTERN1
      pattern2 = strict ? PATTERN_STRICT2 : PATTERN2

      if m = pattern0.match(@value)
        sr = m[1]
        si = m[2]
        po = true
      elsif m = pattern1.match(@value)
        sr = nil
        si = (m[1] || '') + (m[2] || '1')
        po = false
      elsif m = pattern2.match(@value)
        sr = m[1]
        si = m[2] ? m[3] + (m[4] || '1') : nil
        po = false
      else
        return nil
      end

      r = 0
      i = 0

      if sr
        if sr.include?('/')
          r = sr.to_r
        elsif sr.match(/[.eE]/)
          r = sr.to_f
        else
          r = sr.to_i
        end
      end

      if si
        if si.include?('/')
          i = si.to_r
        elsif si.match(/[.eE]/)
          i = si.to_f
        else
          i = si.to_i
        end
      end

      if po
        Complex.polar(r, i)
      else
        Complex.rect(r, i)
      end
    end
  end
end
