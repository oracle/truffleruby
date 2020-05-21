# rubygems/deprecate needs Gem to be defined for `module Gem::Deprecate`.
# On TruffleRuby, Gem is an autoload. So while loading rubygems/deprecate.rb
# it will trigger the autoload and load rubygems.rb, but rubygems.rb needs Gem::Deprecate,
# and the require 'rubygems/deprecate' in rubygems.rb will be skipped since it's already being loaded.
# So a solution is to always load rubygems.rb before starting to load rubygems/deprecate.rb.
# See https://github.com/oracle/truffleruby/issues/2014

# check to avoid circular require warning on require 'rubygems'
if Object.autoload?(:Gem)
  require 'rubygems'
end

require 'rubygems/deprecate'
