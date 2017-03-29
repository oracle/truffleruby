# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle
  module RegexpOperations
    def self.match(re, str, pos=0)
      return nil unless str

      str = str.to_s if str.is_a?(Symbol)
      str = StringValue(str)

      m = Rubinius::Mirror.reflect str
      pos = pos < 0 ? pos + str.size : pos
      pos = m.character_to_byte_index pos
      re.search_region(str, pos, str.bytesize, true)
    end
  end
end
