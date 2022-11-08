require 'mkmf'
require_relative '../../lib/debug/version'
File.write("debug_version.h", "#define RUBY_DEBUG_VERSION \"#{DEBUGGER__::VERSION}\"\n")
create_makefile 'debug/debug'

if defined?(::TruffleRuby) # Mark the gem extension as built
  require 'rubygems'
  require 'fileutils'
  spec = Gem::Specification.find_by_name('debug')
  gem_build_complete_path = spec.gem_build_complete_path
  FileUtils.mkdir_p(File.dirname(gem_build_complete_path))
  FileUtils.touch(gem_build_complete_path)
end
