# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module BenchmarkInterface
  module Backends
    module Deep

      def self.run(benchmark_set, names, options)
        unless names.size == 1
          abort 'The deep backend only works when you run just one benchmark at a time - specify the name on the command line'
        end
        
        BeckhmarkInterface.require_rubygems
        benchmark_interface_original_require 'deep-bench/backend'
        
        benchmark = benchmark_set.benchmark(names.first)
        
        tags = options['--tag']
        properties = options['--prop']
        full_time = options['--time']
        freq = options['--freq']
        log_file = options['--log']
        
        DeepBench::Backend.run(
          tags, properties,
          benchmark.name, benchmark_set.iterations,
          full_time, freq, log_file,
          benchmark.block)
      end

    end
  end
end
