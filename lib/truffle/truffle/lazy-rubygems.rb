# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
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
    module Kernel
      # Take this alias name so RubyGems will reuse this copy
      # and skip the method below once RubyGems is loaded.
      alias :gem_original_require :require

      private def require(path)
        begin
          gem_original_require(path)
        rescue LoadError
          require 'rubygems'
          require path
        end
      end

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
