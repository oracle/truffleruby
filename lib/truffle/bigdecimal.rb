# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

BigDecimal = Truffle::BigDecimal

class BigDecimal < Numeric
  include Comparable

  BASE = 10_000

  SIGN_NEGATIVE_INFINITE = -3
  SIGN_NEGATIVE_FINITE   = -2
  SIGN_NEGATIVE_ZERO     = -1
  SIGN_NaN               = 0
  SIGN_POSITIVE_ZERO     = 1
  SIGN_POSITIVE_FINITE   = 2
  SIGN_POSITIVE_INFINITE = 3

  EXCEPTION_INFINITY   = 1
  EXCEPTION_OVERFLOW   = 1
  EXCEPTION_NaN        = 2
  EXCEPTION_UNDERFLOW  = 4
  EXCEPTION_ZERODIVIDE = 16
  EXCEPTION_ALL        = 255
  ROUND_MODE           = 256

  ROUND_UP        = 1
  ROUND_DOWN      = 2
  ROUND_HALF_UP   = 3
  ROUND_HALF_DOWN = 4
  ROUND_CEILING   = 5
  ROUND_FLOOR     = 6
  ROUND_HALF_EVEN = 7

  def self.mode(key, value = nil)
    key = Truffle::Type.coerce_to_int(key)
    Truffle::Type.check_long(key)

    if key == ROUND_MODE
      Thread.current[:'BigDecimal.rounding_mode'] ||= 3
      if value
        Thread.current[:'BigDecimal.rounding_mode'] = value
      else
        Thread.current[:'BigDecimal.rounding_mode']
      end
    else
      Thread.current[:'BigDecimal.exception_mode'] ||= 0
      case value
      when true
        Thread.current[:'BigDecimal.exception_mode'] |= key
        return value
      when false
        Thread.current[:'BigDecimal.exception_mode'] &= ~key
        return value
      when nil
        return Thread.current[:'BigDecimal.exception_mode']
      else
        raise ArgumentError, 'second argument must be true or false'
      end
    end
  end

  def self.limit(limit = nil)
    Thread.current[:'BigDecimal.precision_limit'] ||= 0
    if limit
      limit = Truffle::Type.coerce_to_int(limit)
      Truffle::Type.check_long(limit)

      old = Thread.current[:'BigDecimal.precision_limit']
      Thread.current[:'BigDecimal.precision_limit'] = limit
      old
    else
      Thread.current[:'BigDecimal.precision_limit']
    end
  end

  def clone
    self
  end
  alias_method :dup, :clone

  # TODO (pitr 20-jun-2015): remove when lazy setup is added
  def self.name
    'BigDecimal'
  end

  def self.double_fig
    20
  end

  def self.ver
    '1.1.0'
  end

  def ==(o)
    (self <=> o) == 0 rescue false
  end
  alias_method :eql?, :==
  alias_method :===, :==

  def coerce(other)
    [BigDecimal(other, 20), self]
  end

  def >(other)
    comp_helper(other, :>)
  end

  def >=(other)
    comp_helper(other, :>=)
  end

  def <(other)
    comp_helper(other, :<)
  end

  def <=(other)
    comp_helper(other, :<=)
  end

  private def comp_helper(other, op)
    comp = (self <=> other)
    if comp
      # cmp >= 0, comp < 0, etc
      comp.send(op, 0)
    else
      if nan? || (BigDecimal === other && other.nan?)
        return false
      else
        raise ArgumentError, "comparison of #{self.class} with #{other.class}"
      end
    end
  end

  def nonzero?
    zero? ? nil : self
  end

  def split
    sign = self.sign
    sign = 1 if sign > 1
    sign = -1 if sign < -1
    [sign, unscaled, 10, exponent]
  end

  def floor(digit = nil)
    if digit
      round digit, ROUND_FLOOR
    else
      rounded = round 0, ROUND_FLOOR
      integer = rounded.to_i
      rounded == integer ? integer : rounded
    end
  end

  def truncate(digit = nil)
    if finite?
      if digit
        round digit, ROUND_DOWN
      else
        rounded = round 0, ROUND_DOWN
        integer = rounded.to_i
        rounded == integer ? integer : rounded
      end
    else
      if digit
        self
      else
        raise FloatDomainError
      end
    end
  end

  def ceil(digit = nil)
    if digit
      round digit, ROUND_CEILING
    else
      rounded = round 0, ROUND_CEILING
      integer = rounded.to_i
      rounded == integer ? integer : rounded
    end
  end

  def fix
    if finite?
      round 0, ROUND_DOWN
    else
      self
    end
  end

  def frac
    if finite?
      self - fix
    else
      self
    end
  end

  def to_s(format = 'E')
    if finite?
      float_format    = format[-1] == 'F' || format[-1] == 'f'
      space_frequency = format.to_i
      prefix          = if self > 0 && [' ', '+'].include?(format[0])
                          format[0]
                        elsif self < 0
                          '-'
                        else
                          ''
                        end
      unscaled_value  = unscaled
      exponent_value  = exponent

      if float_format
        case
        when exponent_value > unscaled_value.size
          before_dot = unscaled_value + '0' * (exponent_value - unscaled_value.size)
          after_dot  = '0'
        when exponent_value <= 0
          before_dot = '0'
          after_dot  = '0' * exponent_value.abs + unscaled_value
        else
          before_dot = unscaled_value[0...exponent_value]
          rest       = unscaled_value[exponent_value..-1]
          after_dot  = rest.empty? ? '0' : rest
        end

        format '%s%s.%s',
               prefix,
               add_spaces_to_s(before_dot, true, space_frequency),
               add_spaces_to_s(after_dot, false, space_frequency)
      else
        format '%s0.%se%d',
               prefix,
               add_spaces_to_s(unscaled_value, false, space_frequency),
               exponent_value
      end
    else
      (sign < 0 ? '-' : '') + unscaled
    end
  end

  def inspect
    finite? ? to_s.downcase : to_s
  end

  def _dump(level=nil)
    # TODO (pitr 30-jun-2015): increase density
    _sig, max_sig = precs
    str = to_s
    "#{max_sig}:#{self}".force_encoding(str.encoding)
  end

  def self._load(data)
    new(data.split(':').last)
  end

  private def add_spaces_to_s(string, reverse, space_frequency)
    return string if space_frequency == 0

    remainder = string.size % space_frequency
    shift     = reverse ? remainder : 0
    pieces    = (string.size / space_frequency).times.map { |i| string[space_frequency*i + shift, space_frequency] }

    if remainder > 0
      if reverse
        pieces.unshift string[0...remainder]
      else
        pieces.push string[-remainder..-1]
      end
    end

    pieces.join ' '
  end

  def self.boolean_mode(key, value = nil)
    if value.nil?
      mode(key) & key == key
    else
      mode key, value
    end
  end
  private_class_method :boolean_mode

  def round_mode_from_symbol(symbol)
    case symbol
    when :up
      ROUND_UP
    when :down, :truncate
      ROUND_DOWN
    when :half_up, :default
      ROUND_HALF_UP
    when :half_down
      ROUND_HALF_DOWN
    when :half_even, :banker
      ROUND_HALF_EVEN
    when :ceiling, :ceil
      ROUND_CEILING
    when :floor
      ROUND_FLOOR
    else
      raise ArgumentError, 'invalid rounding mode'
    end
  end
  private :round_mode_from_symbol

  def self.new(*args)
    warn 'BigDecimal.new is deprecated; use BigDecimal() method instead.'
    BigDecimal(*args)
  end
end

module Kernel
  def BigDecimal(value, precision = Truffle::UNDEFINED)
    Truffle.invoke_primitive :bigdecimal_new, value, precision
  end
end

require 'bigdecimal/math'
