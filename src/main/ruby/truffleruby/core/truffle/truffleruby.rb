# frozen_string_literal: true

# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module TruffleRuby

  def self.revision
    ::RUBY_REVISION
  end

  class AtomicReference

    def compare_and_set(expected_value, new_value)
      if Primitive.object_kind_of?(expected_value, Numeric)
        loop do
          current_value = get

          if Primitive.object_kind_of?(current_value, Numeric) && current_value == expected_value
            if compare_and_set_reference(current_value, new_value)
              return true
            end
          else
            return false
          end
        end
      else
        compare_and_set_reference(expected_value, new_value)
      end
    end

    def marshal_dump
      get
    end

    def marshal_load(value)
      set value
    end

  end

end
