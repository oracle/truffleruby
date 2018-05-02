# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Beware, RubyDebutTest use hard-coded line numbers from this file!

def fnc(n, m)
  x = n + m
  n = m - n
  m = m / 2
  x = x + n * m
  x
end

def main
  i = 10
  res = fnc(i = i + 1, 20)
  res
end
Truffle::Interop.export_method(:main)
