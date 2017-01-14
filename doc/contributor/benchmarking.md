# Benchmarking TruffleRuby

## Benchmarking with Graal

Checkout the `all-ruby-benchmarks` and `benchmark-interface` repositories above
your checkout of JRuby. We usually run like this.

```
$ jt benchmark .../all-ruby-benchmarks/classic/mandelbrot.rb --simple
```

Output is iterations per second, printed roughly every second (more frequently
for the first few iterations).

THe best way to set JVM options here is to use `JAVA_OPTS`.

# Benchmarking without Graal

You can turn off Graal if you want using `--no-graal`.

```
$ jt benchmark --no-graal .../all-ruby-benchmarks/classic/mandelbrot.rb --simple
```

You can benchmark JRuby Classic using `-Xclassic` in `JRUBY_OPTS`.

```
$ JRUBY_OPTS=-Xclassic jt benchmark .../all-ruby-benchmarks/classic/mandelbrot.rb --simple
```

You can benchmark an entirely different implementation using the
`JT_BENCHMARK_RUBY` environment variable.

```
$ JT_BENCHMARK_RUBY=ruby jt benchmark .../all-ruby-benchmarks/classic/mandelbrot.rb --simple
```
