# frozen_string_literal: true

# Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class UnboundMethod
  def inspect
    Truffle::MethodOperations.inspect_method(self, origin, owner)
  end
  alias_method :to_s, :inspect

  def bind_call(recv, ...)
    bind(recv).call(...)
  end
end
