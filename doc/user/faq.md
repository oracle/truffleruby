# User Frequently Asked Questions

### What is TruffleRuby?

TruffleRuby is a high performance implementation of the Ruby programming
language. Built on the GraalVM by Oracle Labs using the Truffle AST interpreter
and the Graal dynamic compiler.

### What is Truffle?

Truffle is a Java framework for writing AST interpreters. To implement a
language using Truffle you write an AST for your language and add methods to
interpret -- perform the action of -- each node.

Truffle also has the concept of specialisation. In most AST interpreters the
nodes are megamorphic -- they handle all possible types and other possible
conditions. In Truffle you write several different nodes for the same semantic
action but for different types and conditions. As runtime conditions change, you
switch which nodes you are using. After the program has warmed up you should end
up with an AST that is precisely tailored for the types and conditions that you
are actually using. If these conditions change, you can just switch nodes again.

### What is Graal?

Graal is a new implementation of a just-in-time compiler (JIT compiler, or we'd
normally say *dynamic compiler*) in the OpenJDK Java Virtual Machine. Unlike the
current compilers, Graal is written in Java, and exposes a Java API to the
running program. This means that instead of emitting bytecode a JVM language can
directly control the compiler. However this is complicated, so normally Truffle
uses Graal on your behalf to *partially evaluate* your AST interpreter into
machine code.

### How do I get TruffleRuby?

The easiest way to get a JVM with Graal is via the GraalVM, available from the
Oracle Technology Network. This includes the JVM, the Graal compiler, and
TruffleRuby, all in one package with compatible versions. You can get either the
runtime environment (RE) or development kit (DK).

See [Using GraalVM](using-graalvm.md).

### Why is TruffleRuby slow on a standard JVM?

The expected way to run TruffleRuby is using the Graal compiler. It isn't
designed to be efficient on a conventional JVM.

### Why is TruffleRuby faster on Graal?

When running on a VM with the Graal compiler, Truffle can use the API exposed by
Graal. Truffle gets the bytecode representation of all of the AST interpreter
methods involved in running your Ruby method, combines them into something like
a single Java method, optimises them together, and emits a single machine code
function. On Graal, Truffle also provides wrappers for JVM functionality not
normally available to Java applications such as code deoptimization. TruffleRuby
uses this to provide dramatically simpler and faster implementations of Ruby.

### Where did this code come from?

[Chris Seaton](https://github.com/chrisseaton) wrote an implementation of Ruby
on Truffle and Graal as part of an internship at Oracle Labs in the first half
of 2013. The code was merged into JRuby in early 2014. Benoit Daloze and Kevin
Menard joined as researchers in the second half of 2014, then Petr Chalupa in
2015, Bradon Fish in 2016 and Duncan MacGregor in 2017. Since then we have also
accepted contributions from people outside Oracle Labs. In 2017 the code was
forked back out of JRuby after it had matured.

### Who do I ask about TruffleRuby?

Ask about Truffle in the `#jruby` Freenode IRC room. We'll get notified if you
mention *truffle* so your question won't be missed. You can also email
chris.seaton@oracle.com.

### How do I know if I’m using a VM that has Graal?

`defined? Truffle` will tell you if you are running with Truffle, and
`Truffle::Graal.graal?` will tell you if you are also running with the Graal
dynamic compiler.

### How can I see that Truffle and Graal are working?

Put this program into `test.rb`:

```ruby
loop do
  14 + 2
end
```

As well as the instructions for running with GraalVM that are described in
[Using GraalVM](using-graalvm.md), we'll also use the `-J-G:+TraceTruffleCompilation`
to ask Truffle to tell us when it compiles something.

```
$ JAVACMD=graalvm/bin/java jruby -J-G:+TraceTruffleCompilation test.rb
[truffle] opt done         + core <opt> <split-1947596f>                               |ASTSize       6/    6 |Time    95(  92+3   )ms |DirectCallNodes I    0/D    0 |GraalNodes    35/   28 |CodeSize          147 |Source           core 
[truffle] opt done         block in block in <main> /Users/chrisseaton/Documents/ruby/test.rb:1 <opt> <split-44b29496>|ASTSize      10/   16 |Time   124( 122+2   )ms |DirectCallNodes I    1/D    0 |GraalNodes    24/    3 |CodeSize           69 |Source /Users/chrisseaton/Documents/ruby/test.rb:1 
[truffle] opt done         truffle:/jruby-truffle/core/kernel.rb:331<OSR> <opt>        |ASTSize       8/   24 |Time    77(  74+4   )ms |DirectCallNodes I    2/D    0 |GraalNodes    89/  124 |CodeSize          341 |Source            n/a 
```

Here you can see that Truffle has decided to use Graal to compile the body of
that loop to machine code - just 66 bytes of machine code in all. Along the way,
it also decided to compile `BasicObject#equal?` - this is because that method is
used enough times while we load the core library for the compiler to realise
that it is hot and also compile it.

### Why doesn’t TruffleRuby work for my application or gem?

We have significant missing functionality, especially OpenSSl and Nokogiri,
which in practice prevent almost all applications and gems from running. We're
working on it.

### Why doesn’t TruffleRuby perform well for my benchmark?

Benchmarks that we haven’t looked at yet are likely to require new code paths to
be specialized. Currently we’ve added specialisation for the code paths in the
benchmarks and applications that we’ve been using. Adding them is generally not
complicated and over time we will have specialisations to cover a broad range of
applications.

Also, check that you are using JVM with Graal (see above).

### How is this related to `invokedynamic`?

TruffleRuby doesn’t use `invokedynamic`, as it doesn't emit bytecode. However it
does have an optimising method dispatch mechanism that achieves a similar
result.

### Why doesn't JRuby switch to Truffle as well?

Truffle is a research project, there is no expected release date, and it does
not perform well on a standard JVM. The TruffleRuby team aren't recommending
that JRuby switch to Truffle. JRuby and its IR is what you want to be using
today. TruffleRuby is what you might want to run in the future, but not yet.

### Why did you fork from JRuby?

We merged into JRuby in order to be able to use large parts of their Java
implementation code. We forked back out of JRuby when we had got to the point
where the code that we were using needed to be modified for our purposes and we
no longer had any dependency on the core part of JRuby. Forking also allowed us
to simplify our repository in preparation for further integration into the rest
of the Graal ecosystem.
