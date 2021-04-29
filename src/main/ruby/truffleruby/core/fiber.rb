# frozen_string_literal: true

# Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class Fiber

  def raise(*args)
    exc = Truffle::ExceptionOperations.make_exception(args)
    exc = RuntimeError.new('') unless exc
    Primitive.fiber_raise(self, exc)
  end

  def inspect
    loc = Primitive.fiber_source_location(self)
    status = Primitive.fiber_status(self)
    "#{super.delete_suffix('>')} #{loc} (#{status})>"
  end
  alias_method :to_s, :inspect
end
