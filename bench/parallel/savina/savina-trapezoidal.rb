# Minimal setup
class SavinaBenchmark
end

require_relative 'lib/savina-trapezoidal'

bench = SavinaTrapezoidal.new

benchmark do
  bench.benchmark
end
