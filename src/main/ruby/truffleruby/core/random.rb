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

class Truffle::Randomizer
  def initialize
    self.seed = generate_seed
  end

  def swap_seed(new_seed)
    old_seed = self.seed
    self.seed = new_seed
    old_seed
  end

  def random(limit)
    if Primitive.undefined?(limit)
      random_float
    else
      if limit.kind_of?(Range)
        if time_value?(limit.min)
          random_time_range(limit)
        else
          random_range(limit)
        end
      elsif limit.kind_of?(Float)
        raise ArgumentError, "invalid argument - #{limit}" if limit <= 0
        random_float * limit
      else
        limit_int = Truffle::Type.coerce_to limit, Integer, :to_int
        raise ArgumentError, "invalid argument - #{limit}" if limit_int <= 0

        if limit.is_a?(Integer)
          random_integer(limit - 1)
        elsif limit.respond_to?(:to_f)
          random_float * limit
        else
          random_integer(limit_int - 1)
        end
      end
    end
  end

  def random_range(limit)
    min, max = limit.max.coerce(limit.min)
    diff = max - min
    diff += 1 if max.kind_of?(Integer)
    random(diff) + min
  end

  ##
  # Returns a random value from a range made out of Time, Date or DateTime
  # instances.
  #
  # @param [Range] range
  # @return [Time|Date|DateTime]
  #
  def random_time_range(range)
    min  = time_to_float(range.min)
    max  = time_to_float(range.max)
    time = Time.at(random(min..max))

    if Object.const_defined?(:DateTime) && range.min.is_a?(DateTime)
      time = time.to_datetime
    elsif Object.const_defined?(:DateTime) && range.min.is_a?(Date)
      time = time.to_date
    end

    time
  end

  ##
  # Casts a Time/Date/DateTime instance to a Float.
  #
  # @param [Time|Date|DateTime] input
  # @return [Float]
  #
  def time_to_float(input)
    input.respond_to?(:to_time) ? input.to_time.to_f : input.to_f
  end

  ##
  # Checks if a given value is a Time, Date or DateTime object.
  #
  # @param [Mixed] input
  # @return [TrueClass|FalseClass]
  #
  def time_value?(input)
    input.is_a?(Time) || (Object.const_defined?(:Date) && input.is_a?(Date))
  end
end

class Random
  def self.new_seed
    Primitive.thread_randomizer.generate_seed
  end

  def self.raw_seed(size)
    raise ArgumentError, 'negative string size (or size too big)' if size < 0
    self.new.bytes(size)
  end

  def self.srand(seed=undefined)
    if Primitive.undefined? seed
      seed = Primitive.thread_randomizer.generate_seed
    end

    seed = Truffle::Type.coerce_to seed, Integer, :to_int
    Primitive.thread_randomizer.swap_seed seed
  end

  def self.rand(limit=undefined)
    Primitive.thread_randomizer.random(limit)
  end

  def self.random_number(limit=undefined)
    rand(limit)
  end

  def self.bytes(length)
    Primitive.randomizer_bytes(Primitive.thread_randomizer, length)
  end

  def initialize(seed=undefined)
    @randomizer = Truffle::Randomizer.new
    if !Primitive.undefined?(seed)
      @randomizer.swap_seed seed.to_int
    end
  end

  def rand(limit=undefined)
    @randomizer.random(limit)
  end

  def seed
    @randomizer.seed
  end

  def state
    @randomizer.seed
  end
  private :state

  def ==(other)
    return false unless other.kind_of?(Random)
    seed == other.seed
  end

  # Returns a random binary string.
  # The argument size specified the length of the result string.
  def bytes(length)
    Primitive.randomizer_bytes @randomizer, length
  end
end

Truffle::Boot.delay do
  Random::DEFAULT = Random.new
end
