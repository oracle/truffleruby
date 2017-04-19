# Using TruffleRuby with JDK 9 EA

TruffleRuby is designed to be run with a JVM that has the Graal compiler. The
easiest way to do this is to use the GraalVM, which includes a JDK, the Graal
compiler, and TruffleRuby, all in one package.

It is also possible to configure a JDK 9 early-access (EA) build to use Graal
and use that to run TruffleRuby. You need to get the latest JDK 9 EA.

https://jdk9.java.net/download/

You will also need a copy of the Graal and Truffle JARs, and these also need to
be built using JDK 9. Follow the instructions for contributors building Graal in
[Building Graal](../contributor/building-graal.md).

Then set the JAVACMD environment variable and pass other command line options to
configure Java to use Graal.

```
$ JAVACMD=.../jdk-9-ea/bin/java \
    bin/truffleruby \
    -J-XX:+UnlockExperimentalVMOptions \
    -J-XX:+EnableJVMCI \
    -J--add-exports=java.base/jdk.internal.module=com.oracle.graal.graal_core \
    -J--module-path=.../truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar:.../compiler/mxbuild/modules/com.oracle.graal.graal_core.jar \
    --no-bootclasspath -e 'p Truffle::Graal.graal?'
```
