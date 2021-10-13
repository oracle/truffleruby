# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Compile libtruffleruby as a .dylib shared library and not a .bundle on macOS to allow -ltruffleruby
if Truffle::Platform.darwin?
  module Truffle::Platform
    remove_const :DLEXT
    DLEXT = SOEXT
  end

  require 'rbconfig'
  RbConfig::CONFIG['LDSHARED'] = "#{RbConfig::CONFIG['CC']} -shared"
  RbConfig::MAKEFILE_CONFIG['LDSHARED'] = '$(CC) -shared'
end

require 'mkmf'

# -DRUBY_EXPORT is added in MRI's configure.in.
$CFLAGS << " -DRUBY_EXPORT"

# libruby depends on librt on Linux, and C extensions like date rely on that because they then
# automatically depend on librt (e.g., for clock_gettime).
$LIBS << '-lrt' if Truffle::Platform.linux?

if Truffle::Platform.darwin?
  # Set the install_name of libtruffleruby on macOS, so mkmf executables linking to it
  # will know they need to look at the rpath to find it.
  $LIBRUBYARG = "-Wl,-install_name,@rpath/libtruffleruby.dylib #{$LIBRUBYARG}"
end

create_makefile('libtruffleruby')
