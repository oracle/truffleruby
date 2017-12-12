Truffle::Patching.require_original __FILE__

module Bundler
  class CurrentRuby
    # TruffleRuby: added method
    def truffleruby?
      defined?(RUBY_ENGINE) && RUBY_ENGINE == "truffleruby"
    end

    alias_method :original_ruby?, :ruby?
    def ruby?
      truffleruby? || original_ruby?
    end
  end
end
