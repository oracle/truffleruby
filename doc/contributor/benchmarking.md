# Benchmarking TruffleRuby

## Benchmarking with the GraalVM Compiler

First build TruffleRuby and include the GraalVM Compiler:

```bash
$ jt build --graal
```

Then run the benchmark, for instance:

```bash
$ jt benchmark bench/classic/mandelbrot.rb --simple
```

Output is iterations per second, printed roughly every second (more frequently
for the first few iterations).

# Benchmarking without the GraalVM Compiler

You can turn off the GraalVM Compiler if you want, by not including it in the GraalVM build:

```bash
$ jt build
```

It's the same command to run the benchmark, for instance:

```bash
$ jt benchmark bench/classic/mandelbrot.rb --simple
```

You can benchmark an entirely different implementation using the
`JT_BENCHMARK_RUBY` environment variable.

```bash
$ JT_BENCHMARK_RUBY=ruby jt benchmark bench/classic/mandelbrot.rb --simple
```
