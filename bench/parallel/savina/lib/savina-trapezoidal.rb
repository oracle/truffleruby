# This code is derived from the Savina benchmark suite,
# maintained at https://github.com/shamsmahmood/savina.
# This benchmark is a Ruby version of the Scala benchmark
# "TrapezoidalAkkaActorBenchmark.scala" in that repository.
# The LICENSE is GPLv2 as the original benchmark:
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html

# Savina Trapezoidal Approximation

actors = Integer(ENV["ACTORS"] || 4)
#ARGS = [10_000_000, 100, 1.0, 5.0]
#ARGS = [10_000_000, 4, 1.0, 5.0] # Good, around 1s/iteration
#ARGS = [1_000_000, 4, 1.0, 5.0]
ARGS = [2_000_000, actors, 1.0, 5.0]

require_relative 'savina-common'

# N: num trapezoids
# W: num workers
# L: left end-point
# R: right end-point
p ARGS
N, W, L, R = ARGS

NUM_WORKERS = W

raise unless L.is_a?(Float) and R.is_a?(Float)
PRECISION = (R - L) / N

class SavinaTrapezoidal < SavinaBenchmark
  def benchmark
    master = MasterActor.new
    master.send! WorkMessage.new(L, R, PRECISION)
    Actor.await_all_actors
    master.result_area
  end

  def self.verify(result)
    eps = 1e-15
    expected = if N == 10_000_000 and W == 4
      0.27108075195294984
    elsif N == 2_000_000 and W == 4
      0.271080751950028
    elsif N == 2_000_000 and W == 2
      0.2710807519500298
    elsif N == 2_000_000 and W == 1
      0.2710807519500336
    elsif N == 1_000_000 and W == 4
      0.27108075194089204
    elsif N == 100_000 and W == 2
      0.27108664843106395
    else
      eps = 1e-5
      0.271080751950029
    end

    diff = (result - expected).abs
    success = diff < eps

    raise "Wrong result: #{result} VS #{expected} (#{diff})" unless success
    true
  end
end

WorkMessage = Struct.new(:l, :r, :h)
ResultMessage = Struct.new(:result, :workerID)

class MasterActor < Actor
  attr_reader :result_area
  def initialize
    @workers = Array.new(NUM_WORKERS) { |i|
      WorkerActor.new(self, i)
    }
    @received = 0
    @result_area = 0.0
  end

  def process(message)
    case message
    when ResultMessage
      @received += 1
      @result_area += message.result
      if @received == NUM_WORKERS
        :exit
      end
    when WorkMessage
      worker_range = (message.r - message.l) / NUM_WORKERS
      @workers.each_with_index { |worker,i|
        wl = (worker_range * i) + message.l
        wr = wl + worker_range

        worker.send! WorkMessage.new(wl, wr, message.h)
      }
    else
      raise
    end
  end
end

class WorkerActor < Actor
  def initialize(master, id)
    @master = master
    @id = id
  end

  def process(message)
    case message
    when WorkMessage
      wm = message
      n = ((wm.r - wm.l) / wm.h).to_i

      area = 0.0
      i = 0
      while i < n
        lx = (i * wm.h) + wm.l
        rx = lx + wm.h

        ly = fx(lx)
        ry = fx(rx)

        area += 0.5 * (ly + ry) * wm.h
        i += 1
      end

      @master.send! ResultMessage.new(area, @id)
      :exit
    else
      raise
    end
  end

  def fx(x)
    a = Math.sin(x**3 - 1)
    b = x + 1
    c = a / b
    d = Math.sqrt(1 + Math.exp(Math.sqrt(2 * x)))
    c * d
  end
end
