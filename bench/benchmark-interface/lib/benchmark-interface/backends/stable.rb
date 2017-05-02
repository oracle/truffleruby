# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module BenchmarkInterface
  module Backends
    module Stable

      def self.run(benchmark_set, names, options)
        full_time = options['--time']
        freq = options['--freq']
        elapsed = options['--elapsed']
        print_iterations = options['--iterations']
        
        inner_iterations = benchmark_set.iterations
        
        benchmark_set.benchmarks(names).each do |benchmark|
          puts benchmark.name
          block = benchmark.block

          start_time = Time.now

          while Time.now - start_time < full_time
            start_round_time = Time.now
            block.call
            round_time = Time.now - start_round_time
            
            ips = 1 / round_time
            puts Time.now - start_time if elapsed
            puts 1 if print_iterations
            puts ips * inner_iterations
          end
        end
      end

    end
  end
end
