require 'mkmf'
require_relative '../../lib/debug/version'
File.write("debug_version.h", "#define RUBY_DEBUG_VERSION \"#{DEBUGGER__::VERSION}\"\n")
create_makefile 'debug/debug'
