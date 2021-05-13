---
layout: docs-experimental
toc_group: ruby
link_title: Runtime Configurations
permalink: /reference-manual/ruby/RuntimeConfigurations/
redirect_from: /docs/reference-manual/ruby/RuntimeConfigurations/
---
# Deploying TruffleRuby

If you are attempting to experiment with deploying TruffleRuby to production we would encourage you to contact us so we can help you understand what is possible at the moment and to help solve any issues for you.

This document details TruffleRuby's different *runtime* configurations.

## TruffleRuby Runtime Configurations

There are two main configurations of TruffleRuby - *native* and *JVM*.
It is important to understand the different configurations of TruffleRuby, as each has different capabilities and performance characteristics.
You should pick the execution mode that is appropriate for your application.

### Native Configuration

When distributed as part of GraalVM, TruffleRuby by default runs in the *native* configuration.
In this configuration, TruffleRuby is ahead-of-time compiled to a standalone native executable.
This means that you do not need a JVM installed on your system to use it.

The advantages of the native configuration are that it [starts about as fast as MRI](https://eregon.me/blog/2019/04/24/how-truffleruby-startup-became-faster-than-mri.html), it may use less memory, and it becomes fast in less time than the *JVM*
configuration.
The disadvantages are that you can't use Java tools like VisualVM, it is less convenient for Java interoperability (see the details [here](compatibility.md#java-interoperability-with-the-native-configuration)), and *peak performance may be lower than on the JVM*.

The native configuration is used by default, but you can also request it using `--native`.
To use polyglot programming with the *native* configuration, you need to pass the `--polyglot` flag.

### JVM Configuration

TruffleRuby can also be used in the *JVM* configuration, where it runs as a normal Java application on the JVM.
The advantages of the JVM configuration are that you can use Java interoperability easily, and *peak performance may be higher than the native configuration*.
The disadvantages are that it takes much longer to start and to get fast, and may use more memory.
You can select the JVM configuration by passing `--jvm`.

## Selecting the Best Configuration

If you are running a short-running program you probably want the default, *native*, configuration.
If you are running a long-running program and want the highest possible performance you probably want the *JVM* configuration, by using `--jvm`.

### Getting the Best Startup Time Performance

To get the best startup time in most cases you want to use the native configuration, which is the default.

### Getting the Lowest Memory Footprint

To get the lowest memory footprint you probably initially want to use the native configuration, but as you get a larger working set of objects you may find that the simpler garbage collector and current lack of compressed ordinary object pointers (OOPS) actually increases your memory footprint and you will be better off with the JVM configuration using `--jvm` to reduce memory use.

### Getting the Best Peak Performance from TruffleRuby

To get the best peak performance from TruffleRuby for longer-running applications we would in most cases recommend the JVM configuration with `--jvm`.

However to reach this peak performance you need to *warm-up* TruffleRuby, as you do with most heavily optimising virtual machines.
This is done by running the application under load for a period of time.
If you monitor the performance (by measuring operation time or response time) you will see it reduce over time and then probably stabilise.

## Logging

Ruby application logging and warning works as in the standard implementation of Ruby.

For logging of TruffleRuby internals, standard Java logging is used.
The logging level can be set with `--log.level=INFO`, `=FINEST`, or so on.
