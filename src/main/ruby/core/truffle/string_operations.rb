# frozen_string_literal: true

# Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle
  module StringOperations

    class SimpleStringIO
      attr_reader :string

      def initialize
        @string = +''
      end

      def write(*args)
        @string.concat(*args)
      end
    end

    def self.gsub_block_set_last_match(s, pattern, &block)
      Truffle::StringOperations.gsub_internal_block(s, pattern) do |m|
        RegexpOperations.set_last_match(m, block.binding)
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
      unless replacement.kind_of?(String)
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
      gsub_internal_core(orig, pattern, replacement.tainted?, replacement.untrusted? ) do |_ret, m|
        replacement[m.to_s]
      end
    end

    def self.gsub_internal_replacement(orig, pattern, replacement)
      gsub_internal_core(orig, pattern, replacement.tainted?, replacement.untrusted? ) do |ret, m|
        replacement.to_sub_replacement(ret, m)
      end
    end

    def self.gsub_internal_core(orig, pattern, tainted=false, untrusted=false)
      unless orig.valid_encoding?
        raise ArgumentError, "invalid byte sequence in #{orig.encoding}"
      end

      pattern = Truffle::Type.coerce_to_regexp(pattern, true) unless pattern.kind_of? Regexp
      match = pattern.search_region(orig, 0, orig.bytesize, true)

      return nil unless match

      last_end = 0
      last_match = nil
      ret = orig.byteslice(0, 0) # Empty string and string subclass

      while match
        offset = match.byte_begin(0)

        str = match.pre_match_from(last_end)
        ret.append str if str

        val = yield ret, match
        untrusted ||= val.untrusted?
        val = val.to_s
        tainted ||= val.tainted?
        ret.append val

        if match.collapsing?
          if (char = orig.find_character(offset))
            offset += char.bytesize
          else
            offset += 1
          end
        else
          offset = match.byte_end(0)
        end

        last_match = match
        last_end = match.byte_end(0)

        match = pattern.match_from orig, offset
      end

      str = orig.byteslice(last_end, orig.bytesize-last_end+1)
      ret.append str if str

      ret.taint if tainted
      ret.untrust if untrusted

      [ret, last_match]
    end

    def self.copy_from(string, other, other_offset, byte_count_to_copy, dest_offset)
      sz = string.bytesize
      osz = other.bytesize

      other_offset = 0 if other_offset < 0
      dest_offset = 0 if dest_offset < 0
      byte_count_to_copy = osz - other_offset if byte_count_to_copy > osz - other_offset
      byte_count_to_copy = sz - dest_offset if byte_count_to_copy > sz - dest_offset

      replacement = other.byteslice(other_offset, byte_count_to_copy)
      Truffle.invoke_primitive(:string_splice, string, replacement, dest_offset, byte_count_to_copy, string.encoding)
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
  end
end
