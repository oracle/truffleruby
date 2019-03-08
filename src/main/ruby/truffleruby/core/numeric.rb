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

class Numeric
  include Comparable

  # Always raises TypeError, as dup'ing Numerics is not allowed.
  def initialize_copy(other)
    raise TypeError, "copy of #{self.class} is not allowed"
  end

  def +@
    self
  end

  def -@
    0 - self
  end

  def divmod(other)
    [div(other), self % other]
  end

  def eql?(other)
    return false unless other.class == self.class
    self == other
  end

  def <=>(other)
    # It's important this method NOT contain the coercion protocols!
    # MRI doesn't and doing so breaks stuff!

    return 0 if self.equal? other
    nil
  end

  def step(orig_limit = undefined, orig_step = undefined, by: undefined, to: undefined)
    limit = if !undefined.equal?(orig_limit) && !undefined.equal?(to)
              raise ArgumentError, 'to is given twice'
            elsif !undefined.equal?(orig_limit)
              orig_limit
            elsif !undefined.equal?(to)
              to
            else
              nil
            end
    step = if !undefined.equal?(orig_step) && !undefined.equal?(by)
             raise ArgumentError, 'step is given twice'
           elsif !undefined.equal?(orig_step)
             orig_step
           elsif !undefined.equal?(by)
             by
           else
             1
           end

    kwargs = {}
    kwargs[:by] = by unless undefined.equal?(by)
    kwargs[:to] = to unless undefined.equal?(to)

    unless block_given?
      return to_enum(:step, orig_limit, orig_step, kwargs) do
        Truffle::NumericOperations.step_size(self, limit, step, kwargs.any?)
      end
    end

    values = Truffle::NumericOperations.step_fetch_args(self, limit, step, kwargs.any?)

    value = values[0]
    limit = values[1]
    step = values[2]
    desc = values[3]
    is_float = values[4]

    infinite = step == 0

    if is_float
      n = Truffle::NumericOperations.float_step_size(value, limit, step, false)

      if n > 0
        if step.infinite?
          yield value
        elsif infinite
          loop do
            yield value
          end
        else
          i = 0
          if desc
            while i < n
              d = i * step + value
              d = limit if limit > d
              yield d
              i += 1
            end
          else
            while i < n
              d = i * step + value
              d = limit if limit < d
              yield d
              i += 1
            end
          end
        end
      end
    else
      if infinite
        loop do
          yield value
          value += step
        end
      else
        if desc
          until value < limit
            yield value
            value += step
          end
        else
          until value > limit
            yield value
            value += step
          end
        end
      end
    end

    self
  end

  def truncate
    Float(self).truncate
  end

  # Delegate #to_int to #to_i in subclasses
  def to_int
    to_i
  end

  def integer?
    false
  end

  def zero?
    self == 0
  end

  def nonzero?
    zero? ? nil : self
  end

  def positive?
    self > 0
  end

  def negative?
    self < 0
  end

  def finite?
    true
  end

  def infinite?
    nil
  end

  def round
    to_f.round
  end

  def abs
    self < 0 ? -self : self
  end

  def floor
    Truffle::Type.rb_num2dbl(self).floor
  end

  def ceil
    Truffle::Type.rb_num2dbl(self).ceil
  end

  def remainder(other)
    mod = self % other

    if mod != 0 and ((self < 0 and other > 0) or (self > 0 and other < 0))
      mod - other
    else
      mod
    end
  end

  def coerce(other)
    other = Truffle::Interop.unbox_if_needed(other)

    if other.instance_of? self.class
      return [other, self]
    end

    [Float(other), Float(self)]
  end

  ##
  # This method mimics the semantics of MRI's do_coerce function
  # in numeric.c. Note these differences between it and #coerce:
  #
  #   1.2.coerce("2") => [2.0, 1.2]
  #   1.2 + "2" => TypeError: String can't be coerced into Float
  #
  # See also Integer#coerce

  def math_coerce(other, error=:coerce_error)
    other = Truffle::Interop.unbox_if_needed(other)
    return math_coerce_error(other, error) unless other.respond_to? :coerce

    values = other.coerce(self)

    unless Truffle::Type.object_kind_of?(values, Array) && values.length == 2
      return nil
    end

    [values[1], values[0]]
  end
  private :math_coerce

  def math_coerce_error(other, error)
    if error == :coerce_error
      raise TypeError, "#{other.class} can't be coerced into #{self.class}"
    elsif error == :compare_error
      raise ArgumentError, "comparison of #{self.class} with #{other.class} failed"
    elsif error == :no_error
      nil
    end
  end
  private :math_coerce_error

  def bit_coerce(other)
    values = math_coerce(other)
    unless values[0].is_a?(Integer) && values[1].is_a?(Integer)
      raise TypeError, "#{values[1].class} can't be coerced into #{self.class}"
    end
    values
  end
  private :bit_coerce

  def redo_coerced(meth, right, *extra)
    b, a = math_coerce(right)
    a.__send__ meth, b, *extra
  end
  private :redo_coerced

  def redo_compare(meth, right)
    b, a = math_coerce(right, :compare_error)
    return nil unless b
    a.__send__ meth, b
  end
  private :redo_compare

  private def redo_compare_no_error(right)
    b, a = math_coerce(right, :no_error)
    return nil unless b
    a <=> b
  end

  def redo_bit_coerced(meth, right)
    b, a = bit_coerce(right)
    a.__send__ meth, b
  end
  private :redo_bit_coerced

  def div(other)
    raise ZeroDivisionError, 'divided by 0' if other == 0
    (self / other).floor
  end

  def fdiv(other)
    self.to_f / other
  end

  def quo(other)
    Truffle.privately do
      Rational.convert(self, 1) / other
    end
  end

  def modulo(other)
    self - other * self.div(other)
  end
  alias_method :%, :modulo

  def i
    Complex(0, self)
  end

  def to_c
    Complex(self, 0)
  end

  def real
    self
  end

  def imag
    0
  end
  alias_method :imaginary, :imag

  def arg
    if self < 0
      Math::PI
    else
      0
    end
  end
  alias_method :angle, :arg
  alias_method :phase, :arg

  def polar
    [abs, arg]
  end

  def conjugate
    self
  end
  alias_method :conj, :conjugate

  def rect
    [self, 0]
  end
  alias_method :rectangular, :rect

  def abs2
    self * self
  end

  alias_method :magnitude, :abs

  def numerator
    to_r.numerator
  end

  def denominator
    to_r.denominator
  end

  def real?
    true
  end

  def singleton_method_added(name)
    self.singleton_class.send(:remove_method, name)
    raise TypeError, "can't define singleton method #{name} for #{self.class}"
  end

end
