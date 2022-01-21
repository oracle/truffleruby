# % ruby -I../../benchmark-ips/lib kwargs-demos/baseline-benchmark.rb                                                                   # => 13.399M (± 1.0%) i/s
# % jt --use master ruby --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/baseline-benchmark.rb                          # => 97.129M (±13.2%) i/s
# % jt --use master call-target-agnostic-kwargs --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/baseline-benchmark.rb   # => 95.070M (±13.0%) i/s

require 'benchmark/ips'

def bar(a, b)
  a + b
end

def foo(a, b)
  bar(a, b)
end

# Defeat value-profiling
foo(100, 200)

Benchmark.ips do |x|
  x.report("foo") do
    foo(1, 2)
  end
end
