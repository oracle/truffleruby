---
layout: docs-experimental
toc_group: ruby
link_title: Debugging Ruby
permalink: /reference-manual/ruby/Debugging/
---
# Debugging TruffleRuby

TruffleRuby, like other GraalVM languages, supports 2 standard debugging protocols:
* the [Debug Adapter Protocol (DAP)](https://www.graalvm.org/latest/tools/dap/), best supported
* the [Chrome DevTools Protocol](https://www.graalvm.org/latest/tools/chrome-debugger/), limited support because the protocol does not handle threads

Also see [Tools](tools.md) for more tools besides just debuggers.

## VSCode

First install [the GraalVM VSCode extension](https://marketplace.visualstudio.com/items?itemName=oracle-labs-graalvm.graalvm).

Then follow [this documentation](https://www.graalvm.org/latest/tools/vscode/graalvm-extension/polyglot-runtime/#debugging-ruby) to debug TruffleRuby with VSCode.

## RubyMine

Unfortunately RubyMine / IntelliJ IDEA do not support the Debug Adapter Protocol yet for Ruby debugging.

Please vote or comment on the [feature request](https://youtrack.jetbrains.com/issue/RUBY-30772) to share your interest.

## Command-line Debugging Options

### Printing Exceptions

There are two ways to print exceptions, which can be useful to find the source of an error:

* the standard Ruby `-d` flag which prints the `file:line` where each exception was raised
* `--backtraces-raise` which show the full backtrace on each exception raised

Both print all exceptions even if the exceptions are later rescued.

Java exceptions can be printed with `--exceptions-print-uncaught-java` or
`--exceptions-print-java`.

See other `--backtraces-*` and `--exceptions-*` options for more possibilities.

### Printing Stacktraces and Backtraces of a Running Process

One can send the `SIGQUIT` signal to TruffleRuby to make it print the Java stacktraces of all threads.
`Ctrl + \ ` can be used to send `SIGQUIT` to the current process in a terminal.
This is useful to debug hangs and deadlocks, or to know what the process is doing.
This works on both TruffleRuby Native and JVM.

Sending `SIGALRM` to a TruffleRuby process will print the Ruby backtraces of all threads.

Note: Printing the Ruby backtraces of all threads significantly lowers performance, so it should only be used for debugging.

### More Information in Backtraces

TruffleRuby tries to match MRI's backtrace format as closely as possible.
This sometimes means that extra available information is not displayed.
When debugging you may want to see this information.

An option to show more information is `--backtraces-interleave-java=true`, which shows you the Java methods involved in executing each Ruby method.

When you are interoperating with other languages, including C extensions, backtraces for Java exceptions may be missing information, as the Java frames are gone by the time Ruby has a chance to format them into a backtrace.

### Printing Subprocesses

You can log subprocesses created by TruffleRuby using the option `--log-subprocess`.

```bash
$ ruby --log-subprocess -e '`ls .`'
[ruby] INFO: spawn: ls .
```

This is not transitive though, unless you set this option in `TRUFFLERUBYOPT`.

### Printing TruffleRuby Processes and Arguments

You can log TruffleRuby processes created using the `bin/truffleruby` launcher and their arguments with `--log-process-args`.

```bash
$ ruby --log-process-args -e 0
[ruby] INFO: new process: truffleruby --log-process-args -e 0
```

You can set this option in `TRUFFLERUBYOPT` to make it apply to TruffleRuby subprocess as well.
Separate log files will be used for different subprocesses running at the same time when using `--log.file=PATH`.
These log files start with the same path but end with `1`, `2`, etc suffixes.
