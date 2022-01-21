# % ruby -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb                                                                   # =>    6.520M (± 0.6%) i/s
# % jt --use master ruby --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb                          # =>    6.929M (±12.3%) i/s
# % jt --use call-target-agnostic-kwargs ruby --engine.InlineOnly=~bar -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb     # =>   59.189M (±12.0%) i/s
# % jt --use master ruby --engine.CompileOnly=nothing -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb                      # =>  537.877k (± 6.2%) i/s
# % jt --use call-target-agnostic-kwargs ruby --engine.CompileOnly=nothing -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb # =>  821.865k (± 5.2%) i/s
# % jt --use master ruby --engine.InlineOnly=~foo -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb                          # =>   13.151M (±10.0%) i/s
# % jt --use call-target-agnostic-kwargs ruby --engine.InlineOnly=~foo -I../../benchmark-ips/lib kwargs-demos/long-benchmark.rb     # =>   77.617M (±12.0%) i/s

require 'benchmark/ips'

def bar(a:, b:, c:, d:, e:, f:, g:, h:)
  a + b + c + d + e + f + g + h
end

def foo(a, b, c, d, e, f, g, h)
  bar(a: a, b: b, c: c, d: d, e: e, f: f, g: g, h: h)
end

# Defeat value-profiling
foo(100, 200, 300, 400, 500, 600, 700, 800)

Benchmark.ips do |x|
  x.report("foo") do
    foo(1, 2, 3, 4, 5, 6, 7, 8)
  end
end
