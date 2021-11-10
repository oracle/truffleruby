# Benchmarking TruffleRuby

## Benchmarking with the GraalVM Compiler

First build TruffleRuby and include the GraalVM Compiler:

```bash
jt build --env native
# or
jt build --env jvm-ce
```

See [The "Building" section of the Contributor Workflow document](workflow.md#Building) for details on the different
types of builds environments. Beware that the default environemnt is `jvm`, which does not include the GraalVM compiler
that performs JIT compilation for Ruby, though that can of course be benchmarked too.

Then run the benchmark, for instance:

```bash
jt --use native benchmark bench/classic/mandelbrot.rb --simple
# or
jt --use jvm-ce benchmark bench/classic/mandelbrot.rb --simple
```

Output is iterations per second, printed roughly every second (more frequently
for the first few iterations).

## Benchmarking the TruffleRuby interpreter

To benchmark the interpreter, you typically want to run fewer iterations,
and to run on a native image of truffleruby to reduce noise.
Use `--engine.Compilation=false` to disable compilation.

As an example, this runs optcarrot for a single iteration:

```bash
bin/jt -u path/to/graalvm/bin/ruby benchmark --iterations --elapsed --ips --fixed-iterations 1 bench/optcarrot/optcarrot.rb -- --engine.Compilation=false
```

The output shows, in order:
* `--iterations`, the iteration number
* `--elapsed`, the time in seconds since the start of the benchmark. For a single iteration that's the time of that iteration.
* `--ips` (iterations/s), so simply 1/`elapsed`.

## OptCarrot

OptCarrot can be run the same way as the example above (with
`bench/optcarrot/optcarrot.rb`), but also has a fixed-workload harness with
minimal dependencies, which can be useful to debug or tune performance.

Run the fixed-workload harness with minimal dependencies with:

```bash
jt --use native ruby bench/optcarrot/fixed-workload.rb
# or
jt --use jvm-ce ruby bench/optcarrot/fixed-workload.rb
```

## Benchmarking Other Implementations

You can benchmark an entirely different implementation using the `--use` option
or with the `RUBY_BIN` environment variable.

```bash
jt --use ruby benchmark bench/classic/mandelbrot.rb --simple
RUBY_BIN=ruby jt benchmark bench/classic/mandelbrot.rb --simple
```
