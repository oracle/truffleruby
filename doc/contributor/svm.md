# Using TruffleRuby with the SVM

The Substrate Virtual Machine, or SubstrateVM, is a closed- and whole-world
analysis ahead-of-time compiler for Java, and an implementation of parts of a
JVM.

We use the SVM to ahead-of-time compile TruffleRuby and the Graal dynamic
compiler to a single, statically-linked native binary executable, that has no
dependency on a JVM, and does not link to any JVM libraries. The technique is
more sophisticated than just appending a JAR as a resource in a copy of the JVM -
only parts of the JVM which are needed are included and they are specialised for
how TruffleRuby uses them. There is no Java bytecode - only compiled native
machine code and compiler graphs for dynamic compilation.

Note that a common confusion is that the SVM is an ahead-of-time compiler for
the Java code that implements the TruffleRuby interpreter and the Graal
compiler, not an ahead-of-time compiler for your Ruby program.

The SVM itself, like Graal and TruffleRuby, is implemented in Java.

https://youtu.be/FJY96_6Y3a4?t=10023

More information can be found in Kevin's
[blog post](http://nirvdrum.com/2017/02/15/truffleruby-on-the-substrate-vm.html).

The TruffleRuby that is distributed in the [GraalVM](../user/installing-graalvm.md)
by default uses a version compiled using SVM - this is since version 0.33 of
GraalVM and was changed to prioritise fast start-up and warm-up time for shorter
running commands and benchmarks.

```bash
$ cd graalvm
$ otool -L jre/bin/ruby
jre/bin/ruby:
	/System/Library/Frameworks/CoreFoundation.framework/Versions/A/CoreFoundation (compatibility version 150.0.0, current version 1348.28.0)
	/usr/lib/libSystem.B.dylib (compatibility version 1.0.0, current version 1238.0.0)
	/usr/lib/libz.1.dylib (compatibility version 1.0.0, current version 1.2.8)

$ du -h jre/bin/ruby
200M	jre/bin/ruby
```

The SVM version of TruffleRuby has better startup performance and lower memory
footprint than JRuby, Rubinius and TruffleRuby on the JVM, and similar or better
startup performance than MRI.

| Implementation | Real Time (s) | Max RSS (MB) |
| -------------- | ------------: | -----------: |
| TruffleRuby SVM | 0.024 |  65 |
| TruffleRuby JVM | 1.890 | 333 |
| MRI 2.5.1       | 0.043 |   9 |
| JRuby 9.1.16.0  | 1.212 | 154 |
| Rubinius 3.100  | 0.137 |  70 |

Run on Linux with an Intel(R) Core(TM) i7-7700HQ CPU @ 2.80GHz with a SSD.

```bash
$ cd graalvm-0.33
$ bin/ruby -e 'puts "Hello"'       # TruffleRuby on the SVM
$ bin/ruby --jvm -e 'puts "Hello"' # TruffleRuby on the JVM

$ chruby jruby-9.1.16.0
$ jruby -e 'puts "Hello"'

$ chruby ruby-2.5.1
$ ruby -e 'puts "Hello"'

$ chruby rbx-3.100
$ rbx -e 'puts "Hello"'
```

The real time and the maximum resident set size are measured with a custom
[C program](https://gist.github.com/eregon/cbf6c89451ecf815463c00aef9745837)
as `time` cannot measure real time in milliseconds and max RSS.
Each command is run 10 times and the average is reported.
`clock_gettime(CLOCK_MONOTONIC)` is used to measure time and `getrusage()` to
measure max RSS.

There is no need to do so, but you can actually also compile your own copy of
the SVM version of TruffleRuby using a tool distributed as part of GraalVM and
the Java version of TruffleRuby from GraalVM.

```
$ native-image -H:Name=native-ruby --language:ruby
```

`native-ruby` is the output file name.

You can build a native image using SVM from a source distribution using:

```
$ jt build native
```

The disadvantages of the SVM version of TruffleRuby are:

* It has lower peak performance, as the GC is simpler and some optimisations
  such as compressed ordinary object pointers (OOPS) are not yet available.
* You can't use Java interoperability.
* You can't use standard Java tools like VisualVM.

So the SVM version may not be appropriate for all uses.

For the highest performance for long-running processes you want to use the
JVM version of TruffleRuby, using `--jvm`.
