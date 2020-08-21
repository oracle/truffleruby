# User Frequently Asked Questions

### What is TruffleRuby?

TruffleRuby is a high performance implementation of the Ruby programming
language. Built on the GraalVM by Oracle Labs using the Truffle Language
Implementation Framework and the GraalVM Compiler. TruffleRuby is one part of
GraalVM, a platform for high-performance polyglot programming.

### What is Truffle?

The Truffle Language Implementation Framework is a Java framework for writing
AST interpreters. To implement a language using Truffle you write an AST for
your language and add methods to interpret -- perform the action of -- each
node.

Truffle also has the concept of specialisation. In most AST interpreters the
nodes are megamorphic -- they handle all possible types and other possible
conditions. In the Truffle Framework you write several different nodes for the
same semantic action but for different types and conditions. As runtime
conditions change, you switch which nodes you are using. After the program has
warmed up you should end up with an AST that is precisely tailored for the
types and conditions that you are actually using. If these conditions change,
you can just switch nodes again.

### What is the GraalVM Compiler?

The GraalVM Compiler is a new implementation of a just-in-time compiler (JIT
compiler, or we'd normally say *dynamic compiler*) in the OpenJDK Java Virtual
Machine. Unlike the current compilers, Graal is written in Java, and exposes a
Java API to the running program. This means that instead of emitting bytecode
a JVM language can directly control the compiler. However this is complicated,
so normally the Truffle Framework uses the GraalVM compiler on your behalf to
*partially evaluate* your AST interpreter into machine code.

### What is GraalVM?

GraalVM is the platform on which TruffleRuby runs. It's a system for
high-performance polyglot programming.

More concretely, GraalVM is a modified version of the OracleJDK that includes
the Truffle Framework, the GraalVM Compiler, TruffleRuby and other languages
supported by GraalVM including JavaScript, Python and R.

See how to [install GraalVM and TruffleRuby](installing-graalvm.md).

### How do I get TruffleRuby?

The easiest way to get TruffleRuby is [GraalVM](installing-graalvm.md).

### Why is TruffleRuby slow on a standard JVM?

The expected way to run TruffleRuby is using the GraalVM Compiler. TruffleRuby
isn't designed to be efficient on a JVM without this.

### Why is TruffleRuby faster on the GraalVM?

When running with the GraalVM Compiler, the Truffle Framework can use the API
exposed by the GraalVM Compiler. The Truffle Framework gets the bytecode
representation of all of the AST interpreter methods involved in running your
Ruby method, combines them into something like a single Java method, optimises
them together, and emits a single machine code function. The Truffle Framework
also provides wrappers for JVM functionality not normally available to Java
applications such as code deoptimization. TruffleRuby uses this to provide a
dramatically simpler and faster implementation of Ruby.

### Where did this code come from?

[Chris Seaton](https://github.com/chrisseaton) wrote an implementation of Ruby
on Truffle and Graal as part of an internship at Oracle Labs in the first half
of 2013. The code was merged into JRuby in early 2014. Benoit Daloze and Kevin
Menard joined as researchers in the second half of 2014, then Petr Chalupa in
2015, Brandon Fish in 2016 and Duncan MacGregor in 2017. Since then we have also
accepted contributions from people outside Oracle Labs. In 2017 the code was
forked back out of JRuby after it had matured.

### Who do I ask about TruffleRuby?

See the Contact section in the [README](../../README.md#contact).

### How do I know if I’m using TruffleRuby?

`RUBY_ENGINE` will be `'truffleruby'`.

### How do I know if I’m using a VM that has the GraalVM Compiler?

`ruby --version` will report `GraalVM CE` or `EE`.

`TruffleRuby.jit?` will tell you if you are also running with the GraalVM
Compiler.

### How do I know that I'm using the Community Edition of GraalVM?

`ruby --version` will report `GraalVM CE`.

### How do I know that I'm using the Enterprise Edition of GraalVM?

`ruby --version` will report `GraalVM EE`.

### How do I know that I'm using the native version of TruffleRuby?

`ruby --version` will report `Native`.

`TruffleRuby.native?` will return `true`.

### How can I see the GraalVM Compiler is working?

Put this program into `test.rb`:

```ruby
loop do
  14 + 2
end
```

We'll use the `--engine.TraceCompilation` to ask the Truffle
Framework to tell us when it compiles something using the GraalVM Compiler.

```
$ ruby --engine.TraceCompilation test.rb
[truffle] opt done         block in <main> test.rb:1 <opt> <split-3a9ffa1b>         |ASTSize       8/    8 |Time   103(  99+4   )ms |DirectCallNodes I    0/D    0 |GraalNodes    24/    3 |CodeSize           69 |CodeAddress 0x11245cf50 |Source   ../test.rb:1
```

Here you can see that Truffle has decided to use the GraalVM Compiler to
compile the block of 127 - the loop to machine code - just 69 bytes of machine
code in all.

### Why doesn’t TruffleRuby perform well for my benchmark?

Benchmarks that we haven’t looked at yet may require new code paths to be
specialized. Currently we’ve added specialisation for the code paths in the
benchmarks and applications that we’ve been using. Adding them is generally not
complicated and over time we will have specialisations to cover a broad range of
applications.

Make sure that you are using the [Enterprise Edition of GraalVM, and have rebuilt the executable images](installing-graalvm.md) for the best
performance.

### How is this related to `invokedynamic`?

TruffleRuby doesn’t use `invokedynamic`, as it doesn't emit bytecode. However it
does have an optimising method dispatch mechanism that achieves a similar
result.

### Why doesn't JRuby switch to Truffle as well?

JRuby is taking a different approach to optimising and adding new functionality
to Ruby. Both JRuby and TruffleRuby are important projects.

### Why did you fork from JRuby?

We merged into JRuby in order to be able to use large parts of their Java
implementation code. We forked back out of JRuby when we had got to the point
where the code that we were using needed to be modified for our purposes and we
no longer had any dependency on the core part of JRuby. Forking also allowed us
to simplify our code base.
