# Using TruffleRuby with JDK 9

TruffleRuby is designed to be run with a JVM that has the Graal compiler. The
easiest way to do this is to use the GraalVM, which includes JDK 8, the Graal
compiler, and TruffleRuby, all in one package.

It is also possible to run on Java 9, but Graal needs to be built with Java 9
and there is no binary distribution of GraalVM based on Java 9 so you will
need to [build Graal yourself](../contributor/building-graal.md), using Java
9 as the JDK. TruffleRuby itself can be built with Java 8 as normal.

```
$ GRAAL_HOME=...path to graal repository built with JDK 9...
$ export JAVACMD=...java 9...
$ bin/truffleruby \
    -J-XX:+UnlockExperimentalVMOptions \
    -J-XX:+EnableJVMCI \
    -J--module-path=$GRAAL_HOME/sdk/mxbuild/modules/org.graalvm.graal_sdk.jar:$GRAAL_HOME/truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar:$GRAAL_HOME/compiler/mxbuild/modules/jdk.internal.vm.compiler.jar \
    -e 'p Truffle.graal?'
```

This should print `true` to say that compilation with Graal is enabled.

As another check you can run a loop and see that it is compiled.

```
$ bin/truffleruby \
    -J-XX:+UnlockExperimentalVMOptions \
    -J-XX:+EnableJVMCI \
    -J--module-path=$GRAAL_HOME/sdk/mxbuild/modules/org.graalvm.graal_sdk.jar:$GRAAL_HOME/truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar:$GRAAL_HOME/compiler/mxbuild/modules/jdk.internal.vm.compiler.jar \
    -J-Dgraal.TraceTruffleCompilation=true \
    -e 'loop { 14 + 2}'
[truffle] opt done ......
```
