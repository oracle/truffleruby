# Deploying TruffleRuby

If you're attempting to experiment with deploying TruffleRuby to production we'd
encourage you to contact us so we can help you understand what is possible at
the moment and to help solve any issues for you.

## TruffleRuby Configurations

There are two main configurations of TruffleRuby - *native* and *JVM*. It's
important to understand the different configurations of TruffleRuby, as each has
different capabilities and performance characteristics. You should pick the
execution mode that is appropriate for your application.

When distributed as part of GraalVM, TruffleRuby by default runs in the
*native* configuration. In this configuration, TruffleRuby is ahead-of-time
compiled to a standalone native executable. This means that you don't need a
JVM installed on your system to use it. The advantage of the native
configuration is that it starts about as fast as MRI, it may use less memory,
and it becomes fast in less time than the *JVM* configuration. The
disadvantage of the native configuration is that you can't use Java tools like
VisualVM, it is less convenient for Java interoperability, and *peak performance may be
lower than on the JVM*. The native configuration is used by default, but you
can also request it using `--native`. To use polyglot programming with the
*native* configuration, you need to use the `--polyglot` flag.

TruffleRuby can also be used in the *JVM* configuration, where it runs as a
normal Java application on the JVM, as any other Java application would. The
advantage of the JVM configuration is that you can use Java interoperability easily, and
*peak performance may be higher than the native configuration*. The disadvantage
of the JVM configuration is that it takes much longer to start and to get fast,
and may use more memory. The JVM configuration is requested using `--jvm`.

If you are running a short-running program you probably want the default,
*native*, configuration. If you are running a long-running program and want the
highest possible performance you probably want the *JVM* configuration, by using
`--jvm`.

### Getting the best startup time performance

To get the best startup time performance in most cases you want to use the
native configuration, which is the default.

### Getting the lowest memory footprint

To get the lowest memory footprint you probably initially want to use the native
configuration, but as you get a larger working set of objects you may find that
the simpler garbage collector and current lack of compressed ordinary object
pointers (OOPS) actually increases your memory footprint and you'll be better
off with the JVM configuration using `--jvm` to reduce memory use.

### Getting the best peak performance from TruffleRuby

To get the best peak performance from TruffleRuby for longer-running
applications we would in most cases recommend the JVM configuration with
`--jvm`.

However to reach this peak performance you need to *warm-up* TruffleRuby, as you
do with most heavily optimising virtual machines. This is done by running the
application under load for a period of time. If you monitor the performance (by
measuring operation time, or response time) you will see it reduce over time and
then probably stabilise.

## Logging

Ruby application logging and warning works as in the standard implementation of
Ruby.

For logging of TruffleRuby internals, standard Java logging is used. The logging
level can be set with `--log.level=INFO`, `=FINEST`, or so on.
