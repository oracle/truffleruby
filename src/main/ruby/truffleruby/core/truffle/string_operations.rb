# frozen_string_literal: true

# Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module StringOperations

    # Similar to MRI's Warning::buffer class
    class SimpleStringIO
      attr_reader :string

      def initialize(str)
        @string = str
      end

      def write(*args)
        @string.concat(*args)
      end
    end

    def self.gsub_block_set_last_match(s, pattern, &block)
      Truffle::StringOperations.gsub_internal_block(s, pattern) do |m|
        Primitive.regexp_last_match_set(Primitive.proc_special_variables(block), m)
        yield m.to_s
      end
    end

    def self.gsub_internal_block(orig, pattern, &block)
      duped = orig.dup
      gsub_internal_core(orig, pattern) do |_ret, m|
        val = yield m
        if duped != orig.dup
          raise RuntimeError, 'string modified'
        end
        val
      end
    end

    def self.gsub_internal(orig, pattern, replacement)
      unless Primitive.object_kind_of?(replacement, String)
        hash = Truffle::Type.rb_check_convert_type(replacement, Hash, :to_hash)
        replacement = StringValue(replacement) unless hash
      end

      if hash
        gsub_internal_hash(orig, pattern, hash)
      else
        gsub_internal_replacement(orig, pattern, replacement)
      end
    end

    def self.gsub_internal_hash(orig, pattern, replacement)
      gsub_internal_core(orig, pattern) do |_ret, m|
        replacement[m.to_s]
      end
    end

    def self.gsub_internal_replacement(orig, pattern, replacement)
      gsub_internal_core(orig, pattern) do |ret, m|
        Truffle::StringOperations.to_sub_replacement(replacement, ret, m)
      end
    end

    def self.gsub_internal_core(orig, pattern)
      unless orig.valid_encoding?
        raise ArgumentError, "invalid byte sequence in #{orig.encoding}"
      end

      if String === pattern
        index = byte_index(orig, pattern, 0)
        match = index ? Primitive.matchdata_create_single_group(pattern, orig.dup, index, index + pattern.bytesize) : nil
      else
        pattern = Truffle::Type.coerce_to_regexp(pattern, true) unless Primitive.object_kind_of?(pattern, Regexp)
        match = Truffle::RegexpOperations.search_region(pattern, orig, 0, orig.bytesize, true)
      end

      return nil unless match

      last_end = 0
      last_match = nil
      ret = orig.byteslice(0, 0) # Empty string and string subclass

      while match
        offset = Primitive.match_data_byte_begin(match, 0)

        str = Truffle::RegexpOperations.pre_match_from(match, last_end)
        Primitive.string_append(ret, str) if str

        val = yield ret, match
        val = val.to_s
        Primitive.string_append(ret, val)

        if Truffle::RegexpOperations.collapsing?(match)
          if (char = Primitive.string_find_character(orig, offset))
            offset += char.bytesize
          else
            offset += 1
          end
        else
          offset = Primitive.match_data_byte_end(match, 0)
        end

        last_match = match
        last_end = Primitive.match_data_byte_end(match, 0)

        if String === pattern
          index = byte_index(orig, pattern, offset)
          match = index ? Primitive.matchdata_create_single_group(pattern, orig.dup, index, index + pattern.bytesize) : nil
        else
          match = Truffle::RegexpOperations.match_from(pattern, orig, offset)
        end
      end

      str = orig.byteslice(last_end, orig.bytesize-last_end+1)
      Primitive.string_append(ret, str) if str

      [ret, last_match]
    end

    def self.concat_internal(string, other)
      Primitive.check_frozen string

      unless Primitive.object_kind_of?(other, String)
        if Primitive.object_kind_of?(other, Integer)
          if string.encoding == Encoding::US_ASCII and other >= 128 and other < 256
            string.force_encoding(Encoding::ASCII_8BIT)
          end

          other = other.chr(string.encoding)
        else
          other = StringValue(other)
        end
      end

      Primitive.string_append(string, other)
    end

    def self.copy_from(string, other, other_offset, byte_count_to_copy, dest_offset)
      sz = string.bytesize
      osz = other.bytesize

      other_offset = 0 if other_offset < 0
      dest_offset = 0 if dest_offset < 0
      byte_count_to_copy = osz - other_offset if byte_count_to_copy > osz - other_offset
      byte_count_to_copy = sz - dest_offset if byte_count_to_copy > sz - dest_offset

      replacement = other.byteslice(other_offset, byte_count_to_copy)
      Primitive.string_splice(string, replacement, dest_offset, byte_count_to_copy, string.encoding)
    end

    def self.case_mapping_option_to_int(option, downcasing=false)
      case option
      when :ascii
        1 << 22 # CASE_ASCII_ONLY
      when :turkic
        1 << 20 # CASE_FOLD_TURKISH_AZERI
      when :lithuanian
        1 << 21 # CASE_FOLD_LITHUANIAN
      when :fold then
        if downcasing
          1 << 19 # CASE_FOLD
        else
          raise ArgumentError, 'option :fold only allowed for downcasing'
        end
      else
        raise ArgumentError, 'invalid option'
      end
    end

    def self.shorten!(string, size)
      return if string.empty?
      Truffle::StringOperations.truncate(string, string.bytesize - size)
    end

    def self.to_sub_replacement(string, result, match)
      index = 0
      while index < string.bytesize
        current = Primitive.find_string(string, '\\', index)
        current = string.bytesize if Primitive.nil? current


        Primitive.string_append(result, string.byteslice(index, current - index))
        break if current == string.bytesize

        # found backslash escape, looking next
        if current == string.bytesize - 1
          Primitive.string_append(result, '\\') # backslash at end of string
          break
        end
        index = current + 1

        cap = string.getbyte(index)

        additional = case cap
                     when 38   # ?&
                       match[0]
                     when 96   # ?`
                       match.pre_match
                     when 39   # ?'
                       match.post_match
                     when 43   # ?+
                       match.captures.compact[-1].to_s
                     when 48..57   # ?0..?9
                       match[cap - 48].to_s
                     when 92 # ?\\ escaped backslash
                       '\\'
                     when 107 # \k named capture
                       if string.getbyte(index + 1) == 60
                         name = +''
                         i = index + 2
                         data = string.bytes
                         while i < string.bytesize && data[i] != 62
                           name << data[i]
                           i += 1
                         end
                         if i >= string.bytesize
                           name << '\\'
                           name << cap.chr
                           index += 1
                           next
                         end
                         index = i
                         name.force_encoding result.encoding
                         match[name]
                       else
                         '\\' + cap.chr
                       end
                     else     # unknown escape
                       '\\' + cap.chr
                     end
        Primitive.string_append(result, additional)
        index += 1
      end
    end

    def self.validate_case_mapping_options(options, downcasing)
      if options.size > 2
        raise ArgumentError, 'too many options'
      end

      if options.size == 0
        0
      elsif options.size == 1
        case_mapping_option_to_int(options.first, downcasing)
      elsif options.size == 2
        first = options[0]
        second = options[1]

        case first
        when :turkic then
          if second == :lithuanian
            case_mapping_option_to_int(:turkic) | case_mapping_option_to_int(:lithuanian)
          else
            raise ArgumentError, 'invalid second option'
          end
        when :lithuanian
          if second == :turkic
            case_mapping_option_to_int(:lithuanian) | case_mapping_option_to_int(:turkic)
          else
            raise ArgumentError, 'invalid second option'
          end
        else raise ArgumentError, 'too many options'
        end
      end
    end

    def self.byte_index(src, str, start=0)
      start += src.bytesize if start < 0
      if start < 0 or start > src.bytesize
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil) if Primitive.object_kind_of?(str, Regexp)
        return nil
      end

      return start if str == ''

      Primitive.encoding_ensure_compatible src, str

      Primitive.string_byte_index(src, str, start)
    end
  end
end
