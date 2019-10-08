# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
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
    not Kernel.select([self], [], [], 0).nil?
  end

  def wait(timeout = nil)
    Kernel.select([self], [], [], timeout).nil? ? nil : self
  end

  alias_method :wait_readable, :wait

  def wait_writable(timeout = nil)
    Kernel.select([], [self], [], timeout).nil? ? nil : self
  end
end
