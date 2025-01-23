# frozen_string_literal: true
# truffleruby_primitives: true

# Copyright (c) 2017, 2025 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class IO
  def nread
    # TODO CS 14-Apr-18 provide a proper implementation
    0
  end

  def ready?
    ensure_open_and_readable
    Truffle::IOOperations.poll(self, IO::READABLE, 0) > 0
  end
end
