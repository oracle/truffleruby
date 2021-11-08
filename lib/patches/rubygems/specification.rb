# truffleruby_primitives: true

# rubygems/specification.rb is required by rubygems.rb.
# require "rubygems/specification" only works on MRI if RubyGems is
# already loaded (and is a no-op), and fails with --disable-gems.
# On TruffleRuby with lazy RubyGems, Gem is an autoload.
# So if we are in that case, just require 'rubygems' instead,
# as if RubyGems was loaded eagerly on startup.

if Object.autoload?(:Gem) # lazy-rubygems enabled, rubygems.rb was not yet loaded
  require 'rubygems'
  raise 'Gem::Specification should have been defined' unless defined?(Gem::Specification)
else
  require Primitive.get_original_require(__FILE__)
end
