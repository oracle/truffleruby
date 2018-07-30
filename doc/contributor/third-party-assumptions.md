# Assumptions Third Party Code Makes About TruffleRuby

When third-party code starts to make assumptions about the way TruffleRuby does
something, such as a `TruffleRuby` method, or the layout of our distribution, we
record it here so that we know who is using it if we need to change something
about it.

## TruffleRuby Extensions in Gems

* `concurrent-ruby` <= 1.0.5 uses: `Truffle::System.full_memory_barrier`,
  `Truffle::Primitive.logical_processors`
* `concurrent-ruby` >= 1.1.0 uses: `TruffleRuby.full_memory_barrier`, 
  `Truffle::AtomicReference`, `Truffle::System.synchronized`
