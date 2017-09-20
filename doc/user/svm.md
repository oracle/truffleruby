# Using TruffleRuby with the SVM

The Substrate Virtual Machine, or SVM, is a closed- and whole-world analysis
ahead-of-time compiler for Java, and an implementation of parts of a JVM.

Using the SVM it is possible to ahead-of-time compile TruffleRuby and the Graal
dynamic compiler to a single, statically linked native binary executable, that
has no dependency on a JVM, and does not link to any JVM libraries. The
technique is more sophisticated than just appending a JAR as a resource in a
copy of the JVM - only parts of the JVM which are needed are included and they
are specialised for how TruffleRuby uses them. There is no Java bytecode - only
compiled native machine code and compiler graphs for dynamic compilation.

Note that a common confusion is that the SVM is an ahead-of-time compiler for
the Java code that implements the TruffleRuby interpreter and the Graal
compiler, not an ahead-of-time compiler for your Ruby program.

The SVM itself, like Graal and TruffleRuby, is implemented in Java.

https://youtu.be/FJY96_6Y3a4?t=10023

More information can be found in Kevin's [blog post](http://nirvdrum.com/2017/02/15/truffleruby-on-the-substrate-vm.html).

To use the SVM you need a release of GraalVM, as described in
[Using GraalVM](using-graalvm.md). You will also need `gcc` and the `zlib`
headers. On Ubuntu, Debian, etc:

```
apt-get install gcc zlib1g-dev
```

You can then run:

```
$ graalvm/bin/native-image --ruby
```

This command will take a few minutes to run, and it requires about 6 GB of
memory so don't run it in an instance or machine with less than 8 GB
of RAM.

This gives you a native binary that implements Ruby, similar to the MRI or
Rubinius executables. This binary is subject to the same OTN licence as
the GraalVM distribution.

The binary doesn't need a JVM:

```
$ otool -L ruby
ruby:
	/System/Library/Frameworks/CoreFoundation.framework/Versions/A/CoreFoundation (compatibility version 150.0.0, current version 1348.28.0)
	/usr/lib/libSystem.B.dylib (compatibility version 1.0.0, current version 1238.0.0)
	/usr/lib/libz.1.dylib (compatibility version 1.0.0, current version 1.2.8)

$ du -h ruby
144M	ruby
```

You should set `-Xhome=` when running an SVM build of TruffleRuby - it can't yet
work out where the standard library is located otherwise.

The SVM version of TruffleRuby has better startup performance and lower memory
footprint than TruffleRuby or JRuby on the JVM, and better startup performance
than Rubinius. We expect these numbers to improve significantly in the future as
we ahead-of-time compile more of the Ruby startup process, and aim to meet or
beat MRI's startup time.

| Implementation | Real Time (s) | Max RSS (MB) |
| -------------- | ------------: | -----------: |
| TruffleRuby SVM | 0.10 | 97 |
| TruffleRuby JVM | 2.86 | 272 |
| JRuby 9.1.13.0 | 1.44 | 154 |
| MRI 2.4.2 | 0.03 | 8 |
| Rubinius 3.84 | 0.25 | 65 |

Run on Linux with an Intel(R) Core(TM) i7-4702HQ CPU @ 2.20GHz.

```
$ export TIME="%e %M"

$ cd graalvm-0.28
$ /usr/bin/time bin/ruby --native -e 'puts "Hello"'  # TruffleRuby on the SVM
Hello
0.10 99764

$ /usr/bin/time bin/ruby --jvm -e 'puts "Hello"'  # TruffleRuby on the JVM
Hello
2.86 278240

$ chruby jruby-9.1.13.0
$ /usr/bin/time jruby -e 'puts "Hello"'
Hello
1.44 158080

$ chruby ruby-2.4.2
$ /usr/bin/time ruby -e 'puts "Hello"'
Hello
0.03 8672

$ chruby rbx-3.84
$ /usr/bin/time rbx -e 'puts "Hello"'
Hello
0.25 66824
```

(The first number `real` is the number of actual seconds which have elapsed while the command
runs, and the second `maximum resident set size` is the maximum amount of memory occupied while the command runs)
