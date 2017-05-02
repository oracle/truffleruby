require File.expand_path('lib/benchmark-interface/version', File.dirname(__FILE__))

Gem::Specification.new do |spec|
  spec.name          = 'benchmark-interface'
  spec.version       = BenchmarkInterface::VERSION
  spec.authors       = ['Chris Seaton']
  spec.email         = ['chris@chrisseaton.com']
  spec.summary       = 'One Ruby benchmarking interface to rule them all'
  spec.homepage      = 'https://github.com/jruby/benchmark-interface'
  spec.description   = 'A new format for writing Ruby benchmarks, and a tool ' \
                       'that lets you run benchmarks written in many formats ' \
                       'with many different benchmarking tools'
  spec.licenses      = ['EPL-1.0', 'GPL-2.0', 'LGPL-2.1']

  spec.files         = `git ls-files -z`.split("\x0")
  spec.require_paths = ['lib']
  spec.bindir        = 'bin'
  spec.executables   = ['benchmark']
end
