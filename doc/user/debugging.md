---
layout: docs-experimental
toc_group: ruby
link_title: Debugging Ruby
permalink: /reference-manual/ruby/Debugging/
redirect_from: /docs/reference-manual/ruby/Debugging/
---
# Debugging TruffleRuby

## Printing Exceptions

There are two ways to print exceptions, which can be useful to find the source of an error:

* the standard Ruby `-d` flag which prints the `file:line` where each exception was raised
* `--backtraces-raise` which show the full backtrace on each exception raised

Both print all exceptions even if the exceptions are later rescued.

Java exceptions can be printed with `--exceptions-print-uncaught-java` or
`--exceptions-print-java`.

See other `--backtraces-*` and `--exceptions-*` options for more possibilities.

## Printing Stacktraces and Backtraces of a Running Process

One can send the `SIGQUIT` signal to TruffleRuby to make it print the Java stacktraces of all threads.
`Ctrl + \ ` can be used to send `SIGQUIT` to the current process in a terminal.
This is useful to debug hangs and deadlocks, or to know what the process is doing.
This works on both TruffleRuby Native and JVM.

Sending `SIGALRM` to a TruffleRuby process will print the Ruby backtraces of all threads.

Note: Printing the Ruby backtraces of all threads significantly lowers performance, so it should only be used for debugging.

## More Information in Backtraces

TruffleRuby tries to match MRI's backtrace format as closely as possible.
This sometimes means that extra available information is not displayed.
When debugging you may want to see this information.

An option to show more information is `--backtraces-interleave-java=true`, which shows you the Java methods involved in executing each Ruby method.

When you are interoperating with other languages, including C extensions, backtraces for Java exceptions may be missing information, as the Java frames are gone by the time Ruby has a chance to format them into a backtrace.
