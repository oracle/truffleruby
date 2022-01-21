# % ruby -I../../benchmark-ips/lib kwargs-demos/short-benchmark.rb                                                                # => 10.025M (± 6.3%) i/s
# % jt --use master ruby --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/short-benchmark.rb                       # => 38.648M (±13.6%) i/s
# % jt --use call-target-agnostic-kwargs ruby --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/short-benchmark.rb  # => 86.955M (±15.8%) i/s

require 'benchmark/ips'

def bar(a:, b:)
  a + b
end

def foo(a, b)
  bar(a: a, b: b)
end

# Defeat value-profiling
foo(100, 200)

Benchmark.ips do |x|
  x.report("foo") do
    foo(1, 2)
  end
end
