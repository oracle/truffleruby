Truffle::Patching.require_original __FILE__

module Bundler
  class RubyVersion
    def self.system
      ruby_engine = if defined?(RUBY_ENGINE) && !RUBY_ENGINE.nil?
                      RUBY_ENGINE.dup
                    else
                      # not defined in ruby 1.8.7
                      "ruby"
                    end
      # :sob: mocking RUBY_VERSION breaks stuff on 1.8.7
      ruby_version = ENV.fetch("BUNDLER_SPEC_RUBY_VERSION") { RUBY_VERSION }.dup
      ruby_engine_version = case ruby_engine
                            when "ruby"
                              ruby_version
                            when "rbx"
                              Rubinius::VERSION.dup
                            when "jruby"
                              JRUBY_VERSION.dup
                            when 'truffleruby'
                              # TruffleRuby: added case branch
                              TRUFFLERUBY_VERSION
                            else
                              raise BundlerError, "RUBY_ENGINE value #{RUBY_ENGINE} is not recognized"
                            end
      patchlevel          = RUBY_PATCHLEVEL.to_s

      @ruby_version ||= RubyVersion.new(ruby_version, patchlevel, ruby_engine, ruby_engine_version)
    end
  end
end
