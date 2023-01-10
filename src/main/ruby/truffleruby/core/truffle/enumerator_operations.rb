# frozen_string_literal: true

# Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module EnumeratorOperations
    LAZY_OVERRIDE_METHODS = %i[map collect flat_map collect_concat select find_all filter filter_map reject grep grep_v
                               zip take take_while drop drop_while uniq with_index]

    def self.lazy_method(meth)
      if LAZY_OVERRIDE_METHODS.include?(meth)
        :"_enumerable_#{meth}"
      else
        meth
      end
    end
  end
end
