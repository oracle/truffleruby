# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

array = (0...1000).to_a
shift = 0

benchmark 'simple-set' do
  i = 0
  while i < 1000
    array[i] = i + shift
    i += 1
  end
  shift += 1
  shift = 0 if shift > 1000
end