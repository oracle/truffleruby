# frozen_string_literal: true

# Should be done in rubygems test files?
ENV["GEM_SKIP"] = "".freeze
ENV.delete("RUBY_CODESIGN")

Warning[:experimental] = false

gem_path = [
  File.realdirpath(".bundle"),
  File.realdirpath("../.bundle", __dir__),
]
ENV["GEM_PATH"] = gem_path.join(File::PATH_SEPARATOR)
ENV["GEM_HOME"] = gem_path.first

# TruffleRuby: we inline the logic of tool/test/runner.rb here because we do not currently import that file
# require_relative '../tool/test/runner'
require 'rbconfig'

# TruffleRuby: adapt test lib path
$LOAD_PATH.unshift File.expand_path("lib", __dir__)
# $LOAD_PATH.unshift File.expand_path("../lib", __dir__)

require 'test/unit'

# require "profile_test_all" if ENV.key?('RUBY_TEST_ALL_PROFILE')
# require "tracepointchecker"
# require "zombie_hunter"
# require "iseq_loader_checker"
# require "gc_checker"
# require_relative "../test-coverage.rb" if ENV.key?('COVERAGE')

case $0
when __FILE__
  dir = __dir__
when "-e"
  # No default directory
else
  dir = File.expand_path("..", $0)
end
exit Test::Unit::AutoRunner.run(true, dir)
