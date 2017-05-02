# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module BenchmarkInterface
  class Benchmark
  
    attr_reader :name, :block
  
    def initialize(name, block)
      @name = name
      @block = block
    end

    def remove_line_numbers
      @name = @name.split(':')[0...-1].join(':') if @name.include? ':'
    end

    def time_block(desired_time)
      iterations = 1
      while true
        start = Time.now
        if block.arity == 1
          block.call iterations
        else
          iterations.times do
            block.call
          end
        end

        time = Time.now - start
        return [time, iterations] if time >= desired_time
        iterations *= 2
      end
    end

    def basic_iteration_time
      time, iterations = time_block(0.1)
      time / iterations.to_f
    end

    def iterations_for_one_second
      _, iterations = time_block(1)
      iterations
    end
    
    def needs_iterating?
      @block.arity == 1
    end

    def iterate(iterations)
      original_block = @block

      if original_block.arity == 1
        @block = Proc.new do
          original_block.call iterations
        end
      else
        @block = Proc.new do
          iterations.times do
            original_block.call
          end
        end
      end
    end
  
  end
end
