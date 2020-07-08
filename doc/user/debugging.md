# Debugging TruffleRuby

## Printing Exceptions

There are two ways to print exceptions, which can be useful to find the source of an error:

* the standard Ruby `-d` flag which print the `file:line` where each exception was raised.
* `--backtraces-raise` which show the full backtrace on each exception raised.

Both print all exceptions even if the exceptions are later rescued.

Java exceptions can be printed with `exceptions-print-uncaught-java` or
`--exceptions-print-java`.

See other `--backtraces-*` and `--exceptions-*` options for more possibilities.

## More Information in Backtraces

We try to match MRI's backtrace format as closely as possible. This sometimes means
that we don't display extra information that we actually do have available.
When debugging you may want to see this information.

An option to show more information is `--backtraces-interleave-java=true`
which shows you the Java methods involved in executing each Ruby method.

When you are interoperating with other languages, including C extensions,
backtraces for Java exceptions may be missing information, as the Java frames
are gone by the time Ruby has a chance to format them into a backtrace.
