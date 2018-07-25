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

begin
  require "rubygems/deprecate"
rescue LoadError
  # it's fine if it doesn't exist on the current RubyGems...
  nil
end

module Bundler
  # TODO (pitr-ch 26-Mar-2018): we need to also check that it's not autoload cosntant
  # Resources: https://github.com/bundler/bundler/issues/6163 https://github.com/rubinius/rubinius/issues/3769
  if defined?(Bundler::Deprecate) && !autoload?(:Deprecate)
    # nothing to do!
  elsif defined? ::Deprecate
    Deprecate = ::Deprecate
  elsif defined? Gem::Deprecate
    Deprecate = Gem::Deprecate
  else
    class Deprecate
    end
  end

  unless Deprecate.respond_to?(:skip_during)
    def Deprecate.skip_during
      original = skip
      self.skip = true
      yield
    ensure
      self.skip = original
    end
  end

  unless Deprecate.respond_to?(:skip)
    def Deprecate.skip
      @skip ||= false
    end
  end

  unless Deprecate.respond_to?(:skip=)
    def Deprecate.skip=(skip)
      @skip = skip
    end
  end
end
