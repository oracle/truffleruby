# frozen_string_literal: true

# Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module EncodingOperations
    def self.dummy_encoding(name)
      new_encoding, index = Primitive.encoding_create_dummy name
      ::Encoding::EncodingMap[name.upcase.to_sym] = [nil, new_encoding]
      [new_encoding, index]
    end
  end
end
