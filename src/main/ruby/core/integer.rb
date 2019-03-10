# frozen_string_literal: true

# Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
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

Fixnum = Bignum = Integer
Object.deprecate_constant :Fixnum, :Bignum

class Integer < Numeric

  alias_method :truncate, :to_i
  alias_method :ceil, :to_i
  alias_method :floor, :to_i

  # Have a copy in Integer of the Numeric version, as MRI does
  public :remainder

  def **(o)
    Truffle.primitive :integer_pow

    if o.is_a?(Float) && self < 0 && o != o.round
      return Complex.new(self, 0) ** o
    elsif o.is_a?(Integer) && o < 0
      return Rational.new(self, 1) ** o
    end

    redo_coerced :**, o
  end

  def [](index)
    index = Truffle::Type.coerce_to_int(index)
    index < 0 ? 0 : (self >> index) & 1
  end

  def coerce(other)
    if other.kind_of? Integer
      return [other, self]
    end

    [Float(other), Float(self)]
  end

  def divmod(b)
    Truffle.primitive :integer_divmod
    raise ZeroDivisionError if b == 0
    [
      (self / b).floor,
      self - b * (self / b).floor
    ]
  end

  def fdiv(n)
    if n.kind_of?(Integer)
      to_f / n
    else
      redo_coerced :fdiv, n
    end
  end

  def times
    return to_enum(:times) { self } unless block_given?

    i = 0
    while i < self
      yield i
      i += 1
    end
    self
  end

  def chr(enc=undefined)
    if self < 0 || (self & 0xffff_ffff) != self
      raise RangeError, "#{self} is outside of the valid character range"
    end

    if undefined.equal? enc
      if 0xff < self
        enc = Encoding.default_internal
        if enc.nil?
          raise RangeError, "#{self} is outside of the valid character range"
        end
      elsif self < 0x80
        enc = Encoding::US_ASCII
      else
        enc = Encoding::ASCII_8BIT
      end
    else
      enc = Truffle::Type.coerce_to_encoding enc
    end

    String.from_codepoint self, enc
  end

  def round(ndigits=undefined)
    return self if undefined.equal? ndigits

    if Float === ndigits && ndigits.infinite?
      raise RangeError, "float #{ndigits} out of range of integer"
    end

    ndigits = Truffle::Type.coerce_to_int(ndigits)
    Truffle::Type.check_int(ndigits)

    if ndigits > 0
      to_f
    elsif ndigits == 0
      self
    else
      ndigits = -ndigits

      # We want to return 0 if 10 ** ndigits / 2 > self.abs, or, taking
      # log_256 of both sides, if log_256(10 ** ndigits / 2) > self.size.
      # We have log_256(10) > 0.415241 and log_256(2) = 0.125, so:
      return 0 if 0.415241 * ndigits - 0.125 > size

      f = 10 ** ndigits

      if kind_of? Integer and f.kind_of? Integer
        x = self < 0 ? -self : self
        x = (x + f / 2) / f * f
        x = -x if self < 0
        return x
      end

      return 0 if f.kind_of? Float

      h = f / 2
      r = self % f
      n = self - r

      unless self < 0 ? r <= h : r < h
        n += f
      end

      n
    end
  end

  def gcd(other)
    raise TypeError, "Expected Integer but got #{other.class}" unless other.kind_of?(Integer)
    min = self.abs
    max = other.abs
    while min > 0
      tmp = min
      min = max % min
      max = tmp
    end
    max
  end

  def rationalize(eps = nil)
    Rational(self, 1)
  end

  def numerator
    self
  end

  def denominator
    1
  end

  def to_r
    Rational(self, 1)
  end

  def lcm(other)
    raise TypeError, "Expected Integer but got #{other.class}" unless other.kind_of?(Integer)
    if self.zero? or other.zero?
      0
    else
      (self.div(self.gcd(other)) * other).abs
    end
  end

  def gcdlcm(other)
    gcd = self.gcd(other)
    if self.zero? or other.zero?
      [gcd, 0]
    else
      [gcd, (self.div(gcd) * other).abs]
    end
  end

  def next
    self + 1
  end
  alias_method :succ, :next

  def integer?
    true
  end

  def even?
    self & 1 == 0
  end

  def odd?
    self & 1 == 1
  end

  def ord
    self
  end

  def pred
    self - 1
  end

  def digits(base = 10)
    raise Math::DomainError, 'out of domain' if negative?
    base = Truffle::Type.coerce_to_int(base)

    to_s(base).chars.map(&:to_i).reverse
  end

  def self.sqrt(n)
    n = Truffle::Type.coerce_to_int(n)
    raise Math::DomainError if n.negative?
    Math.sqrt(n).floor
  end

  private def upto_internal(val)
    return to_enum(:upto, val) { self <= val ? val - self + 1 : 0 } unless block_given?

    i = self
    while i <= val
      yield i
      i += 1
    end
    self
  end

  private def downto_internal(val)
    return to_enum(:downto, val) { self >= val ? self - val + 1 : 0 } unless block_given?

    i = self
    while i >= val
      yield i
      i -= 1
    end
    self
  end
end
