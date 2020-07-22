# frozen_string_literal: true

# Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
#
# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

module Truffle
  class Splitter
    DEFAULT_PATTERN = ' '

    class << self
      def split(string, pattern, limit, &block)
        # Odd edge case
        return result(string, [], &block) if string.empty?

        if Primitive.undefined?(limit)
          limit = 0
        else
          limit = Primitive.rb_to_int limit
        end

        if limit == 1
          dup_string = string.dup

          ret = add_or_call([], dup_string, &block)
          return result(dup_string, ret, &block)
        end

        pattern ||= ($; || DEFAULT_PATTERN)

        # SPLIT_TYPE_AWK
        if pattern == DEFAULT_PATTERN
          awk_limit = limit < 0 ? -1 : limit

          return Primitive.string_awk_split string, awk_limit, block
        elsif pattern.kind_of?(Regexp)
          # Handle SPLIT_TYPE_REGEXP below
        else
          pattern = StringValue(pattern)

          valid_encoding?(string)
          valid_encoding?(pattern)

          unless limit > 0 # limited
            if pattern.empty?
              unless tail_empty?(limit)
                return split_type_chars(string, limit, &block)
              end
            else
              return split_type_string(string, pattern, limit, &block)
            end
          end

          pattern = Regexp.new(Regexp.quote(pattern))
        end

        # Handle // as a special case.
        if pattern.source.empty?
          return split_type_chars(string, limit, &block)
        end

        split_type_regexp(string, pattern, limit, &block)
      end

      private

      def valid_encoding?(string)
        raise ArgumentError, "invalid byte sequence in #{string.encoding.name}" unless string.valid_encoding?
      end

      def split_type_chars(string, limit, &block)
        if limit > 0
          last = string.size > (limit - 1) ? string[(limit - 1)..-1] : empty_string(string)

          if block_given?
            string.each_char.each_with_index do |char, index|
              break if index == limit - 1
              block.call(char)
            end

            block.call(last)

            string
          else
            string.chars.take(limit - 1) << last
          end
        else
          if block_given?
            string.each_char(&block)

            block.call(empty_string(string)) if tail_empty?(limit)

            string
          else
            ret = string.chars.to_a

            ret << empty_string(string) if tail_empty?(limit)

            ret
          end
        end
      end


      def split_type_string(string, pattern, limit, &block)
        pos = 0
        empty_count = 0

        ret = []

        pat_size = pattern.bytesize
        str_size = string.bytesize

        while pos < str_size
          nxt = Primitive.find_string(string, pattern, pos)
          break unless nxt

          match_size = nxt - pos
          empty_count = add_substring(string, ret, string.byteslice(pos, match_size), empty_count, &block)

          pos = nxt + pat_size
        end

        # No more separators, but we need to grab the last part still.
        empty_count = add_substring(string, ret, string.byteslice(pos, str_size - pos), empty_count, &block)

        if tail_empty?(limit)
          add_empty(string, ret, empty_count, &block)
        end

        result(string, ret, &block)
      end

      def split_type_regexp(string, pattern, limit, &block)
        start = 0
        ret = []
        count = 0
        empty_count = 0
        limited = limit > 0

        last_match = nil
        last_match_end = 0

        while match = Truffle::RegexpOperations.match(pattern, string, start)
          break if limited && limit - count <= 1

          collapsed = Truffle::RegexpOperations.collapsing?(match)

          unless collapsed && (match.byte_begin(0) == last_match_end)
            substring = Truffle::RegexpOperations.pre_match_from(match, last_match_end)
            empty_count = add_substring(string, ret, substring, empty_count, &block)

            # length > 1 means there are captures
            if match.length > 1
              match.captures.compact.each do |capture|
                empty_count = add_substring(string, ret, capture, empty_count, &block)
              end
            end

            count += 1
          end

          start = match.byte_end(0)
          if collapsed
            start += 1
          end

          last_match = match
          last_match_end = last_match.byte_end(0)
        end

        if last_match
          empty_count = add_substring(string, ret, last_match.post_match, empty_count, &block)
        elsif ret.empty?
          empty_count = add_substring(string, ret, string.dup, empty_count, &block)
        end

        if tail_empty?(limit)
          add_empty(string, ret, empty_count, &block)
        end

        result(string, ret, &block)
      end


      def add_substring(string, array, substring, empty_count, &block)
        return empty_count + 1 if substring.length == 0 # remember another one empty match

        add_empty(string, array, empty_count, &block)

        add_or_call(array, substring, &block)

        0 # always release all empties when we get non empty substring
      end

      def add_empty(string, array, count, &block)
        count.times { add_or_call(array, empty_string(string), &block) }
      end

      def add_or_call(array, element, &block)
        if block_given?
          block.call(element)
        else
          array << element
        end
      end

      def empty_string(original)
        # Use #byteslice because it returns the right class and taints automatically.
        original.byteslice(0,0)
      end

      def tail_empty?(limit)
        limit != 0
      end

      def result(string, res, &block)
        return string if block_given?

        res
      end
    end
  end
end
