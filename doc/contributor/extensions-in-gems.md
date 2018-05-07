# TruffleRuby Extensions in Gems

When we use a TruffleRuby extension, such as a non-standard method, or
non-standard behaviour, in third-party code we record it here so that we
know who is using it if we need to change something about it.

* `concurrent-ruby` using `Truffle.full_memory_barrier` (actually it is using
  `Truffle::System.full_memory_barrier` - need to update).
