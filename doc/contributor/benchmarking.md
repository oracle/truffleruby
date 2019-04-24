# Benchmarking TruffleRuby

## Benchmarking with the GraalVM Compiler

```
$ jt benchmark bench/classic/mandelbrot.rb --simple
```

Output is iterations per second, printed roughly every second (more frequently
for the first few iterations).

# Benchmarking without the GraalVM Compiler

You can turn off the GraalVM Compiler if you want using `--no-graal`.

```
$ jt benchmark --no-graal bench/classic/mandelbrot.rb --simple
```

You can benchmark an entirely different implementation using the
`JT_BENCHMARK_RUBY` environment variable.

```
$ JT_BENCHMARK_RUBY=ruby jt benchmark bench/classic/mandelbrot.rb --simple
```
