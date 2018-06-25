# Assumptions Third Party Code Makes About TruffleRuby

When third-party code starts to make assumptions about the way TruffleRuby does
something, such as a `TruffleRuby` method, or the layout of our distribution, we
record it here so that we know who is using it if we need to change something
about it.

## TruffleRuby Extensions in Gems

* `concurrent-ruby` using `TruffleRuby.full_memory_barrier` (actually it is using
  `Truffle::System.full_memory_barrier` - need to update).
