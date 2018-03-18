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

class Bundler::Dependency

  # TruffleRuby: add record for truffleruby
  const_set :PLATFORM_MAP,
            remove_const(:PLATFORM_MAP).merge(truffleruby: Gem::Platform::RUBY)

  # TruffleRuby: recompute REVERSE_PLATFORM_MAP
  remove_const :REVERSE_PLATFORM_MAP
  const_set(:REVERSE_PLATFORM_MAP, {}.tap do |reverse_platform_map|
    Bundler::Dependency::PLATFORM_MAP.each do |key, value|
      reverse_platform_map[value] ||= []
      reverse_platform_map[value] << key
    end

    reverse_platform_map.each { |_, platforms| platforms.freeze }
  end.freeze)

end
