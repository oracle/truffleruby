# Minimal setup
class SavinaBenchmark
end

require_relative 'lib/savina-apsp'

bench = SavinaApsp.new

benchmark do
  bench.benchmark
end
