# frozen_string_literal: true

# Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module InteropOperations
    TO_ENUM = Kernel.instance_method(:to_enum)

    def self.get_iterator(obj)
      TO_ENUM.bind_call(obj, :each)
    end

    def self.enumerator_has_next?(enum)
      begin
        enum.peek
        true
      rescue StopIteration
        false
      end
    end

  end
end
