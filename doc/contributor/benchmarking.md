# Benchmarking TruffleRuby

## Benchmarking with Graal

```
$ jt benchmark bench/classic/mandelbrot.rb --simple
```

Output is iterations per second, printed roughly every second (more frequently
for the first few iterations).

The best way to set JVM options here is to use `JAVA_OPTS`.

# Benchmarking without Graal

You can turn off Graal if you want using `--no-graal`.

```
$ jt benchmark --no-graal bench/classic/mandelbrot.rb --simple
```

You can benchmark an entirely different implementation using the
`JT_BENCHMARK_RUBY` environment variable.

```
$ JT_BENCHMARK_RUBY=ruby jt benchmark bench/classic/mandelbrot.rb --simple
```
