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

Use the [CPUSampler](https://www.graalvm.org/dev/tools/profiling/) ([blog post](https://medium.com/graalvm/where-has-all-my-run-time-gone-245f0ccde853)) in flamegraph mode, like this:
```bash
ruby --cpusampler=flamegraph ...
```

This will create a `flamegraph.svg`, which you can open in a web browser
(Chromium-based browsers seem faster for rendering the flamegraph).


There is also `jt profile` which does the same but uses a unique filename, prints the wall-clock time and open the SVG for you:
Here are some examples:
```bash
jt profile -e "x = 'abc'; 100_000.times { x.upcase }"
jt profile -S gem list
```

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
