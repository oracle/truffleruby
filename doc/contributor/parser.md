# TruffleRuby Parser

## Setup

Owing to our heritage as part of JRuby, we use the same parser generator as JRuby.
In order to build the TruffleRuby parser, you'll need to first clone JRuby's fork of
the [Jay](https://github.com/jruby/jay) repository.

Assuming you're in the TruffleRuby source directory:

```bash
cd ..
git clone https://github.com/jruby/jay.git
```

NB: the location of the Jay repository is significant. Please ensure it is a sibling to
the TruffleRuby source checkout. Otherwise steps later on will fail.

Now you'll need to build Jay. This will require a C compiler and `make`:

```bash
cd jay
make
```

You'll see error messages about missing packages (e.g.,
"error: package jay.yydebug does not exist"). Their presence is unfortunate, but they
can be safely ignored.

## Building the Parser

With Jay built you can now build the TruffleRuby parser. If you haven't yet, you should read 
through our [general developer guide](workflow.md). In particular, you'll want to ensure you
have the `jt` command set up.

The parser generator source file (YACC compatible) can be found at `truffleruby/src/main/java/org/truffleruby/parser/parser/RubyParser.y`.

Once you've modified the YACC file, you can build the parser by running:

```bash
jt build parser
```

You should see your changes reflected in `truffleruby/src/main/java/org/truffleruby/parser/parser/RubyParser.java`.
At this point, all you need to do is build the TruffleRuby source (`jt build`).