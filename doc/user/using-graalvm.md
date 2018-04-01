# Using TruffleRuby with GraalVM

TruffleRuby is designed to be run with a JVM that has the Graal compiler.

The easiest way to get a JVM with Graal is via the GraalVM, available from the
Oracle Technology Network. This includes the JVM, the Graal compiler, and
TruffleRuby, all in one package with compatible versions. You normally want
the version that includes the Labs JDK.

http://www.oracle.com/technetwork/oracle-labs/program-languages/

Inside the GraalVM is a `jre/languages/ruby` directory which has the usual
structure of a Ruby implementation. It is recommended to add this directory to
a Ruby manager, see [configuring Ruby managers](ruby-managers.md) for more
information.

By default, GraalVM runs TruffleRuby in the native configuration, which is best
for short-running processes, but it can also run it on top of the JVM which is
best for getting the highest peak performance and for Java interopability. Use
`--jvm` to use the JVM configuration. See the [readme](../../README.md) for
more information on different configurations.

You can also use GraalVM to run a different version of TruffleRuby than the one
it packages, but this not advised for end-users.
