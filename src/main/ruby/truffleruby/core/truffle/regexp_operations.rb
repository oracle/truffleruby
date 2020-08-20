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

    COMPARE_ENGINES = false
    USE_TRUFFLE_REGEX = false
    USE_TRUFFLE_REGEX_EXEC_BYTES = false

    TREGEX = Primitive.object_hidden_var_create :tregex

    def self.search_region(re, str, start_index, end_index, forward)
      raise TypeError, 'uninitialized regexp' unless Primitive.regexp_initialized?(re)
      raise ArgumentError, "invalid byte sequence in #{str.encoding}" unless str.valid_encoding?
      Primitive.encoding_ensure_compatible(re, str)

      if forward
        from = start_index
        to = end_index
      else
        from = end_index
        to = start_index
      end
      match_in_region(re, str, from, to, false, true, 0)
    end

    # This path is used by some string and scanner methods and allows
    # for at_start to be specified on the matcher.  FIXME it might be
    # possible to refactor search region to offer the ability to
    # specify at start, we should investigate this at some point.
    def self.match_onwards(re, str, from, at_start)
      md = match_in_region(re, str, from, str.bytesize, at_start, true, from)
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

    def self.match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      if COMPARE_ENGINES
        begin
          md1 = match_in_region_tregex(re, str, from, to, at_start, encoding_conversion, start)
          md2 = Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
          if md1 == md2
            return md2
          else
            $stderr.puts "match_in_region(#{re}, #{str}, #{from}, #{to}, #{at_start}, #{encoding_conversion}, #{start}) gave #{md1} but should have given #{md2}."
            return md2
          end
        rescue => e
          $stderr.puts "match_in_region(#{re}, str, #{from}, #{to}, #{at_start}, #{encoding_conversion}, #{start}) gave #{md1} raised #{e}"
          return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
        end
      else
        Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      end
    end

    def self.match_in_region_tregex(re, str, from, to, at_start, encoding_conversion, start)
      if (nil == Primitive.object_hidden_var_get(re, TREGEX))
        begin
          Primitive.object_hidden_var_set(re, TREGEX, tregex_engine.call(re.source))
        rescue => e
          $stderr.puts "Failure to compile #{re.source} using tregex - generated error #{e}."
          return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
        end
      end
      if to < from
        $stderr.puts 'Backwards searching not yet supported'
        return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      end
      tr = Primitive.object_hidden_var_get(re, TREGEX)
      if USE_TRUFFLE_REGEX_EXEC_BYTES && str.encoding == Encoding::UTF_8
        bytes = Truffle::StringOperations.raw_bytes(str.byteslice(from, to - from))
        tr_match = tr.execBytes(bytes, from)
      else
        tr_match = tr.exec(str, from)
      end
      if (tr_match.isMatch)
        starts = []
        ends = []
        pos = 0
        while true
          a_start = tr_match.getStart(pos)
          a_end = tr_match.getEnd(pos)
          break if (a_start == -1)
          starts << a_start + from
          ends << a_end + from
          pos += 1
        end
        Primitive.matchdata_create(re, str, starts, ends)
      else
        nil
      end
    end

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

    def self.compare_engines(&block)
      use_tregex = const_get(:USE_TRUFFLE_REGEX)
      begin
        const_set(:USE_TRUFFLE_REGEX, false)
        r1 = yield
        const_set(:USE_TRUFFLE_REGEX, true)
        r2 = yield
      ensure
        const_set(:USE_TRUFFLE_REGEX, use_tregex)
      end
      return r1 == r2
    end
  end
end
