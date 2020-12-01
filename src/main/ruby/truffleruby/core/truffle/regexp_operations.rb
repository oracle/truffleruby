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
    USE_TRUFFLE_REGEX = true

    TREGEX_CACHE = Primitive.object_hidden_var_create :tregex_cache

    TRegexCacheKey = Struct.new(:sticky, :encoding)

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

    def self.results_match(md1, md2)
      if md1 == nil then
        md2 == nil
      elsif md2 == nil then
        false
      elsif md1.kind_of?(Exception) then
        md1.class == md2.class
      elsif md2.kind_of?(Exception) then
        false
      else
        if md1.size != md2.size then
          return false
        end
        md1.size.times do |x|
          if md1.begin(x) != md2.begin(x) || md1.end(x) != md2.end(x)
            return false
          end
        end
        true
      end
    end

    def self.print_match_data(md)
      if md == nil
        $stderr.puts '    NO MATCH'
      elsif md.kind_of?(Exception) then
        $stderr.puts "    EXCEPTION - #{md}"
      else
        md.size.times do |x|
          $stderr.puts "    #{md.begin(x)} - #{md.end(x)}"
        end
        md.captures.each do |c|
          $stderr.puts "    #{c}"
        end
      end
    end

    def self.return_match_data(md)
      if md.kind_of?(Exception) then
        raise md
      else
        md
      end
    end

    def self.match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      if COMPARE_ENGINES
        begin
          md1 = match_in_region_tregex(re, str, from, to, at_start, encoding_conversion, start)
        rescue => e
          md1 = e
        end
        begin
          md2 = Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
        rescue => e
          md2 = e
        end
        if self.results_match(md1, md2) then
          return self.return_match_data(md1)
        else
          $stderr.puts "match_in_region(/#{re}/, \"#{str}\"@#{str.encoding}, #{from}, #{to}, #{at_start}, #{encoding_conversion}, #{start}) gate"
          self.print_match_data(md1)
          $stderr.puts 'but we expected'
          self.print_match_data(md2)
          return self.return_match_data(md2)
        end
      elsif USE_TRUFFLE_REGEX
        match_in_region_tregex(re, str, from, to, at_start, encoding_conversion, start)
      else
        Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      end
    end

    def self.options_to_flags(options)
      flags = ''
      if options & Regexp::MULTILINE != 0
        flags += 'm'
      end
      if options & Regexp::IGNORECASE != 0
        flags += 'i'
      end
      if options & Regexp::EXTENDED != 0
        flags += 'x'
      end
      flags
    end

    def self.select_encoding(re, str, encoding_conversion)
      if encoding_conversion then
        if re.encoding == Encoding::US_ASCII && StringOperations::ascii_only?(str) then
          re.encoding
        elsif str.encoding.ascii_compatible? && re.options & Regexp::FIXEDENCODING != 0 then
          re.encoding
        else
          str.encoding
        end
      else
        re.encoding
      end
    end

    def self.to_tregex_encoding(encoding)
      case encoding
      when Encoding::UTF_8
        'UTF-8'
      when Encoding::US_ASCII, Encoding::ASCII_8BIT
        'LATIN-1'
      else
        nil
      end
    end

    def self.match_in_region_tregex(re, str, from, to, at_start, encoding_conversion, start)
      if to < from || to != str.bytes.length || start != 0 || from < 0 || !to_tregex_encoding(select_encoding(re, str, encoding_conversion)) then
        return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      end
      begin
        processed_re_source = preprocess_regexp_source(re.source)
        re_source = StringOperations::java_string(processed_re_source)
        if processed_re_source.length != Truffle::Interop::from_java_string(re_source).length then
          # Calling java_string with certain string and certain encodings (e.g. non-ASCII characters in BINARY encoding)
          # can lead to differences in escape behavior, yielding different strings. These result in strings of different
          # sizes after the round trip. In such cases, we bail out.
          return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
        end
      rescue => e
        # Some strings might contain invalid (non-Unicode) characters, e.g. values higher than 127 in ASCII strings.
        # These strings can then throw CannotConvertBinaryRubyStringToJavaString exception.
        return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      end
      compiled_regex_cache = Primitive.object_hidden_var_get(re, TREGEX_CACHE)
      if compiled_regex_cache == nil then
        compiled_regex_cache = Hash.new do |cache, cache_key|
          if cache_key.sticky then
            flags = options_to_flags(re.options) + 'y'
          else
            flags = options_to_flags(re.options)
          end
          cache[cache_key] = tregex_engine.call(re_source, flags, to_tregex_encoding(cache_key.encoding))
        end
        Primitive.object_hidden_var_set(re, TREGEX_CACHE, compiled_regex_cache)
      end
      cache_key = TRegexCacheKey.new(at_start, select_encoding(re, str, encoding_conversion))
      compiled_regex = compiled_regex_cache[cache_key]
      begin
        str_bytes = StringOperations::raw_bytes(str)
        regex_result = compiled_regex.execBytes(str_bytes, from)
      rescue => e
        if !(e.message.index('UnsupportedRegexException'))
          $stderr.puts "Failure to execute #{re.source} using tregex - generated error #{e}."
          raise e
        end
        return Primitive.regexp_match_in_region(re, str, from, to, at_start, encoding_conversion, start)
      end
      if regex_result.isMatch
        starts = []
        ends = []
        compiled_regex.groupCount.times do |pos|
          starts << regex_result.getStart(pos)
          ends << regex_result.getEnd(pos)
        end
        Primitive.matchdata_create(re, str.dup, starts, ends)
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
  end
end
