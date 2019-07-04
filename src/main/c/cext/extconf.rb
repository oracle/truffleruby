# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require 'mkmf'

$srcs = %w[ruby.c internal.c st.c]

# st.c needs this for TRUE/FALSE to be defined.
# -DRUBY_EXPORT is added in MRI's configure.in.
$CFLAGS << " -DRUBY_EXPORT"

# We need ruby.so to link against libpolyglot-mock,
# otherwise compilation of ruby.so fails with "Undefined symbols" on macOS.
$LIBS += " -lpolyglot-mock"

# Do no link against libruby for libruby itself
if Truffle::Platform.darwin?
  # Set the install_name of libtruffleruby on macOS, so C exts linking to it
  # will know they need to look at the rpath to find it.
  $LIBRUBYARG = "-Wl,-install_name,@rpath/libtruffleruby.dylib"
else
  $LIBRUBYARG = ""
end

create_makefile('libtruffleruby')
