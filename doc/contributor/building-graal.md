# Building the GraalVM Compiler

If you want to build the Truffle Language Implementation Framework and the
GraalVM Compiler and other dependencies from scratch to test running with them
when developing TruffleRuby, or possibly to modify them, or to use developer
tools that come with Graal such as the Ideal Graph Visualizer, then you should
install Graal locally.

## Building Graal with jt

Our workflow tool can build Graal automatically with:

```bash
$ jt build --graal
```

You can then use `jt` as normal to run TruffleRuby with Graal.

```bash
$ jt ruby ...
```
