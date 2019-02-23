# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class IO

  def nonblock(nb=true)
    if nb
      previous = nonblock?
      begin
        self.nonblock = true unless previous
        yield self
      rescue
        self.nonblock = previous unless previous
      end
    else
      yield self
    end
  end
  Truffle.invoke_primitive :method_unimplement, IO, :nonblock

  def nonblock=(nb)
    raise NotImplementedError
  end
  Truffle.invoke_primitive :method_unimplement, IO, :nonblock=

  def nonblock?
    false
  end
  Truffle.invoke_primitive :method_unimplement, IO, :nonblock?

end
