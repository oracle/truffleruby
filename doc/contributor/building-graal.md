# Building Graal

The easiest way to get Graal to develop TruffleRuby is to use the GraalVM, as
described in `doc/user/using-graalvm.md`.

If you want to build Graal from scratch, possibly to modify Graal or Truffle, to
use developer tools that come with Graal such as the Ideal Graph Visualizer,
then you should follow the instructions in the Graal core repository.

https://github.com/graalvm/graal-core

You can then set the `GRAAL_HOME` environment variable to the location of the
`graal-core` checkout and use `jt` to run TruffleRuby.

```
$ GRAAL_HOME=..../graal-core tool/jt.rb run --graal ...
```
