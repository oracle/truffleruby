# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
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
    Truffle::IOOperations.poll(self, Truffle::IOOperations::POLLIN, 0)
  end

  def wait_readable(timeout = nil)
    ensure_open_and_readable
    Truffle::IOOperations.poll(self, Truffle::IOOperations::POLLIN, timeout) ? self : nil
  end
  alias_method :wait, :wait_readable

  def wait_writable(timeout = nil)
    ensure_open_and_writable
    Truffle::IOOperations.poll(self, Truffle::IOOperations::POLLOUT, timeout) ? self : nil
  end
end
