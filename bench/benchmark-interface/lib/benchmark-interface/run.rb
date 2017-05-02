# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module BenchmarkInterface

  NON_MRI_INDICATORS = %w(
    Benchmark.measure Benchmark.realtime Benchmark.benchmark Benchmark.bm
    Benchmark.bmbm RBench.run Benchmark.ips Perfer.session harness_sample
  )

  def self.run(args)
    set = BenchmarkInterface::BenchmarkSet.new

    command = nil

    backend = BenchmarkInterface::Backends::Bips
    names = []
    
    options = {
      '--no-scale' => false,
      '--use-cache' => false,
      '--show-rewrite' => false,
      '--cache' => false,
      '--time' => 10,
      '--freq' => 1,
      '--elapsed' => false,
      '--iterations' => false,
      '--log' => nil,
      '--tag' => [],
      '--prop' => {}
    }

    to_load = []
    first = false

    n = 0
    while n < args.size
      arg = args[n]
      if arg.start_with? '-'
        case arg
          when '--help', '-h', '-help', '--version', '-v', '-version'
            help
          when '--simple'
            backend = BenchmarkInterface::Backends::Simple
          when '--stable'
            backend = BenchmarkInterface::Backends::Stable
          when '--bips'
            backend = BenchmarkInterface::Backends::Bips
          when '--bm'
            backend = BenchmarkInterface::Backends::Bm
          when '--bmbm'
            backend = BenchmarkInterface::Backends::BmBm
          when '--bench9000'
            backend = BenchmarkInterface::Backends::Bench9000
          when '--deep'
            backend = BenchmarkInterface::Backends::Deep
          when '--Xfirst'
            first = true
          when '--time'
            options[arg] = Integer(args[n + 1])
            n += 1
          when '--freq'
            options[arg] = Float(args[n + 1])
            n += 1
          when '--elapsed'
            options[arg] = true
          when '--log'
            options[arg] = args[n + 1]
            n += 1
          when '--tag'
            options[arg].push args[n + 1]
            n += 1
          when '--prop'
            options[arg][args[n + 1]] = args[n + 2]
            n += 2
          else
            abort "unknown option #{arg}" unless options.keys.include?(arg)
            options[arg] = true
        end
      elsif arg.include?('.rb')
        to_load.push arg
      elsif to_load.empty? && !command
        command = arg.to_sym
      else
        names.push arg
      end
      n += 1
    end
    
    command ||= :run

    to_load.each do |path|
      source = File.read(path)
      if NON_MRI_INDICATORS.any? { |t| source.include?(t) } || source =~ /benchmark.*\{/ || source =~ /benchmark.*do/
        set.load_benchmarks path
      else
        set.load_mri_benchmarks path, options
      end
    end
    
    case command
    when :run
      set.prepare

      if set.benchmarks.empty?
        abort 'No benchmarks found!'
      end
      
      if first
        set.benchmarks.replace [set.benchmarks.first]
      end

      names.each do |name|
        unless set.benchmark(name)
          abort "Unknown benchmark #{name}"
        end
      end

      names = set.benchmarks.map(&:name) if names.empty?

      backend.run set, names, options
    when :list
      puts set.benchmarks.map(&:name)
    else
      abort "Unknown command #{command}"
    end
  end

  def self.help
    puts "Benchmark-Interface #{VERSION}"
    puts
    puts 'benchmark [command] benchmark-files.rb... [benchmark names...] [options]'
    puts
    puts 'Commands:'
    puts '  run       run listed benchmarks (default)'
    puts '  list      list benchmarks available in the files'
    puts
    puts 'Backends:'
    puts '  --simple'
    puts '  --stable'
    puts '  --bips (default)'
    puts '  --bm'
    puts '  --bmbm'
    puts '  --bench9000'
    puts '  --deep'
    puts
    puts 'Options:'
    puts '  --no-scale        Don\'t scale benchmarks for backends that expects benchmarks to take about a second'
    puts '  --show-rewrite    Show rewritten MRI benchmarks'
    puts '  --cache           Cache MRI rewrites'
    puts '  --use-cache       Use cached MRI rewrites'
    puts '  --time n          Run for n seconds, if the backend supports that'
    exit 1
  end

end
