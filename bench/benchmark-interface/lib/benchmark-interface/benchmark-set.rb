# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module BenchmarkInterface
  class BenchmarkSet

    attr_reader :iterations

    def initialize
      @benchmarks = []
      @counter = 0
      @@current = self
      @iterations = 1
    end

    def load_benchmarks(path)
      @path = path
      @@current = self
      load(path)
    end

    def load_mri_benchmarks(path, options)
      @path = path
      @@current = self
      Frontends::MRI.load_mri path, options
    end

    def register(name, code)
      name = implicit_name unless name
      @benchmarks.push Benchmark.new(name, code)
    end

    def implicit_name
      file = File.basename(@path, '.rb')
      @counter += 1
      "#{file}:#{@counter}"
    end

    def prepare
      # Don't give benchmarks line numbers if there's only one

      if @benchmarks.size == 1
        @benchmarks.first.remove_line_numbers
      end

      # Give benchmarks iterations if needed

      if @benchmarks.any?(&:needs_iterating?)
        print "This benchmark set contains blocks that want a number of iterations - "
        STDOUT.flush

        iterations = @benchmarks.map(&:iterations_for_one_second).min

        puts "running all iterations #{iterations} times"

        @benchmarks.each do |b|
          b.iterate iterations
        end

        @iterations = iterations
      end
    end

    def benchmarks(names=nil)
      if names
        @benchmarks.select { |b| names.include?(b.name) }
      else
        @benchmarks
      end
    end

    def benchmark(name)
      benchmarks([name]).first
    end

    @@current = nil

    def self.current
      @@current
    end

  end
end
