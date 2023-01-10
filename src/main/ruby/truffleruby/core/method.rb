# frozen_string_literal: true

# Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class Method
  def inspect
    Truffle::MethodOperations.inspect_method(self, Primitive.class_of(receiver), owner, receiver)
  end
  alias_method :to_s, :inspect

  def curry(curried_arity = nil)
    self.to_proc.curry(curried_arity)
  end

  def >>(other)
    self.to_proc >> other
  end

  def <<(other)
    self.to_proc << other
  end
end
