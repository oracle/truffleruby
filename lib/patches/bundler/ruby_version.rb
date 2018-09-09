# Portions copyright (c) 2010 Andre Arko
# Portions copyright (c) 2009 Engine Yard
#
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining
# a copy of this software and associated documentation files (the
# "Software"), to deal in the Software without restriction, including
# without limitation the rights to use, copy, modify, merge, publish,
# distribute, sublicense, and/or sell copies of the Software, and to
# permit persons to whom the Software is furnished to do so, subject to
# the following conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
# LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
# OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
# WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

require 'bundler/ruby_version'

module Bundler
  class RubyVersion
    def self.system
      ruby_engine = if defined?(RUBY_ENGINE) && !RUBY_ENGINE.nil?
                      RUBY_ENGINE.dup
                    else
                      # not defined in ruby 1.8.7
                      "ruby"
                    end
      # :sob: mocking RUBY_VERSION breaks stuff on 1.8.7
      ruby_version = ENV.fetch("BUNDLER_SPEC_RUBY_VERSION") { RUBY_VERSION }.dup
      ruby_engine_version = case ruby_engine
                            when "ruby"
                              ruby_version
                            when "rbx"
                              Rubinius::VERSION.dup
                            when "jruby"
                              JRUBY_VERSION.dup
                            else
                              # TruffleRuby: use the standard constant
                              RUBY_ENGINE_VERSION.dup
                            end
      patchlevel = RUBY_PATCHLEVEL.to_s

      @ruby_version ||= RubyVersion.new(ruby_version, patchlevel, ruby_engine, ruby_engine_version)
    end
  end
end
