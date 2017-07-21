Truffle::Patching.require_original __FILE__

# TruffleRuby: build C extensions conditionally

module Truffle::Patching::ConditionallyBuildNativeExtensions
  def build_extensions
    return if @spec.extensions.empty?

    if ENV['TRUFFLERUBY_CEXT_ENABLED'] && ENV['TRUFFLERUBY_CEXT_ENABLED'] != "false"
      super
    else
      puts "WORKAROUND: Not building native extensions for #{@spec.name}.\n" +
           'Support of C extensions is in development, set TRUFFLERUBY_CEXT_ENABLED=true to experiment.'
    end
  end
end

class Gem::Ext::Builder
  prepend Truffle::Patching::ConditionallyBuildNativeExtensions
end
