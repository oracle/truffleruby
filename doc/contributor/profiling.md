# Profiling TruffleRuby

There is no shortage of tools for profiling TruffleRuby. When running in JVM
mode, we can use standard JVM tooling, such as VisualVM and Java Flight
Recorder. When run as a Native Image we can use callgrind from the Valgrind
tool suite and other system tools, such as strace. As a GraalVM language we
can also use other GraalVM [tools](../user/tools.md). For a broad enough
definition of profiling, we can also use the Ideal Graph Visualizer (IGV) and
C1 Visualizer to inspect Graal's output.

This document is less about how to use each tool and more about suggestions for extracting
the most useful information from the tools, assuming basic knowledge of their usage.

### Creating a Flame Graph

#### The Easy Way

The easiest way to generate a flame graph from a profile is to accept the default profiling
options configured in _jt_. In this case, simply run `jt profile` like you would `jt ruby`
and your application or inline script will be profiled and an SVG written to your
TruffleRuby source directory, named _profiles/flamegraph\_\<timestamp\>.svg_.

To make things even easier, `jt profile` will automatically open up the flame graph for
you in whatever application you have configured as your default for SVG files. This is
handled by the `open` command on macOS and the `xdg-open` command on Linux.

To illustrate, here's a command for profiling and inline script.

```bash
jt profile -e "x = 'abc'; 100_000.times { x.upcase }"
```

Here's a command for profiling the `gem list` command:

```bash
jt profile -S gem list
```

#### The Less Easy Way

The histogram output from the Truffle profiler can be quite large, making it difficult to
analyze. Additionally, as a flat format it isn't possible to analyze a call graph as that
information simply isn't encoded in the output. A flame graph shows the entire call graph
and its structure makes it considerably simpler to see where the application time is being
spent.

Creating the flame graph is a multi-stage process. First, we need to profile the application
with the JSON formatter:

```bash
jt ruby --cpusampler --cpusampler.SampleInternal --cpusampler.Mode=roots --cpusampler.Output=json -e 'p :hello' > simple-app.json
```

Since we want to profile the TruffleRuby runtime itself, we use the
`--cpusampler.SampleInternal=true` option. The `--cpusampler.Mode=roots` option will
sample roots, including inlined functions, which can often give a better idea of what
is contributing to the overall method execution time.

The JSON profiler formatter encodes call graph information that isn't available in the
histogram format. To make a flame graph out of this output, however, we need to transform
it into a format that folds the call stack samples into single lines. This can be done
using [stackcollapse-graalvm.rb](https://github.com/eregon/FlameGraph/blob/graalvm/stackcollapse-graalvm.rb)
from Benoit's fork of FlameGraph.

If you haven't yet, you should clone Benoit's [fork of FlameGraph](https://github.com/eregon/FlameGraph/tree/graalvm)
into TruffleRuby's parent directory. Now you can run the script to transform the output and
pipe it into the script that will generate the SVG data:

```bash
../FlameGraph/stackcollapse-graalvm.rb simple-app.json | ../FlameGraph/flamegraph.pl > simple-app.svg
```

At this point, you should open the SVG file in a Chromium-based web browser. Your system
might have a different image manipulation application configured as the default application
for SVG files. While loading the file in such an application make render a graph, it likely
will not handle the interactive components of the flame graph. Firefox may work as well,
but Chromium-based browsers seem to have better support and performance for the flame graph
files as of this writing (Dec. 2018).

### Require Profiling
The `--metrics-profile-require` option can be used to profile the time used for searching, parsing, translating and loading files during require.

For example, the `summary` view provides an overview of where time is spent:
```
$ jt ruby --experimental-options --cpusampler --metrics-profile-require=summary -e 'require "rubygems"' |& grep "metrics "
 metrics execute                                                      |       1122ms  99.6% |   0.0% ||        212ms  18.8% |   0.0% | (metrics)~1:0 
 metrics parsing                                                      |         71ms   6.3% |   0.0% ||         71ms   6.3% |   0.0% | (metrics)~1:0 
 metrics translating                                                  |         60ms   5.3% |   0.0% ||         60ms   5.3% |   0.0% | (metrics)~1:0 
 metrics require                                                      |       1123ms  99.7% |   0.0% ||         37ms   3.3% |   0.0% | (metrics)~1:0 
 metrics searching                                                    |          6ms   0.5% |   0.0% ||          6ms   0.5% |   0.0% | (metrics)~1:0 
```

This feature can also be used to generate a flame graph with detailed require timings:

`$ jt profile --metrics-profile-require=detail -e 'require "rubygems"'`
