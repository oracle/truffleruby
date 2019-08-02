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

## OptCarrot

OptCarrot can be run the same way as the example above (with `bench/optcarrot/optcarrot.rb`),
but also has a fixed-workload harness with minimal dependencies,
which can be useful to debug or tune performance.

Run the fixed-workload harness with minimal dependencies with:

```bash
$ jt ruby bench/optcarrot/fixed-workload.rb
```

# Benchmarking without the GraalVM Compiler

You can turn off the GraalVM Compiler if you want, by not including it in the GraalVM build:

```bash
$ jt build
```

It's the same command to run the benchmark, for instance:

```bash
$ jt benchmark bench/classic/mandelbrot.rb --simple
```

You can benchmark an entirely different implementation using the `--use`
option or with `RUBY_BIN` environment variable.

```bash
$ jt --use ruby benchmark bench/classic/mandelbrot.rb --simple
$ RUBY_BIN=ruby jt benchmark bench/classic/mandelbrot.rb --simple
```
