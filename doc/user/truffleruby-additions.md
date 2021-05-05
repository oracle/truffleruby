# TruffleRuby Additional Functionality

TruffleRuby is intended to be usable as a standard Ruby implementation that runs programs developed on other implementations, but it also provides additional functionality beyond that of other implementations.

See the [Compatibility](compatibility.md) guide for compatibility with other Ruby implementations.

## Detecting If You Run on TruffleRuby

You can use the `--version` command-line option. TruffleRuby will report for example:
```shell
truffleruby ..., like ruby ..., GraalVM CE Native [x86_64-darwin]
```

In Ruby code, you can look at the standard `RUBY_ENGINE` constant, which will be `'truffleruby'`.
In C code `TRUFFLERUBY` is defined.

It is also possible to use feature-detection instead of looking at `RUBY_ENGINE`.

TruffleRuby is an integral part of GraalVM, so the version number of TruffleRuby is always the same as the version of GraalVM that contains it.
If you are using TruffleRuby outside of GraalVM, such as a standard JVM, the version will be `'0.0'`.
You can find the version number of GraalVM and TruffleRuby using the standard `RUBY_ENGINE_VERSION` constant.

## TruffleRuby Methods and Classes

TruffleRuby provides these non-standard methods and classes that provide additional functionality in the `TruffleRuby` module:

* `TruffleRuby.jit?` reports if the GraalVM Compiler is available and will be used.

* `TruffleRuby.native?` reports if TruffleRuby is compiled as a native executable.

* `TruffleRuby.cexts?` reports if TruffleRuby has the GraalVM LLVM Runtime for C extensions available.

* `TruffleRuby.revision` reports the source control revision used to build TruffleRuby as a String. Also available as `RUBY_REVISION`, like CRuby 2.7+.

* `TruffleRuby.full_memory_barrier` ensures lack of reordering of loads or stores before the barrier with loads or stores after the barrier.

* `TruffleRuby.graalvm_home` returns the GraalVM home or `nil` if running outside of GraalVM (e.g., TruffleRuby standalone).

* `TruffleRuby.synchronized(object) { }` will run the block while holding an implicit lock per object instance.

### Atomic References

* `atomic_reference = TruffleRuby::AtomicReference.new(value=nil)` creates a new atomic reference with a reference to a given object.

* `atomic_reference.get` gets the value of an atomic reference, returning the value.

* `atomic_reference.set(new_value)` sets the value of an atomic reference and causes a memory barrier on writes involving `new_value`.

* `atomic_reference.get_and_set(new_value)` sets the value of an atomic reference, returns the previous value, and causes a memory barrier on writes involving `new_value`.

* `atomic_reference.compare_and_set(expected_value, new_value)` sets the value of an atomic reference, only if it currently has the expected value, returning a boolean to say whether or not it was set, and causes a memory barrier on writes involving `new_value`. For numeric objects it will get the current value and then check that the current value is also a numeric and that it is equal to the expected value by calling `==`, then perform an atomic compare and set.

* `AtomicReference` is marshalable.

### Concurrent Maps

`TruffleRuby::ConcurrentMap` is a key-value data structure, like a `Hash` and using `#hash` and `#eql?` to compare keys and identity to compare values. Unlike `Hash` it is unordered. All methods on `TruffleRuby::ConcurrentMap` are thread-safe but should have higher concurrency than a fully syncronized implementation. It is intended to be used by gems such as Concurrent Ruby - please use via this gem rather than using directly in most cases.

* `map = TruffleRuby::ConcurrentMap.new([initial_capacity: 1024], [load_factor: 0.5])`

* `map[key] = new_value`

* `map[key]`

* `map.compute_if_absent(key) { computed_value }` if the key is not found, run the block and store the result. If the block returns `nil` the store is cancelled. The block is run at most once. Returns the final value, or `nil` if the store was cancelled.

* `map.compute_if_present(key) { |current_value| computed_value }` if the key is found, run the block and store the result. If the block returns `nil` the store is cancelled. The block is run at most once. Returns the final value, or `nil` if the store was cancelled.

* `map.compute(key) { |current_value| computed_value }` run the block, passing the current value if there is one or `nil`, and store the result. Returns the final value, or `nil` if the store was cancelled.

* `map.merge_pair(key, new_value) { |existing_value| merged_value }` if key is not found or is `nil`, store the new value, otherwise call the block and store the result. Returns the final value, or `nil` if the store was cancelled.

* `map.replace_pair(key, expected_value, new_value)` replace the value for key but only if it was as expected. Returns if the value was replaced or not.

* `map.replace_if_exists(key, value)` replace the value for key but only if it was found. Returns if the value was replaced or not.

* `map.get_and_set(key, new_value)` sets the value for a key and returns the previous value.

* `map.key?(key)` returns if a key is in the map.

* `map.delete(key)` removes a key from the map if it exists, returning the value or `nil` if it did not exist.

* `map.delete_pair(key, expected_value)` removes a key but only if it was the expected value. Returns if the key was deleted.

* `map.clear` removes all entries from the map.

* `map.size` gives the number of entries in the map.

* `map.get_or_default(key, default_value)`

* `map.each_pair { |key, value| ... }`

## FFI

TruffleRuby includes a [Ruby-FFI](https://github.com/ffi/ffi) backend. This should be transparent: you can just install the `ffi` gem as normal, and it will use TruffleRuby's FFI backend. TruffleRuby also includes a default version of the FFI gem, so `require "ffi"` always works on TruffleRuby, even if the gem is not installed.

## Polyglot Programming

The `Polyglot` and `Java` modules provide access to the polyglot programming functionality of GraalVM.
They are described in the [Polyglot Programming](polyglot.md) guide.

## Unsupported Additional Functionality

You may be able to find some other modules and methods not listed here that look interesting, such as `Truffle::POSIX` or `Truffle::FFI`.
Additional modules and methods not listed in this document are designed to support the implementation of TruffleRuby and should not be used. They may be modified or made not visible to user programs in the future, and you should not use them.

Extra macros, functions, and variables in TruffleRuby C extension headers beyond those provided by MRI, such as those starting with `rb_tr_*`, are unsupported and should not be used by any C extension.
