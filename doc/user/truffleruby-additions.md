# TruffleRuby Additional Functionality

TruffleRuby is intended to be usable as a standard Ruby implementation that runs
programs developed on other implementations, but it also provides additional
functionality beyond that of other implementations.

See the [Compatibility](compatibility.md) guide for compatibility with
other Ruby implementations.

## Detecting If You Run on TruffleRuby

You can use the `--version` command-line option. TruffleRuby will report for
example:

```
truffleruby ..., like ruby ..., GraalVM CE Native [x86_64-darwin]
```

In Ruby code, you can look at the standard `RUBY_ENGINE` constant, which will be
`'truffleruby'`. In C code `TRUFFLERUBY` is defined.

It is also possible to use feature-detection instead of looking at
`RUBY_ENGINE`.

TruffleRuby is an integral part of GraalVM, so the version number of TruffleRuby
is always the same as the version of GraalVM that contains it. If you are using
TruffleRuby outside of GraalVM, such as a standard JVM, the version will be
`'0.0'`. You can find the version number of GraalVM and TruffleRuby using the
standard `RUBY_ENGINE_VERSION` constant.

## TruffleRuby Methods and Classes

TruffleRuby provides these non-standard methods and classes that provide
additional functionality in the `TruffleRuby` module.

`TruffleRuby.graalvm_home` returns the GraalVM home or `nil` if running outside of GraalVM (e.g., TruffleRuby standalone).

`TruffleRuby.jit?` reports if the GraalVM Compiler is available and will be
used.

`TruffleRuby.native?` reports if TruffleRuby is compiled as a native image.

`TruffleRuby.cexts?` reports if TruffleRuby has the GraalVM LLVM Runtime for C
extensions available.

`TruffleRuby.revision` reports the source control revision used to build
TruffleRuby as a String. Also available as `RUBY_REVISION`, like CRuby 2.7+.

`TruffleRuby.full_memory_barrier` ensures lack of reordering of loads or stores
before the barrier with loads or stores after the barrier.

`TruffleRuby.synchronized(object) { }` will run the block while holding an
implicit lock per object instance.

### Atomic references

`atomic_reference = TruffleRuby::AtomicReference.new(value=nil)` creates a new
atomic reference with a reference to a given object.

`atomic_reference.get` gets the value of an atomic reference, returning the
value.

`atomic_reference.set(new_value)` sets the value of an atomic reference and
causes a memory barrier on writes involving `new_value`.

`atomic_reference.get_and_set(new_value)` sets the value of an atomic reference,
returns the previous value, and causes a memory barrier on writes involving
`new_value`.

`atomic_reference.compare_and_set(expected_value, new_value)` sets the value
of an atomic reference, only if it currently has the expected value, returning
a boolean to say whether or not it was set, and causes a memory barrier on
writes involving `new_value`. For numeric objects it will get the current
value and then check that the current value is also a numeric and that it is
equal to the expected value by calling `==`, then perform an atomic compare
and set.

`AtomicReference` is marshalable.

## FFI

TruffleRuby includes a built-in implementation of [Ruby-FFI](https://github.com/ffi/ffi),
compatible with version 1.12.2 of the `ffi` gem. This should be transparent: you can
just install the `ffi` gem as normal, and it will use our built-in implementation,
regardless of the version of the `ffi` gem.

## Polyglot Programming

The `Polyglot` and `Java` modules provides access to the polyglot programming
functionality of GraalVM.
They are described in the [Polyglot Programming](polyglot.md) guide.

## Unsupported Additional Functionality

You may be able to find some other modules and methods not listed here that look
interesting, such as `Truffle::POSIX` or `Truffle::FFI`. Additional modules and
methods not listed in this document are designed to support the implementation
of TruffleRuby and should not be used. They may be modified or made not visible
to user programs in the future, and you should not use them.

Extra macros, functions, and variables in TruffleRuby C extension headers
beyond those provided by MRI, such as those starting with `rb_tr_*`, are
unsupported and should not be used by any C extension.
