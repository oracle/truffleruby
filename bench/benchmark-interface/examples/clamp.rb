# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

def clamp_a(min, value, max)
  [min, value, max].sort[1]
end

def clamp_b(min, value, max)
  return min if value < min
  return max if value > max
  value
end
