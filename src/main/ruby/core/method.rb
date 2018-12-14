# frozen_string_literal: true

# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class Method

  def inspect
    extra = ''

    if Truffle.invoke_primitive :method_unimplemented?, self
      extra = ' (not-implemented)'
    else
      file, line = source_location

      if file && line
        extra = " #{file}:#{line}"
      end
    end

    "#<#{self.class}: #{receiver.class}(#{owner})##{name}#{extra}>"
  end

  def curry(curried_arity = nil)
    self.to_proc.curry(curried_arity)
  end
  
  alias_method :to_s, :inspect

end
