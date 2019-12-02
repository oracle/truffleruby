# frozen_string_literal: true

# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module TrufflePrimitive

  def self.method_missing(name, *args, &block)
    raise NoMethodError.new(
        "TrufflePrimitive.#{name} has to be called syntactically.", name)
  end

end
