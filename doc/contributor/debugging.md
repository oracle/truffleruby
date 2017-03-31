# Debugging TruffleRuby

## More information in backtraces

We try to match MRI's backtrace format as closely as possible. This often means
that we don't display extra information that we actually do have available.
When debugging you may want to see this information.

Two options to show more information are `-Xbacktraces.hide_core_files=false`
which doesn't hide methods that are used to implement the core library, and
`-Xbacktraces.interleave_java=true` which shows you the Java methods involved in
executing each Ruby method.
