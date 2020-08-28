# frozen_string_literal: true

# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module RegexpOperations

    def self.search_region(re, str, start_index, end_index, forward)
      raise TypeError, 'uninitialized regexp' unless Primitive.regexp_initialized?(re)
      raise ArgumentError, "invalid byte sequence in #{str.encoding}" unless str.valid_encoding?
      raise Encoding::CompatibilityError, "incompatible character encodings: #{re.encoding} and #{str.encoding}" unless Encoding.compatible?(re, str)

      if forward
        from = start_index
        to = end_index
      else
        from = end_index
        to = start_index
      end
      Primitive.regexp_match_in_region(re, str, from, to, false, true, 0)
    end

    # This path is used by some string and scanner methods and allows
    # for at_start to be specified on the matcher.  FIXME it might be
    # possible to refactor search region to offer the ability to
    # specify at start, we should investigate this at some point.
    def self.match_onwards(re, str, from, at_start)
      md = Primitive.regexp_match_in_region(re, str, from, str.bytesize, at_start, true, from)
      Primitive.matchdata_fixup_positions(md, from) if md
      md
    end

    def self.match(re, str, pos=0)
      return nil unless str

      str = str.to_s if str.is_a?(Symbol)
      str = StringValue(str)

      pos = pos < 0 ? pos + str.size : pos
      pos = Primitive.string_byte_index_from_char_index(str, pos)
      search_region(re, str, pos, str.bytesize, true)
    end

    def self.match_from(re, str, pos)
      return nil unless str

      search_region(re, str, pos, str.bytesize, true)
    end

    def self.last_match(a_binding)
      Primitive.frame_local_variable_get(:'$~', a_binding)
    end
    Truffle::Graal.always_split(method(:last_match))

    def self.set_last_match(value, a_binding)
      unless Primitive.nil?(value) || Primitive.object_kind_of?(value, MatchData)
        raise TypeError, "Wrong argument type #{value} (expected MatchData)"
      end
      # TODO DMM 2018-01-12 Proc.binding being nil is a bug, we can remove the check once we've fixed it.
      Primitive.frame_local_variable_set(:'$~', a_binding, value)
    end
    Truffle::Graal.always_split(method(:set_last_match))

    def self.compilation_stats
      Hash[*compilation_stats_array]
    end

    def self.match_stats
      Hash[*match_stats_array]
    end

    def self.collapsing?(match)
      match.byte_begin(0) == match.byte_end(0)
    end

    def self.pre_match_from(match, idx)
      source = Primitive.match_data_get_source(match)
      return source.byteslice(0, 0) if match.byte_begin(0) == 0

      nd = match.byte_begin(0) - 1
      source.byteslice(idx, nd-idx+1)
    end
  end
end
