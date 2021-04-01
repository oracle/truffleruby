# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module LessAllocations
  EMPTY_HASH = {}.freeze
end

env = {}

benchmark 'core-fiber-locals-read' do
  Thread.current[:fiber_local]
end

benchmark 'core-fiber-locals-write' do
  Thread.current[:fiber_local] = env
end

benchmark 'core-fiber-locals-swap' do
  prev = Thread.current[:fiber_local] || LessAllocations::EMPTY_HASH
  Thread.current[:fiber_local] = env
  begin
    # nothing
  ensure
    Thread.current[:fiber_local] = prev
  end
end
