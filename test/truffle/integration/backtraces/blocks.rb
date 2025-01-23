# Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

require_relative 'backtraces'

def m
  yield
end

check('blocks.backtrace') do
  [1].each do |n|
    {a: 1}.each do |k, v|
      true.tap do |t|
        m do
          raise 'message'
        end
      end
    end
  end
end
