# Reporting Performance Problems

We are interested to hear from you if performance of TruffleRuby is lower than
other implementations of Ruby for code that you care about. `compatibility.md`
lists some features which we know are slow and are unlikely to get faster.

TruffleRuby uses extremely sophisticated techniques to optimise your Ruby
program. These optimisations take time to apply, so TruffleRuby is often a lot
slower than other implementations until it has had time to 'warm up'. Also,
TruffleRuby tries to find a 'stable state' of your program and to automatically
remove the dynamism of Ruby where it is not needed, but this then means that if
the stable state is disturbed by something performance lowers again until
TruffleRuby can adapt to the new stable state. Another problem is that
TruffleRuby is very good at removing unnecessary work, such as calculations that
are not needed or loops that contain no work.

All of these issues make it hard to benchmark TruffleRuby. This isn't a problem
that is unique to us - it applies to many sophisticated virtual machines - but
most Ruby implementations are not yet doing optimisations powerful enough to
show them so it may be a new problem to some people in the Ruby community.

## How to write a performance benchmark

We recommend that you use [`benchmark-ips`](https://github.com/evanphx/benchmark-ips), by Evan Phoenix, to check the performance of TruffleRuby, and it makes things easier for us if you report any potential performance problems using a report from `benchmark-ips`. 

A benchmark could look like this:

```ruby
require 'benchmark/ips'

Benchmark.ips do |x|
  
  x.iterations = 3
  
  x.report("adding") do
    14 + 2
  end
  
end
```

We use the `x.iterations =` extension in `benchmark-ips` to run the warmup and
measurement cycles of `benchmark-ips` three times, because otherwise the first
transition from warmup to measurement triggers one of those stable state upsets
described above and will temporarily lower performance just as timing starts. I
wrote about this issue in [more technical
depth](https://github.com/evanphx/benchmark-ips/pull/58) if you want to know
more details.

I usually install `benchmark-ips` using another Ruby and then just point the
include path at this version when running with TruffleRuby, since we don't
support much of RubyGems yet.

```
$ bin/ruby -I~/.rbenv/versions/2.3.3/lib/ruby/gems/2.3.0/gems/benchmark-ips-2.7.2/lib benchmark.rb
```

You'll see something like this:

```
Warming up --------------------------------------
              adding    20.933k i/100ms
              adding     1.764M i/100ms
              adding     1.909M i/100ms
Calculating -------------------------------------
              adding      2.037B (±12.7%) i/s -      9.590B in   4.965741s
              adding      2.062B (±11.5%) i/s -     10.123B in   4.989398s
              adding      2.072B (±10.5%) i/s -     10.176B in   4.975818s
```

We want to look at the last line, which says that TruffleRuby runs 2.072 billion
iterations of this block per second, with a margin of error of ±10.5%.

Compare that to an implementation like Rubinius:

```
Warming up --------------------------------------
              adding    71.697k i/100ms
              adding    74.983k i/100ms
              adding    75.195k i/100ms
Calculating -------------------------------------
              adding      2.111M (±12.2%) i/s -     10.302M
              adding      2.126M (±10.6%) i/s -     10.452M
              adding      2.134M (± 9.2%) i/s -     10.527M
```

So we'd describe that as a thousand times faster than Rubinius. That seems like
a lot - and what is actually happening here is that TruffleRuby is optimising
away your benchmark. There's not much we can do about that (a more technical
discussion of why follows), but it's the best we can do. The effect is less with
complex code that we cannot optimise away.

## More technical notes

### Black holes

Some other benchmarking tools for other languages have a feature called 'black
holes'. These surround a value and make it appear to be variable at runtime even
if it's in fact a constant, so that the optimiser does not remove it and
actually performs any computations that use it. However, TruffleRuby uses
extensive value profiling (caching of values and turning them into constants),
and even if you make a value appear to be a variable at its source, it is likely
to be value profiled at an intermediate stage. In general we'd prefer more
complex benchmarks that naturally defeat value profiling, rather than manually
adding annotations to turn off important features.
