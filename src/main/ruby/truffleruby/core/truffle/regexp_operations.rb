# frozen_string_literal: true

# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module RegexpOperations
    def self.match(re, str, pos=0)
      return nil unless str

      str = str.to_s if str.is_a?(Symbol)
      str = StringValue(str)

      pos = pos < 0 ? pos + str.size : pos
      pos = TrufflePrimitive.string_byte_index_from_char_index(str, pos)
      re.search_region(str, pos, str.bytesize, true)
    end

    def self.last_match(a_binding)
      Truffle::KernelOperations.frame_local_variable_get(:'$~', a_binding)
    end
    Truffle::Graal.always_split(method(:last_match))

    def self.set_last_match(value, a_binding)
      unless value.nil? || Truffle::Type.object_kind_of?(value, MatchData)
        raise TypeError, "Wrong argument type #{value} (expected MatchData)"
      end
      # TODO DMM 2018-01-12 Proc.binding being nil is a bug, we can remove the check once we've fixed it.
      Truffle::KernelOperations.frame_local_variable_set(:'$~', a_binding, value)
    end
    Truffle::Graal.always_split(method(:set_last_match))

    def self.compilation_stats
      Hash[*compilation_stats_array]
    end

    def self.match_stats
      Hash[*match_stats_array]
    end
  end
end
