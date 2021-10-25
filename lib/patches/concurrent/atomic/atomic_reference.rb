# truffleruby_primitives: true

# Fix TypeError: superclass must be a Class on some concurrent-ruby releases.
# See https://github.com/ruby-concurrency/concurrent-ruby/pull/925

module Concurrent
  begin
    require Primitive.get_original_require(__FILE__)
  rescue TypeError => e
    if e.message == 'superclass must be a Class' and
        defined?(AtomicReferenceImplementation) and
        defined?(TruffleRubyAtomicReference) and
        Symbol === AtomicReferenceImplementation

      remove_const :AtomicReferenceImplementation
      AtomicReferenceImplementation = TruffleRubyAtomicReference
      private_constant :AtomicReferenceImplementation

      class AtomicReference < AtomicReferenceImplementation
        def to_s
          format '%s value:%s>', super[0..-2], get
        end
        alias_method :inspect, :to_s
      end

      # Mark the original file as loaded (even though it raise'd) so it is not loaded a second time
      Truffle::FeatureLoader.provide_feature(Primitive.get_original_require(__FILE__))
    else
      raise e
    end
  end
end
