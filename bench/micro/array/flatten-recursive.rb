# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

array = []
current_array = array
100.times do
  next_array = []
  current_array.append(0, 1, next_array, 3, 4)
  current_array = next_array
end

benchmark 'core-array-flatten-recursive' do
  array.flatten
end
