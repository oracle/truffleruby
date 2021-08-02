# frozen_string_literal: true

# Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module RegexpOperations

    LAST_MATCH_SET = -> v, s {
      unless Primitive.nil?(v) || Primitive.object_kind_of?(v, MatchData)
        raise TypeError, "Wrong argument type #{v} (expected MatchData)"
      end
      Primitive.regexp_last_match_set(s, v)
    }

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
      match_in_region(re, str, from, to, false, 0)
    end

    def self.match(re, str, pos=0)
      return nil unless str

      str = str.to_s if Primitive.object_kind_of?(str, Symbol)
      str = StringValue(str)

      pos = pos < 0 ? pos + str.size : pos
      pos = Primitive.string_byte_index_from_char_index(str, pos)
      search_region(re, str, pos, str.bytesize, true)
    end

    def self.match_from(re, str, pos)
      return nil unless str

      search_region(re, str, pos, str.bytesize, true)
    end

    Truffle::Boot.delay do
      COMPARE_ENGINES = Truffle::Boot.get_option('compare-regex-engines')
      USE_TRUFFLE_REGEX = Truffle::Boot.get_option('use-truffle-regex')

      if Truffle::Boot.get_option('regexp-instrument-creation') or Truffle::Boot.get_option('regexp-instrument-match')
        at_exit do
          Truffle::RegexpOperations.print_stats
        end
      end
    end

    def self.match_in_region(re, str, from, to, at_start, start)
      if COMPARE_ENGINES
        match_in_region_compare_engines(re, str, from, to, at_start, start)
      elsif USE_TRUFFLE_REGEX
        Primitive.regexp_match_in_region_tregex(re, str, from, to, at_start, start)
      else
        Primitive.regexp_match_in_region(re, str, from, to, at_start, start)
      end
    end

    def self.match_in_region_compare_engines(re, str, from, to, at_start, start)
      begin
        md1 = Primitive.regexp_match_in_region_tregex(re, str, from, to, at_start, start)
      rescue => e
        md1 = e
      end
      begin
        md2 = Primitive.regexp_match_in_region(re, str, from, to, at_start, start)
      rescue => e
        md2 = e
      end
      if self.results_match?(md1, md2)
        return self.return_match_data(md1)
      else
        $stderr.puts match_args_to_string(re, str, from, to, at_start, start, 'gave')
        print_match_data(md1)
        $stderr.puts 'but we expected'
        print_match_data(md2)
        return self.return_match_data(md2)
      end
    end

    def self.warn_fallback(re, str, from, to, at_start, start)
      warn match_args_to_string(re, str, from, to, at_start, start, 'cannot be run as a Truffle regexp and fell back to Joni'), uplevel: 1
    end

    def self.warn_fallback_regex(re, at_start, encoding)
      warn "Regexp #{re.inspect} at_start=#{at_start} encoding=#{encoding} cannot be compiled to a Truffle regexp and fell back to Joni", uplevel: 1
    end

    def self.warn_backtracking(re, at_start, encoding)
      warn "Regexp #{re.inspect} at_start=#{at_start} encoding=#{encoding} requires backtracking and will not match in linear time", uplevel: 1
    end

    def self.match_args_to_string(re, str, from, to, at_start, start, suffix)
      "match_in_region(#{re.inspect}, #{str.inspect}@#{str.encoding}, #{from}, #{to}, #{at_start}, #{start}) #{suffix}"
    end

    def self.results_match?(md1, md2)
      if md1 == nil
        md2 == nil
      elsif md2 == nil
        false
      elsif Primitive.object_kind_of?(md1, Exception)
        md1.class == md2.class
      elsif Primitive.object_kind_of?(md2, Exception)
        false
      else
        if md1.size != md2.size
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
      elsif Primitive.object_kind_of?(md, Exception)
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
      if Primitive.object_kind_of?(md, Exception)
        raise md
      else
        md
      end
    end

    def self.compilation_stats_literal_regexp
      Hash[*literal_regexp_compilation_stats_array]
    end

    def self.compilation_stats_dynamic_regexp
      Hash[*dynamic_regexp_compilation_stats_array]
    end

    def self.match_stats_joni
      Hash[*joni_match_stats_array]
    end

    def self.match_stats_tregex
      Hash[*tregex_match_stats_array]
    end

    def self.print_stats
      puts '--------------------'
      puts 'Regular expression statistics'
      puts '--------------------'

      if Truffle::Boot.get_option('regexp-instrument-creation')
        puts '  Compilation (Literal)'
        print_stats_table compilation_stats_literal_regexp
        puts '  --------------------'
        puts '  Compilation (Dynamic)'
        print_stats_table compilation_stats_dynamic_regexp
        puts '  --------------------'
      end

      if Truffle::Boot.get_option('regexp-instrument-match')
        puts '  Matches (Joni)'
        print_stats_table match_stats_joni
        puts '  --------------------'
        puts '  Matches (TRegex)'
        print_stats_table match_stats_tregex
        puts '--------------------'
      end
    end

    def self.print_stats_table(table)
      return if table.empty?
      sorted = table.to_a.sort_by(&:last).reverse
      width = sorted.first.last.to_s.size
      sorted.each do |regexp, count|
        printf "    %#{width}d    %s\n", count, regexp
      end
    end

    def self.option_to_string(option)
      string = +''
      string << 'm' if (option & Regexp::MULTILINE) > 0
      string << 'i' if (option & Regexp::IGNORECASE) > 0
      string << 'x' if (option & Regexp::EXTENDED) > 0
      string
    end

    def self.collapsing?(match)
      Primitive.match_data_byte_begin(match, 0) == Primitive.match_data_byte_end(match, 0)
    end

    def self.pre_match_from(match, idx)
      source = Primitive.match_data_get_source(match)
      match_byte_begin = Primitive.match_data_byte_begin(match, 0)
      return source.byteslice(0, 0) if match_byte_begin == 0

      nd = match_byte_begin - 1
      source.byteslice(idx, nd-idx+1)
    end
  end
end
