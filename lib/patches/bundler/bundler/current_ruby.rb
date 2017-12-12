Truffle::Patching.require_original __FILE__

module Bundler
  class CurrentRuby
    # TruffleRuby: added method
    def truffleruby?
      defined?(RUBY_ENGINE) && RUBY_ENGINE == "truffleruby"
    end

    def ruby?
      truffleruby?
    end
  end
end
