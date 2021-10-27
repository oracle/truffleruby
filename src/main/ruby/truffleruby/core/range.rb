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

class Range

  include Enumerable

  # Only used if called directly with allocate + #initialize.
  # Range.new is defined in Java.
  def initialize(first, last, exclude_end = false)
    raise NameError, "`initialize' called twice" if self.begin

    unless Primitive.object_kind_of?(first, Integer) && Primitive.object_kind_of?(last, Integer)
      begin
        raise ArgumentError, 'bad value for range' unless first <=> last
      rescue
        raise ArgumentError, 'bad value for range'
      end
    end

    Primitive.range_initialize self, first, last, exclude_end
  end
  private :initialize

  def ==(other)
    return true if equal? other

    Primitive.object_kind_of?(other, Range) and
      self.begin == other.begin and
      self.end == other.end and
      self.exclude_end? == other.exclude_end?
  end

  def eql?(other)
    return true if equal? other

    Primitive.object_kind_of?(other, Range) and
        self.begin.eql?(other.begin) and
        self.end.eql?(other.end) and
        self.exclude_end? == other.exclude_end?
  end

  def bsearch(&block)
    return to_enum :bsearch unless block_given?

    start = self.begin
    stop  = self.end

    if Float === start || Float === stop
      bsearch_float(&block)
    elsif Integer === start
      if Primitive.nil? stop
        bsearch_endless(&block)
      elsif Integer === stop
        bsearch_integer(&block)
      else
        raise TypeError, "bsearch is not available for #{stop.class}"
      end
    elsif Primitive.nil?(start) && Integer === stop
      bsearch_beginless(&block)
    else
      raise TypeError, "bsearch is not available for #{start.class}"
    end
  end

  private def midpoint(low, high)
    mid1 = (low + high) / 2
    mid2 = low/2 + high/2
    mid1.abs.infinite? ? mid2 : mid1 # mitigate potential overflow

    # NOTE(norswap, 23 Jan. 2020)
    #   This might not be a very "centered" midpoint, but it's sufficient to cut the range so that we still get
    #   ~ O(log n) binary search runtime.
  end

  private def bsearch_float(&block)
    normalized_begin = Primitive.nil?(self.begin) ? -Float::INFINITY : self.begin.to_f
    normalized_end = Primitive.nil?(self.end) ? Float::INFINITY : self.end.to_f
    normalized_end = normalized_end.prev_float if self.exclude_end?
    min = normalized_begin
    max = normalized_end
    last_admissible = nil
    stop = false

    # The next max-min comparisons simplify infinity handling.
    # Infinity-handling is necessary to be able to cut ranges in two.
    return nil if max < min || max == min && self.exclude_end? #-inf.prev_float == -inf!
    if max == min
      result = yield max
      return result == true || result == 0 ? max : nil
    end

    # max == -Float::INFINITY will already be covered by the comparisons above.
    if max == Float::INFINITY
      result = yield max
      case result
      when Numeric
        if result == 0
          return max
        elsif result > 0
          # There is nothing above infinity.
          return nil
        end
      when true
        last_admissible = max
      end
      max = normalized_end = Float::MAX # guaranteed to be >= min
    end

    # min == Float::INFINITY is already covered by the comparisons above.
    if min == -Float::INFINITY
      result = yield min
      # Find-minimum mode: It's not going to get any smaller than negative infinity.
      return min if result == true || result == 0
      return nil if Primitive.object_kind_of?(result, Numeric) && result < 0
      min = normalized_begin = -Float::MAX # guaranteed to be <= max
    end

    mid = 0
    until stop
      mid = midpoint(min, max)
      result = yield mid

      # Must check this before modifying min or max.
      stop = true if min == mid || mid == max

      case result
      when Numeric
        if result > 0
          min = mid
        elsif result < 0
          max = mid
        else
          last_admissible = mid
          break
        end
      when true
        last_admissible = mid
        max = mid
      when false, nil
        min = mid
      else
        raise TypeError, 'Range#bsearch block must return Numeric or boolean'
      end
    end

    # At the end of the loop, mid could be equal to min or max due to precision limits and the other bound
    # might not have been tested in some cases.
    if mid == min && max == normalized_end && (Primitive.nil?(last_admissible) || last_admissible == Float::INFINITY)
      # max is untested only if it's the end of the range.
      # It can only change the result of the search if we haven't found an admissible value yet (+ infinity edge case).
      result = yield max
      return max if result == true || result == 0
    elsif mid == max && min == normalized_begin
      # min is untested only if it's the begin of the range.
      result = yield min
      # Favor smallest value in case we're in find-minimum mode.
      return min if result == true || result == 0
    end

    last_admissible
  end

  private def bsearch_endless(&block)
    min = self.begin
    cur = min
    diff = 1

    while true
      result = yield cur
      case result
      when 0
        return cur
      when Numeric
        return (min..cur).bsearch(&block) if result < 0
      when true
        return (min..cur).bsearch(&block)
      end
      cur += diff
      diff *= 2
    end
  end

  private def bsearch_beginless(&block)
    max = self.end
    cur = max
    diff = 1

    while true
      result = yield cur
      case result
      when 0
        return cur
      when Numeric
        return (cur..max).bsearch(&block) if result > 0
      when false
        return (cur..max).bsearch(&block)
      end
      cur -= diff
      diff *= 2
    end
  end

  private def bsearch_integer(&block)
    min = self.begin
    max = self.end
    max -= 1 if Primitive.object_kind_of?(max, Integer) and exclude_end?
    return nil if max < min
    last_admissible = nil
    stop = false

    until stop
      # Do one last iteration on the bounds when they converge.
      stop = true if min == max
      mid = (min + max) / 2 # integers don't overflow \o/
      result = yield mid

      case result
      when Numeric
        if result > 0
          min = mid + 1
        elsif result < 0
          max = mid
        else
          return mid
        end
      when true
        last_admissible = mid
        max = mid
      when false, nil
        min = mid + 1
      else
        raise TypeError, 'Range#bsearch block must return Numeric or boolean'
      end
    end

    last_admissible
  end

  def count(item = undefined)
    if Primitive.nil?(self.begin) || Primitive.nil?(self.end)
      return Float::INFINITY unless block_given? || !Primitive.undefined?(item)
    end

    super
  end

  private def each_internal(&block)
    return to_enum { size } unless block_given?
    first, last = self.begin, self.end

    unless first.respond_to?(:succ) && !Primitive.object_kind_of?(first, Time)
      raise TypeError, "can't iterate from #{first.class}"
    end

    return each_endless(first, &block) if Primitive.nil? last

    case first
    when Integer
      last -= 1 if exclude_end?

      i = first
      while i <= last
        yield i
        i += 1
      end
    when String
      first.upto(last, exclude_end?) do |str|
        yield str
      end
    when Symbol
      first.to_s.upto(last.to_s, exclude_end?) do |str|
        yield str.to_sym
      end
    else
      current = first
      if exclude_end?
        while (current <=> last) < 0
          yield current
          current = current.succ
        end
      else
        while (c = current <=> last) && c <= 0
          yield current
          break if c == 0
          current = current.succ
        end
      end
    end

    self
  end

  private def each_endless(first, &block)
    if Integer === first
      i = first
      while true
        yield i
        i += 1
      end
    else
      current = first
      while true
        yield current
        current = current.succ
      end
    end
  end

  def first(n=undefined)
    raise RangeError, 'cannot get the first element of beginless range' if Primitive.nil?(self.begin)
    return self.begin if Primitive.undefined? n

    super
  end

  # Random number for hash codes. Stops hashes for similar values in
  # different classes from clashing, but defined as a constant so
  # that hashes will be deterministic.

  CLASS_SALT = 0xb36df84d

  private_constant :CLASS_SALT

  def hash
    val = Primitive.vm_hash_start(CLASS_SALT)
    val = Primitive.vm_hash_update(val, exclude_end? ? 1 : 0)
    val = Primitive.vm_hash_update(val, self.begin.hash)
    val = Primitive.vm_hash_update(val, self.end.hash)
    Primitive.vm_hash_end(val)
  end

  def include?(value)
    if self.begin.respond_to?(:to_int) ||
        self.end.respond_to?(:to_int) ||
        Primitive.object_kind_of?(self.begin, Numeric) ||
        Primitive.object_kind_of?(self.end, Numeric) ||
        Primitive.object_kind_of?(self.begin, Time) ||
        Primitive.object_kind_of?(self.end, Time)
      cover? value
    else
      super
    end
  end

  alias_method :member?, :include?

  def ===(value)
    cover?(value)
  end

  def inspect
    sep = exclude_end? ? '...' : '..'
    if (Primitive.nil?(self.begin) && Primitive.nil?(self.end))
      "nil#{sep}nil"
    else
      (Primitive.nil?(self.begin) ? '' : self.begin.inspect) + sep + (Primitive.nil?(self.end) ? '' : self.end.inspect)
    end
  end

  def last(n=undefined)
    raise RangeError, 'cannot get the last element of endless range' if Primitive.nil? self.end
    return self.end if Primitive.undefined? n

    to_a.last(n)
  end

  def max
    raise RangeError, 'cannot get the maximum of endless range' if Primitive.nil? self.end
    if Primitive.nil? self.begin
      raise RangeError, 'cannot get the maximum of beginless range with custom comparison method' if block_given?
      return exclude_end? ? self.end - 1 : self.end
    end
    return super if block_given? || (exclude_end? && !Primitive.object_kind_of?(self.end, Numeric))
    return nil if Comparable.compare_int(self.end <=> self.begin) < 0
    return nil if exclude_end? && Comparable.compare_int(self.end <=> self.begin) == 0
    return self.end unless exclude_end?

    unless Primitive.object_kind_of?(self.end, Integer)
      raise TypeError, 'cannot exclude non Integer end value'
    end

    unless Primitive.object_kind_of?(self.begin, Integer)
      raise TypeError, 'cannot exclude end value with non Integer begin value'
    end

    self.end - 1
  end

  def min
    raise RangeError, 'cannot get the minimum of beginless range' if Primitive.nil? self.begin
    if Primitive.nil? self.end
      raise RangeError, 'cannot get the minimum of endless range with custom comparison method' if block_given?
      return self.begin
    end
    return super if block_given?
    if Comparable.compare_int(self.end <=> self.begin) < 0
      return nil
    elsif exclude_end? && self.end == self.begin
      return nil
    end

    self.begin
  end

  def minmax(&block)
    if block
      super(&block)
    else
      [min, max]
    end
  end

  def %(n)
    step(n)
  end

  private def step_internal(step_size=1, &block) # :yields: object

    if !block_given? && (Primitive.nil?(self.begin) || Primitive.object_kind_of?(self.begin, Numeric)) && (Primitive.nil?(self.end) || Primitive.object_kind_of?(self.end, Numeric))
      return Enumerator::ArithmeticSequence.new(self, :step, self.begin, self.end, step_size, self.exclude_end?)
    end

    return to_enum(:step, step_size) do
      validated_step_args = Truffle::RangeOperations.validate_step_size(self.begin, self.end, step_size)
      Truffle::RangeOperations.step_iterations_size(self, *validated_step_args)
    end unless block_given?

    values = Truffle::RangeOperations.validate_step_size(self.begin, self.end, step_size)
    first = values[0]
    last = values[1]
    step_size = values[2]

    return step_endless(first, step_size, &block) if Primitive.nil? last

    case first
    when Float
      iterations = Truffle::NumericOperations.float_step_size(first, last, step_size, exclude_end?)

      i = 0
      while i < iterations
        curr = i * step_size + first
        curr = last if last < curr
        yield curr
        i += 1
      end
    when Numeric
      curr = first
      last -= 1 if exclude_end?

      while curr <= last
        yield curr
        curr += step_size
      end
    else
      i = 0
      each do |item|
        yield item if i % step_size == 0
        i += 1
      end
    end

    self
  end

  private def step_endless(first, step_size, &block)
    if Numeric === first
      curr = first
      while true
        yield curr
        curr += step_size
      end
    else
      i = 0
      each_endless(first) do |item|
        yield item if i % step_size == 0
        i += 1
      end
    end
  end

  def to_s
    "#{self.begin}#{exclude_end? ? "..." : ".."}#{self.end}"
  end

  def cover?(value)
    if Primitive.object_kind_of?(value, Range)
      Truffle::RangeOperations.range_cover?(self, value)
    else
      Truffle::RangeOperations.cover?(self, value)
    end
  end

  def size
    return Float::INFINITY if Primitive.nil? self.begin
    return nil unless Primitive.object_kind_of?(self.begin, Numeric)
    return Float::INFINITY if Primitive.nil? self.end

    delta = self.end - self.begin
    return 0 if delta < 0

    if Primitive.object_kind_of?(self.begin, Float) || Primitive.object_kind_of?(self.end, Float)
      return delta if delta == Float::INFINITY

      err = (self.begin.abs + self.end.abs + delta.abs) * Float::EPSILON
      err = 0.5 if err > 0.5

      (exclude_end? ? delta - err : delta + err).floor + 1
    else
      delta += 1 unless exclude_end?
      delta
    end
  end

  def map(&block)
    ary = Primitive.range_integer_map(self, block)
    if !Primitive.undefined?(ary)
      ary
    else
      super(&block)
    end
  end
  alias_method :collect, :map

  private def to_a_internal # MODIFIED called from java to_a
    return to_a_from_enumerable unless Primitive.object_kind_of?(self.begin, Integer) and Primitive.object_kind_of?(self.end, Integer)

    fin = self.end
    fin += 1 unless exclude_end?

    size = fin - self.begin
    return [] if size <= 0

    ary = Array.new(size)

    i = 0
    while i < size
      ary[i] = self.begin + i
      i += 1
    end

    ary
  end

  private def to_a_from_enumerable(*arg)
    ary = []
    each(*arg) do
      o = Primitive.single_block_arg
      ary << o
      nil
    end
    ary
  end
end
