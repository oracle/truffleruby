# Building the GraalVM Compiler

If you want to build the Truffle Language Implementation Framework and the
GraalVM Compiler and other dependencies from scratch to test running with them
when developing TruffleRuby, or possibly to modify them, or to use developer
tools that come with Graal such as the Ideal Graph Visualizer, then you should
install Graal locally.

## Installing Graal with jt

Our workflow tool can install Graal automatically under the directory
`../graal/compiler` with:

```
$ jt install graal
```

You can then use `jt` to run TruffleRuby with Graal.

```
$ jt ruby --graal ...
```

## Building Graal manually

If you want to build the GraalVM Compiler by yourself, follow the instructions
in the Graal repository.

https://github.com/oracle/graal

You can then set the `GRAAL_HOME` environment variable to the location of the
`compiler` directory inside the `graal` repository and use `jt` to run
TruffleRuby.

```
$ GRAAL_HOME=.../graal/compiler jt ruby --graal ...
```
