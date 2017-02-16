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

To use the SVM you need a release of GraalVM, as described in
[Using GraalVM](using-graalvm.md). You can then run:

```
$ graalvm-0.20/bin/aot-image --ruby
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
| TruffleRuby SVM | 0.40 | 139 |
| TruffleRuby JVM | 5.03 | 442 |
| JRuby 9.1.7.0 | 2.25 | 191 |
| MRI 2.4.0 | 0.03 | 8 |
| Rubinius 3.60 | 0.61 | 64 |

```
$ /usr/bin/time -l ./ruby -Xhome=language/ruby -e "puts 'hello'"  # TruffleRuby on the SVM
hello
        0.40 real         0.16 user         0.07 sys
 145813504  maximum resident set size
 
$ /usr/bin/time -l graalvm-0.20/bin/ruby -e "puts 'hello'"  # TruffleRuby on the JVM
hello
        5.03 real        10.84 user         1.85 sys
 463806464  maximum resident set size
 
$ /usr/bin/time -l jruby-9.1.7.0/bin/jruby -e "puts 'hello'"
hello
       2.25 real         5.27 user         0.30 sys
200794112  maximum resident set size
 
$ /usr/bin/time -l 2.4.0/bin/ruby -e "puts 'hello'"
hello
        0.03 real         0.02 user         0.00 sys
   8773632  maximum resident set size

$ /usr/bin/time -l rbx-3.60/bin/ruby -e "puts 'hello'"
hello
       0.61 real         0.32 user         0.20 sys
  66744320  maximum resident set size
```

(`real` is the number of actual seconds which have elapsed while the command runs, `resident set size` is the total memory occupied while the command runs)
