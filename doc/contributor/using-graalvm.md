# Using TruffleRuby with GraalVM for Contributors

The easiest way for end-users to get the Graal compiler and a compatible JVM is
with the GraalVM. We recommend that they use the version of TruffleRuby which is
bundled in GraalVM, but for contributors GraalVM is also a convenient way to get
everything else.

Follow the instructions for end-users in `doc/user/using-graalvm.md`, but then
set the environment variable `GRAALVM_BIN` to the `bin/java` executable in
GraalVM and use `jt` to run TruffleRuby.

```
$ GRAALVM_BIN=..../bin/java tool/jt.rb run --graal ...
```
