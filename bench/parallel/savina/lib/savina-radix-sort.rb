# This code is derived from the Savina benchmark suite,
# maintained at https://github.com/shamsmahmood/savina.
# This benchmark is a Ruby version of the Scala benchmark
# "RadixSortAkkaActorBenchmark.scala" in that repository.
# The LICENSE is GPLv2 as the original benchmark:
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html

# Savina Radix Sort

#ARGS = [100_000, 256, 74755] # 10 actors
ARGS = [100_000, 64, 74755] # 8 actors (1 + 6 + 1)
#ARGS = [200_000, 16, 74755] # 6 actors

require_relative 'savina-common'

NUM_VALUES, MAX_VALUE, SEED = ARGS

class SavinaRadixSort < SavinaBenchmark
  def benchmark
    validation_actor = ValidationActor.new
    source_actor = IntSourceActor.new

    radix = MAX_VALUE / 2
    next_actor = validation_actor
    while radix > 0
      sort_actor = SortActor.new(radix, next_actor)
      radix /= 2
      next_actor = sort_actor
    end

    source_actor.send!(NextActorMessage.new(next_actor))
    Actor.await_all_actors
    validation_actor.sum_so_far
  end

  def self.verify(result)
    expected = case
    when NUM_VALUES == 100 && MAX_VALUE == 256 && SEED == 74755
      13606
    when NUM_VALUES == 10_000 && MAX_VALUE == 65536 && SEED == 74755
      329373752
    when NUM_VALUES == 50_000 && MAX_VALUE == 65536 && SEED == 74755
      1642300184
    when NUM_VALUES == 500_000 && MAX_VALUE == 16 && SEED == 74755
      3750000
    when NUM_VALUES == 100_000 && MAX_VALUE == 256 && SEED == 74755
      12750640
    when NUM_VALUES == 100_000 && MAX_VALUE == 64 && SEED == 74755
      3150128
    when NUM_VALUES == 200_000 && MAX_VALUE == 16 && SEED == 74755
      1500000
    when NUM_VALUES == 300_000 && MAX_VALUE == 256 && SEED == 74755
      38249616
    else
      warn "result: #{result} does not have a hardcoded verification result for this config yet"
    end
    raise "Wrong result: #{result} VS #{expected}" unless result == expected
    true
  end
end

NextActorMessage = Struct.new(:actor)
ValueMessage = Struct.new(:value)

class IntSourceActor < Actor
  def initialize
    @random = MyRandom.new(SEED)
  end

  def process(message)
    case message
    when NextActorMessage
      NUM_VALUES.times {
        candidate = @random.next % MAX_VALUE
        message.actor.send! ValueMessage.new(candidate)
      }
      :exit
    else
      raise
    end
  end
end

class SortActor < Actor
  def initialize(radix, next_actor)
    @radix = radix
    @next_actor = next_actor
    @ordering_array = Array.new(NUM_VALUES, 0)
    @values_so_far = 0
    @j = 0
  end

  def process(message)
    case message
    when ValueMessage
      @values_so_far += 1
      current = message.value
      if (current & @radix) == 0
        @next_actor.send! message
      else
        @ordering_array[@j] = current
        @j += 1
      end

      if @values_so_far == NUM_VALUES
        @j.times { |i|
          # Polymorphic as next_actor can be ValidationActor
          # Would help to have 2 lookup/1 call here
          @next_actor.send! ValueMessage.new(@ordering_array[i])
        }
        :exit
      end
    else
      raise
    end
  end
end

class ValidationActor < Actor
  attr_reader :sum_so_far

  def initialize
    @sum_so_far = 0
    @values_so_far = 0
    @prev_value = 0
    @error_value = -1
    @error_index = -1
  end


  def process(message)
    case message
    when ValueMessage
      @values_so_far += 1
      value = message.value
      if value < @prev_value and @error_value < 0
        @error_value = value
        @error_index = @values_so_far - 1
      end

      @prev_value = value
      @sum_so_far += @prev_value

      if @values_so_far == NUM_VALUES
        if @error_value >= 0
          puts "ERROR: Value out of place: #{@error_value} at index #{@error_index}"
        else
          # puts "Elements sum: #{@sum_so_far}"
        end
        :exit
      end
    else
      raise
    end
  end
end
