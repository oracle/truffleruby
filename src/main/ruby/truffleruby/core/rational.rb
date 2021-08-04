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

#
#   rational.rb -
#       $Release Version: 0.5 $
#       $Revision: 1.7 $
#       $Date: 1999/08/24 12:49:28 $
#       by Keiju ISHITSUKA(SHL Japan Inc.)
#

class Rational < Numeric
  attr_reader :numerator
  attr_reader :denominator

  class << self
    alias_method :new_already_canonical, :new
    undef_method :new
  end

  def *(other)
    case other
    when Rational
      num = @numerator * other.numerator
      den = @denominator * other.denominator
      Rational(num, den)
    when Integer
      Rational(@numerator * other, @denominator)
    when Float
      to_f * other
    else
      redo_coerced :*, other
    end
  end

  def **(other)
    if Primitive.object_kind_of?(other, Rational) && other.denominator == 1
      other = other.numerator
    end

    case other
    when Integer
      if Truffle::Type.fits_into_long?(other)
        if other > 0
          Rational(@numerator ** other, @denominator ** other)
        elsif other < 0
          raise ZeroDivisionError, 'divided by 0' if self == 0
          Rational(@denominator ** -other, @numerator ** -other)
        elsif other == 0
          Rational.new_already_canonical(1, 1)
        end
      else
        if self == 0
          if other < 0
            raise ZeroDivisionError, 'divided by 0'
          elsif other > 0
            Rational.new_already_canonical(0, 1)
          end
        elsif self == 1
          Rational.new_already_canonical(1, 1)
        elsif self == -1
          Rational.new_already_canonical(other.even? ? 1 : -1, 1)
        else
          to_f ** other
        end
      end
    when Float
      to_f ** other
    when Rational
      if self == 0 && other < 0
        raise ZeroDivisionError, 'divided by 0'
      end

      to_f ** other
    else
      redo_coerced :**, other
    end
  end

  def +(other)
    case other
    when Rational
      num = @numerator * other.denominator + @denominator * other.numerator
      den = @denominator * other.denominator
      Rational(num, den)
    when Integer
      Rational(@numerator + other * @denominator, @denominator)
    when Float
      to_f + other
    else
      redo_coerced :+, other
    end
  end

  def -(other)
    case other
    when Rational
      num = @numerator * other.denominator - @denominator * other.numerator
      den = @denominator * other.denominator
      Rational(num, den)
    when Integer
      Rational(@numerator - other * @denominator, @denominator)
    when Float
      to_f - other
    else
      redo_coerced :-, other
    end
  end

  def /(other)
    case other
    when Rational
      num = @numerator * other.denominator
      den = @denominator * other.numerator
      Rational(num, den)
    when Integer
      raise ZeroDivisionError, 'divided by 0' if other == 0
      Rational(@numerator, @denominator * other)
    when Float
      to_f / other
    else
      redo_coerced :/, other
    end
  end
  alias_method :divide, :/
  alias_method :quo, :/

  def <=>(other)
    case other
    when Rational
      diff = @numerator * other.denominator - @denominator * other.numerator
      diff <=> 0
    when Integer
      diff = @numerator - @denominator * other
      diff <=> 0
    when Float
      to_f <=> other
    else
      redo_compare_no_error(other)
    end
  end

  def ==(other)
    case other
    when Rational
      @numerator == other.numerator && @denominator == other.denominator
    when Integer
      @numerator == other && @denominator == 1
    when Float
      to_f == other
    else
      other == self
    end
  end

  def abs
    return self if @numerator >= 0

    Rational.new_already_canonical(-@numerator, @denominator)
  end

  def ceil(precision = 0)
    if precision == 0
      -(-@numerator / @denominator)
    else
      with_precision(:ceil, precision)
    end
  end

  def coerce(other)
    case other
    when Integer
      [Rational.new_already_canonical(other, 1), self]
    when Float
      [other, self.to_f]
    when Rational
      [other, self]
    when Complex
      if other.imag.zero?
        [Rational(other.real), self]
      else
        [other, Complex(self)]
      end
    else
      raise TypeError, "#{other.class.name} can't be coerced into Rational"
    end
  end

  def floor(precision = 0)
    if precision == 0
      @numerator / @denominator
    else
      with_precision(:floor, precision)
    end
  end

  # Random number for hash codes. Stops hashes for similar values in
  # different classes from clashing, but defined as a constant so
  # that hashes will be deterministic.

  CLASS_SALT = 0x3c654ce9

  private_constant :CLASS_SALT

  def hash
    val = Primitive.vm_hash_start CLASS_SALT
    val = Primitive.vm_hash_update val, @numerator.hash
    val = Primitive.vm_hash_update val, @denominator.hash
    Primitive.vm_hash_end val
  end

  def inspect
    "(#{self})"
  end

  def rationalize(eps = undefined)
    return self if Primitive.undefined?(eps)

    e = eps.abs
    a = self - e
    b = self + e

    p0 = 0
    p1 = 1
    q0 = 1
    q1 = 0

    c = a.ceil
    until c < b
      k = c - 1
      p2 = k * p1 + p0
      q2 = k * q1 + q0
      t = 1 / (b - k)
      b = 1 / (a - k)
      a = t

      p0 = p1
      q0 = q1
      p1 = p2
      q1 = q2

      c = a.ceil
    end

    # The rational number is guaranteed to be in lowest terms.
    Rational.new_already_canonical(c * p1 + p0, c * q1 + q0)
  end

  def round(precision = 0, half: nil)
    return with_precision(:round, precision, half: half) unless precision == 0
    return 0 if @numerator == 0
    return @numerator if @denominator == 1

    # Rounding logic copied from MRI

    num = @numerator.abs * 2 + @denominator
    den = @denominator * 2

    case half
    when nil, :up
      rounded = num / den
    when :even
      rounded, remainder = num.divmod(den)
      rounded = rounded & ~1 if remainder.zero?
    when :down
      rounded = (num - 1) / den
    else
      raise ArgumentError, "invalid rounding mode: #{half}"
    end

    if @numerator < 0
      -rounded
    else
      rounded
    end
  end

  def to_f
    @numerator.to_f / @denominator.to_f
  end

  def to_i
    truncate
  end

  def to_r
    self
  end

  def to_s
    "#{@numerator}/#{@denominator}"
  end

  def truncate(precision = 0)
    if precision == 0
      @numerator < 0 ? ceil : floor
    else
      with_precision(:truncate, precision)
    end
  end

  def self.convert(num, den)
    if num.equal?(nil) || den.equal?(nil)
      raise TypeError, 'cannot convert nil into Rational'
    end

    if Integer === num && Integer === den
      return reduce(num, den)
    end

    case num
    when Integer
      # nothing
    when Float, String, Complex
      num = num.to_r
    end

    case den
    when Integer
      # nothing
    when Float, String, Complex
      den = den.to_r
    end

    if den.equal?(1) && !(Integer === num)
      return Truffle::Type.coerce_to(num, Rational, :to_r)
    elsif Numeric === num && Numeric === den &&
        !(Integer === num && Integer === den)
      return num / den
    end

    reduce(num, den)
  end
  private_class_method :convert

  def self.reduce(num, den)
    case num
    when Integer
      # nothing
    when Numeric
      num = num.to_i
    else
      raise TypeError, 'numerator is not an Integer'
    end

    case den
    when Integer
      if den == 0
        raise ZeroDivisionError, 'divided by 0'
      elsif den < 0
        num = -num
        den = -den
      end

      if den == 1
        return new_already_canonical(num, den)
      end
    when Numeric
      den = den.to_i
    else
      raise TypeError, 'denominator is not an Integer'
    end

    gcd = num.gcd(den)
    num = num / gcd
    den = den / gcd

    new_already_canonical(num, den)
  end
  private_class_method :reduce

  def initialize(num, den)
    @numerator = num
    @denominator = den
    freeze
  end
  private :initialize

  def marshal_dump
    ary = [@numerator, @denominator]

    instance_variables.each do |ivar|
      ary.instance_variable_set(ivar, instance_variable_get(ivar))
    end

    ary
  end
  private :marshal_dump

  def marshal_load(ary)
    @numerator, @denominator = ary

    ary.instance_variables.each do |ivar|
      instance_variable_set(ivar, ary.instance_variable_get(ivar))
    end

    self
  end
  private :marshal_load

  def with_precision(method, n, **kwargs)
    raise TypeError, 'not an Integer' unless Primitive.object_kind_of?(n, Integer)

    p = 10 ** n
    s = self * p

    if kwargs.empty?
      wp = s.send(method)
    else
      wp = s.send(method, **kwargs)
    end

    r = Rational(wp, p)
    n < 1 ? r.to_i : r
  end
  private :with_precision
end
