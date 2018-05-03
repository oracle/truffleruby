# Using TruffleRuby with JDK 10

TruffleRuby is designed to be run with a JVM that has the Graal compiler. The
easiest way to do this is to use GraalVM.

It is also possible to run on Java 10, but we do not support this and at the
moment we only document it for interest.

To run on Java 10, Graal needs to be built with Java 10. As there is no binary
distribution of GraalVM based on Java 10 you will need to
[build Graal yourself](../contributor/building-graal.md), using Java 10 as the
JDK (use the `jt install graal --no-jvmci` option to tell it to use your system
Java 10). You should also build TruffleRuby with the same JDK.

```
$ GRAAL_REPO=...path to graal repository built with JDK 10...
$ bin/truffleruby \
    -J-XX:+UnlockExperimentalVMOptions \
    -J-XX:+EnableJVMCI \
    -J--module-path=$GRAAL_REPO/sdk/mxbuild/modules/org.graalvm.graal_sdk.jar:$GRAAL_REPO/truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar \
    -J--upgrade-module-path=$GRAAL_REPO/compiler/mxbuild/modules/jdk.internal.vm.compiler.jar \
    -e 'p Truffle.graal?'
```

This should print `true` to say that compilation with Graal is enabled.

As another check you can run a loop and see that it is compiled.

```
$ bin/truffleruby \
    -J-XX:+UnlockExperimentalVMOptions \
    -J-XX:+EnableJVMCI \
    -J--module-path=$GRAAL_REPO/sdk/mxbuild/modules/org.graalvm.graal_sdk.jar:$GRAAL_REPO/truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar \
    -J--upgrade-module-path=$GRAAL_REPO/compiler/mxbuild/modules/jdk.internal.vm.compiler.jar \
    -J-Dgraal.TraceTruffleCompilation=true \
    -e 'loop { 14 + 2}'
[truffle] opt done ......
```
