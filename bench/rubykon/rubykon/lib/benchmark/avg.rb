require_relative 'avg/job'
require_relative 'avg/benchmark_suite'

module Benchmark
  module Avg
    def avg
      benchmark_suite = BenchmarkSuite.new
      yield benchmark_suite
      benchmark_suite.run
    end
  end

  extend Benchmark::Avg
end
