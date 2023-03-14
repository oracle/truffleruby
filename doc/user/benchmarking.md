---
layout: docs-experimental
toc_group: ruby
link_title: Benchmarking TruffleRuby
permalink: /reference-manual/ruby/Benchmarking/
---
# Benchmarking TruffleRuby

This document lists the most important points to consider when benchmarking TruffleRuby.

## Guidelines for Benchmarking TruffleRuby

We expect anyone publishing benchmark numbers about TruffleRuby to follow these guidelines.

### Use TruffleRuby EE

Use TruffleRuby EE, it is faster than CE overall and represents what TruffleRuby is capable of.

### Use the Latest Release

Always use the latest release at the time of benchmarking (so it does not misrepresent TruffleRuby by using an old release which may have known performance issues).

### Use the Correct Runtime Configuration

TruffleRuby has two Runtime Configurations, Native and JVM, see [this comparison](../../README.md#truffleruby-runtime-configurations).

If you want to benchmark peak performance, you should use the JVM configuration.
To do so, set the environment variable `TRUFFLERUBYOPT=--jvm` so it affects all TruffleRuby processes.
You can also pass `--jvm` as an argument to TruffleRuby if you are sure there are no subprocesses.

The Native configuration provides better startup and warmup but has slower peak performance.

Of course you can also benchmark both configurations and see which one is better for what you are benchmarking.

### Consider Disabling the Global C-Extension Lock

On TruffleRuby, C extensions by default use a global lock for maximum compatibility with CRuby.
If you are benchmarking a multi-threaded Ruby program (e.g. Rails on a multi-threaded server), it is worth trying
`TRUFFLERUBYOPT="--experimental-options --cexts-lock=false"`.
[This issue](https://github.com/oracle/truffleruby/issues/2136) tracks a way to automatically not use the lock for extensions which do not need it.

## Recommendations

These are more general recommendations about benchmarking.

### Avoid Benchmarking on a Laptop

Performance on laptops is very sensitive to heat, and so overall quite unstable.
As an example, if the CPU gets too warm the operating system will throttle it, making the benchmark results unfair and unstable.
So benchmarking should be done on on a desktop computer or server.

### Avoid Other Running Processes

As those would cause extra noise in benchmarking results.
Definitely no browser, slack, IDE, etc as those use a lot of CPU.

### Disable Frequency Scaling

CPU frequency scaling and boost generally just increases noise in benchmarking results,
so it is recommended to disable them when benchmarking for more stable results.

For Intel CPUs use:

```bash
sudo sh -c 'echo 1 > /sys/devices/system/cpu/intel_pstate/no_turbo'
```

For AMD CPUs use:

```bash
sudo sh -c 'echo 0 > /sys/devices/system/cpu/cpufreq/boost'
```

Also make sure the performance governor is used on Linux:

```bash
sudo cpupower frequency-set -g performance
cpupower frequency-info
```

### Do not pin TruffleRuby to a Single Core

TruffleRuby uses multiple threads for the JIT Compiler, the GC, etc.
Restricting it to a single core for benchmarking does not make sense, it would cause a lot of contention.

### Avoid Benchmarking on macOS

macOS's memory management is sub-par and can cause unnecessary memory swapping even when there is enough memory.

macOS's TCP stack is also sub-par, see the [Passenger docs](https://www.phusionpassenger.com/library/config/apache/optimization/#operating-system-recommendations) on this subject.
