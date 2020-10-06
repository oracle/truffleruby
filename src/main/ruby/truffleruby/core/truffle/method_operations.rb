# frozen_string_literal: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module MethodOperations
    def self.inspect_method(meth, origin, owner, receiver = undefined)
      extra = ''
      if Primitive.method_unimplemented? meth
        extra = ' (not-implemented)'
      else
        file, line = meth.source_location
        if file && line
          extra = " #{file}:#{line}"
        end
      end

      if !Primitive.undefined?(receiver) && owner.singleton_class?
        "#<#{meth.class}: #{receiver.inspect}.#{meth.name}#{extra}>"
      else
        origin_owner = origin == owner ? origin : "#{origin}(#{owner})"
        "#<#{meth.class}: #{origin_owner}##{meth.name}#{extra}>"
      end
    end
  end
end
