Truffle::Patching.require_original __FILE__

# TruffleRuby: build C extensions conditionally

module Truffle::Patching::ConditionallyBuildNativeExtensions
  def build_extensions
    return if @spec.extensions.empty?

    if ENV['TRUFFLERUBY_CEXT_ENABLED'] == "false"
      puts "C extensions for #{@spec.name} disabled by TRUFFLERUBY_CEXT_ENABLED=false"
    else
      super
    end
  end
end

class Gem::Ext::Builder
  prepend Truffle::Patching::ConditionallyBuildNativeExtensions
end
