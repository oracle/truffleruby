# Debugging TruffleRuby

You've got many options for debugging the implementation of TruffleRuby.

If you want to debug an application running on TruffleRuby, and don't suspect
any bug in TruffleRuby itself, use GraalVM [tools](../user/tools.md) as usual.

## Print Debugging

`System.err.println(...);` in Java and `p` in Ruby is a great first tool to use for
debugging. You can add them easily, and modifications to the core library in
Ruby don't even require any recompilation if you're running via `jt ruby` in
the JVM runtime configuration.

When you're in Java, you run arbitrary Ruby code, including `p`, using for
example `DebugHelpers.eval("p a", "a", a)`. You can print a Ruby backtrace
from Java using `DebugHelpers.eval("puts nil, caller, nil")`.

## IntelliJ IDEA Debugging

Run Ruby with `jt ruby --jdebug ...` and Ruby will wait for you to attach
the IntelliJ IDEA debugger. When running some other commands like `jt test`,
`--jdebug` might need be passed after like `jt test some/spec -- --jdebug`.
See `jt help` for where `ruby options` are passed.
You can attach the IntelliJ debugger by using *Run*, *Debug 'GraalDebug'*,
which is a pre-configured debugging profile. TruffleRuby should then continue to
run as normal.

You can then pause at any point, and inspect the Java stack trace and variables.
Use *Run*, *Debugging Actions*, *Evaluate Expression...*, to get a Java REPL.
Remember that you can use code like `DebugHelpers.eval("p a", "a", a)` to effectively
get a Ruby REPL within this Java REPL. You can also set breakpoints, and
conditional breakpoints, both while the program is already running and also
ahead of time. Conditional breakpoints can use `DebugHelpers` to break based on
Ruby conditions, but this may be very slow as it'll all be interpreted.

It is often useful to get a Ruby backtrace to find where the interpreter is in the Ruby code.
This can be done conveniently by right-clicking the `Variables` pane of the debugger, then
`New Watch...` and paste `org.truffleruby.language.backtrace.BacktraceFormatter.printableRubyBacktrace(this)`.
You can then click `View` to see the Ruby backtrace at any time, and also copy/paste that output to
`Analyze` => `Stack Trace or Thread Dump...` to be able to simply click on backtrace entries
and make IntelliJ open the file in the IDE.

A common problem is that the first thing TruffleRuby does when it starts
is to execute a lot of Ruby code to load the core library. If you set a
breakpoint it's likely to be triggered while loading the core, rather than
when in your test code. To get around this if that's not what you want, we can
set a breakpoint in `RubyLanguage#applicationStarts`, and when that breakpoint is reached,
then set or enable the breakpoints that we want for our application code.

Generally, IDEA has a very powerful debugger that you may not expect if you're
coming from the perspective of a Ruby developer, and you should explore it and
see what you're able to do beyond this.
