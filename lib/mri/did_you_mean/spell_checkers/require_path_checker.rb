# frozen-string-literal: true

require_relative "../spell_checker"
require_relative "../tree_spell_checker"
require "rbconfig"

module DidYouMean
  class RequirePathChecker
    attr_reader :path

    INITIAL_LOAD_PATH = $LOAD_PATH.dup.freeze
    Ractor.make_shareable(INITIAL_LOAD_PATH) if defined?(Ractor)

    if defined?(::TruffleRuby)
      # did_you_mean is loaded even with --disable-gems, and then there is no RbConfig autoload.
      # Also we don't want to require 'rbconfig' as that needs the runtime home, but did_you_mean is loaded during preinit.
      ENV_SPECIFIC_EXT  = ".#{Truffle::Platform::DLEXT}"
    else
      ENV_SPECIFIC_EXT = ".#{RbConfig::CONFIG["DLEXT"]}"
    end
    Ractor.make_shareable(ENV_SPECIFIC_EXT) if defined?(Ractor)


    private_constant :INITIAL_LOAD_PATH, :ENV_SPECIFIC_EXT

    def self.requireables
      @requireables ||= INITIAL_LOAD_PATH
                          .flat_map {|path| Dir.glob("**/???*{.rb,#{ENV_SPECIFIC_EXT}}", base: path) }
                          .map {|path| path.chomp!(".rb") || path.chomp!(ENV_SPECIFIC_EXT) }
    end

    def initialize(exception)
      @path = exception.path
    end

    def corrections
      @corrections ||= begin
                         threshold     = path.size * 2
                         dictionary    = self.class.requireables.reject {|str| str.size >= threshold }
                         spell_checker = path.include?("/") ? TreeSpellChecker : SpellChecker

                         spell_checker.new(dictionary: dictionary).correct(path).uniq
                       end
    end
  end
end
