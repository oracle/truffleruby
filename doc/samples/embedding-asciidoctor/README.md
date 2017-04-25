# Embedding Sample

TruffleRuby supports embedding in a Java application, using the `PolglotEngine`
API. This is a Truffle feature, so it works the same as for other Truffle
languages such as JavaScript and R.

There aren't conventional Maven releases of TruffleRuby at the moment, but you
can compile a Java program against the JARs included in GraalVM.

http://www.oracle.com/technetwork/oracle-labs/program-languages/

This sample runs the Ruby gem `asciidoctor` from Java which formats Asciidoc
files.

First, clone `asciidoctor`

```bash
$ git clone https://github.com/asciidoctor/asciidoctor.git
$ pushd asciidoctor
$ git checkout v1.5.5
$ popd
```

Then compile the Java program using GraalVM.

```bash
$ .../graalvm/bin/javac -classpath .../graalvm/lib/truffle/truffle-api.jar Asciidoctor.java
```

And then run the Java program. We need to put the `asciidoctor/lib` directory on
the load path, but we don't have a Ruby command line. Instead, we use the
`load_paths` option, which as a Java system property is written
`truffleruby.load_paths`.

```bash
$ cp asciidoctor/benchmark/sample-data/mdbasics.adoc sample.adoc
$ .../graalvm/bin/java -polyglot -classpath . -Dtruffleruby.load_paths=asciidoctor/lib Asciidoctor sample.adoc
$ open sample.html
```
