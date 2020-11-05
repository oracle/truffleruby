# frozen_string_literal: true
require 'rbconfig'

if defined?(::TruffleRuby)
  $LOAD_PATH.unshift File.expand_path("lib", __dir__)
else
  $LOAD_PATH.unshift File.expand_path("../lib", __dir__)
end

require 'test/unit'

require "profile_test_all" if ENV.key?('RUBY_TEST_ALL_PROFILE')
require "tracepointchecker" unless defined?(::TruffleRuby)
require "zombie_hunter"
require "iseq_loader_checker" unless defined?(::TruffleRuby)
require "gc_compact_checker"
require_relative "../test-coverage.rb" if ENV.key?('COVERAGE')

case $0
when __FILE__
  dir = __dir__
when "-e"
  # No default directory
else
  dir = File.expand_path("..", $0)
end
exit Test::Unit::AutoRunner.run(true, dir)
