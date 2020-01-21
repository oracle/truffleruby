# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Delay this so the pre-initialized context can also be used with --disable-gems
# Otherwise, --disable-gems would degrade startup which is counter-intuitive.
Truffle::Boot.delay do
  if Truffle::Boot.get_option 'rubygems'
    module Truffle::LazyRubyGems
    end

    module Kernel
      # Take this alias name so RubyGems will reuse this copy
      # and skip the method below once RubyGems is loaded.
      alias :gem_original_require :require

      private def require(path)
        begin
          gem_original_require(path)
        rescue LoadError
          gem_original_require 'rubygems'

          # Check that #require was redefined by RubyGems, otherwise we would end up in infinite recursion
          new_require = ::Kernel.instance_method(:require)
          if new_require == Truffle::LazyRubyGems::LAZY_REQUIRE
            raise 'RubyGems did not redefine #require as expected, make sure $LOAD_PATH and home are set correctly'
          end
          new_require.bind(self).call(path)
        end
      end

      Truffle::LazyRubyGems::LAZY_REQUIRE = instance_method(:require)

      private def gem(*args)
        require 'rubygems'
        gem(*args)
      end
    end

    class Object
      autoload :Gem, 'rubygems'

      # RbConfig is required by RubyGems, which makes it available in Ruby by default.
      # Autoload it since we do not load RubyGems eagerly.
      autoload :RbConfig, 'rbconfig'
      # Defined by RbConfig
      autoload :CROSS_COMPILING, 'rbconfig'

      # StringIO is required by RubyGems
      autoload :StringIO, 'stringio'
    end
  end
end
