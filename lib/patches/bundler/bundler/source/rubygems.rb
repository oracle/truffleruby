# frozen_string_literal: true

# Portions copyright (c) 2010 Andre Arko
# Portions copyright (c) 2009 Engine Yard
# 
# MIT License
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

Truffle::Patching.require_original __FILE__

# TruffleRuby: do not skips gems with extensions not built

module Bundler
  class Source
    class Rubygems < Source
      def installed_specs
        @installed_specs ||= begin
          idx = Index.new
          have_bundler = false
          Bundler.rubygems.all_specs.reverse_each do |spec|
            next if spec.name == "bundler" && spec.version.to_s != VERSION
            have_bundler = true if spec.name == "bundler"
            spec.source = self
            # if Bundler.rubygems.spec_missing_extensions?(spec, false)
            #   Bundler.ui.debug "Source #{self} is ignoring #{spec} because it is missing extensions"
            #   next
            # end
            idx << spec
          end

          # Always have bundler locally
          unless have_bundler
            # We're running bundler directly from the source
            # so, let's create a fake gemspec for it (it's a path)
            # gemspec
            bundler = Gem::Specification.new do |s|
              s.name     = "bundler"
              s.version  = VERSION
              s.platform = Gem::Platform::RUBY
              s.source   = self
              s.authors  = ["bundler team"]
              s.loaded_from = File.expand_path("..", __FILE__)
            end
            idx << bundler
          end
          idx
        end
      end

      def cached_specs
        @cached_specs ||= begin
          idx = installed_specs.dup

          Dir["#{cache_path}/*.gem"].each do |gemfile|
            next if gemfile =~ /^bundler\-[\d\.]+?\.gem/
            s ||= Bundler.rubygems.spec_from_gem(gemfile)
            s.source = self
            # if Bundler.rubygems.spec_missing_extensions?(s, false)
            #   Bundler.ui.debug "Source #{self} is ignoring #{s} because it is missing extensions"
            #   next
            # end
            idx << s
          end
        end

        idx
      end
    end
  end
end
