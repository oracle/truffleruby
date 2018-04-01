Truffle::Patching.require_original __FILE__

# TruffleRuby: build C extensions conditionally

if ENV['TRUFFLERUBY_CEXT_ENABLED'] == "false"
  module Truffle::Patching::ConditionallyBuildNativeExtensions
    def build_extensions
      return if @spec.extensions.empty?
      warn "C extensions for #{@spec.name} are not built, as TRUFFLERUBY_CEXT_ENABLED is 'false'"
    end
  end

  class Gem::Ext::Builder
    prepend Truffle::Patching::ConditionallyBuildNativeExtensions
  end
end
