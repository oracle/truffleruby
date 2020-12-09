# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

if RUBY_ENGINE == 'truffleruby'
  str = "x"
  100.times do
    str = "ab#{str}yz"
  end

  flat = Truffle::Ropes.flatten_rope(str)
  # Truffle::Ropes.debug_print_rope(str)

  benchmark "core-string-flatten" do
    flat = Truffle::Ropes.flatten_rope(str)
  end
end
