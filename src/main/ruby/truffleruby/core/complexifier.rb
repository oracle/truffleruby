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

    PATTERN0 = /\A#{SPACE}(#{NUMBER})@(#{NUMBER})#{SPACE}/
    PATTERN1 = /\A#{SPACE}([-+])?(#{NUMBER})?[iIjJ]#{SPACE}/
    PATTERN2 = /\A#{SPACE}(#{NUMBER})(([-+])(#{NUMBERNOS})?[iIjJ])?#{SPACE}/

    PATTERN_STRICT0 = /\A#{SPACE}(#{NUMBER})@(#{NUMBER})#{SPACE}\z/
    PATTERN_STRICT1 = /\A#{SPACE}([-+])?(#{NUMBER})?[iIjJ]#{SPACE}\z/
    PATTERN_STRICT2 = /\A#{SPACE}(#{NUMBER})(([-+])(#{NUMBERNOS})?[iIjJ])?#{SPACE}\z/

    def initialize(value)
      @value = value
    end

    def convert
      unless @value.encoding.ascii_compatible?
        raise Encoding::CompatibilityError, "ASCII incompatible encoding: #{@value.encoding.name}"
      end

      # Moved this parsing logic out of #convert_internal to avoid a called splitting and improve performance
      if m = PATTERN0.match(@value)
        sr = m[1]
        si = m[2]
        po = true
      elsif m = PATTERN1.match(@value)
        sr = nil
        si = (m[1] || '') + (m[2] || '1')
        po = false
      elsif m = PATTERN2.match(@value)
        sr = m[1]
        si = m[2] ? m[3] + (m[4] || '1') : nil
        po = false
      else
        return Complex.new(0, 0)
      end

      convert_internal(sr, si, po)
    end

    def strict_convert(exception)
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

      # Moved this parsing logic out of #convert_internal to avoid a called splitting and improve performance
      if m = PATTERN_STRICT0.match(@value)
        sr = m[1]
        si = m[2]
        po = true
      elsif m = PATTERN_STRICT1.match(@value)
        sr = nil
        si = (m[1] || '') + (m[2] || '1')
        po = false
      elsif m = PATTERN_STRICT2.match(@value)
        sr = m[1]
        si = m[2] ? m[3] + (m[4] || '1') : nil
        po = false
      else
        if exception
          raise ArgumentError, "invalid value for convert(): #{@value.inspect}"
        else
          return nil
        end
      end

      convert_internal(sr, si, po)
    end

    private

    def convert_internal(real_string, imaginary_string, is_polar)
      r = 0
      i = 0

      if real_string
        if real_string.include?('/')
          r = real_string.to_r
        elsif real_string.match(/[.eE]/)
          r = real_string.to_f
        else
          r = real_string.to_i
        end
      end

      if imaginary_string
        if imaginary_string.include?('/')
          i = imaginary_string.to_r
        elsif imaginary_string.match(/[.eE]/)
          i = imaginary_string.to_f
        else
          i = imaginary_string.to_i
        end
      end

      if is_polar
        Complex.polar(r, i)
      else
        Complex.rect(r, i)
      end
    end
  end
end
