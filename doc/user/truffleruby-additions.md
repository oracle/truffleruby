# TruffleRuby Additional Functionality

TruffleRuby is intended to be usable as a standard Ruby implementation that runs
programs developed on other implementations, but it also provides additional
functionality beyond that of other implementations.

Also see the [document describing our compatibility](compatibility.md) with
other Ruby implementations.

## Detecting if you are running on TruffleRuby

You can use the `--version` command-line option. TruffleRuby will report for
example:

```
truffleruby 0.24, like ruby 2.3.3 <Java HotSpot(TM) 64-Bit Server VM 1.8.0_121-b13 with Graal> [darwin-x86_64]
```

In Ruby code, you can look at the standard `RUBY_ENGINE` constant, which will be
`'truffleruby'`.

It is also possible to use feature-detection instead of looking at
`RUBY_ENGINE`. For example if you are writing an application that needs
JavaScript interoperability you could test for `defined?(Truffle::Interop) &&
Truffle::Interop.mime_type_supported?('application/javascript')`.

TruffleRuby is an integral part of GraalVM, so the version number of TruffleRuby
is always the same as the version of GraalVM that contains it. If you are using
TruffleRuby outside of GraalVM, such as from Java 9 or a modified Java 8, the
version will be `'0.0'`. You can find the version number of GraalVM and
TruffleRuby using the standard `RUBY_ENGINE_VERSION` constant.

## Truffle methods

`Truffle.graal?` reports if the Graal compiler is available and will be
used.

`Truffle.aot?` reports if TruffleRuby has been ahead-of-time compiled.
In practice this implies that the SubstrateVM is being used.

## Polyglot interoperability

The `Truffle::Interop` modules provides access to the interopability
functionality of GraalVM. It is [described in a separate document](interop.md).

## Unsupported additional functionality

You may be able to find some other modules and methods not listed here, such as
`Truffle::POSIX`, which are designed to support the implementation of
TruffleRuby and should not be used. They may modified or made not visible to
user programs in the future, and you should not use them.
