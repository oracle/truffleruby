# Minimal setup
class SavinaBenchmark
end

require_relative 'lib/savina-radix-sort'

bench = SavinaRadixSort.new

benchmark do
  bench.benchmark
end
