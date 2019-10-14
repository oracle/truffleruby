# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

small = 10
large = 1_000_000

def str(n)
  Array.new(n, 'a'.ord).pack('C*')
end

s1_small = str(small)
s2_small = str(small)

s1_large = str(large)
s2_large = str(large)

equal = s1_small == s2_small
raise unless equal
equal = s1_large == s2_large
raise unless equal

benchmark "core-string-equal-#{small}" do
  equal = s1_small == s2_small
end

benchmark "core-string-equal-#{large}" do
  equal = s1_large == s2_large
end
