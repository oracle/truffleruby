# TruffleRuby: ignore RubyDep warnings, RubyDep is unmaintained
# See https://github.com/e2/ruby_dep/pull/37
# See https://github.com/e2/ruby_dep/wiki/Disabling-warnings for how this works
require 'ruby_dep/quiet'

# Require the original file
require 'ruby_dep/warning'
