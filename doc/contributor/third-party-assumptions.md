# Assumptions Third Party Code Makes About TruffleRuby

Third-party code should never use any APIs apart from standard Ruby, and those
listed in [TruffleRuby additions](../user/truffleruby-additions.md).

When third-party code uses the TruffleRuby additions, it should be listed here
so that we know why non-standard APIs have been added.

## TruffleRuby Extensions in Gems

* `concurrent-ruby` < 1.1.0 used `Truffle::System.full_memory_barrier`,
  `Truffle::Primitive.logical_processors`,  `Truffle::AtomicReference`, and
  `Truffle::System.synchronized`. All of these have been removed.
  
* `concurrent-ruby` >= 1.1.0 uses `TruffleRuby.full_memory_barrier`,
  `TruffleRuby.synchronized`, and `TruffleRuby::AtomicReference`, 
