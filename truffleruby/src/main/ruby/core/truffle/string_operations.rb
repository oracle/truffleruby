# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle
  module StringOperations

    def self.gsub_block_set_last_match(s, pattern, &block)
      Truffle::StringOperations.gsub_internal_block(s, pattern) do |m, str|
        Regexp.set_block_last_match(block, m)
        yield str
      end
    end

    def self.gsub_internal_block(orig, pattern, &block)
      duped = orig.dup
      gsub_internal_core(orig, pattern) do |ret, m, str|
        val = yield m, str
        if duped != orig.dup
          raise RuntimeError, "string modified"
        end
        val
      end
    end

    def self.gsub_internal(orig, pattern, replacement=undefined, &block)
      unless replacement.kind_of?(String)
        hash = Rubinius::Type.check_convert_type(replacement, Hash, :to_hash)
        replacement = StringValue(replacement) unless hash
      end

      if hash
        gsub_internal_hash(orig, pattern, hash)
      else
        gsub_internal_replacement(orig, pattern, replacement)
      end
    end

    def self.gsub_internal_hash(orig, pattern, replacement)
      gsub_internal_core(orig, pattern, replacement.tainted?, replacement.untrusted? ) do |ret, m, str|
        replacement[str]
      end
    end

    def self.gsub_internal_replacement(orig, pattern, replacement)
      gsub_internal_core(orig, pattern, replacement.tainted?, replacement.untrusted? ) do |ret, m, str|
        replacement.to_sub_replacement(ret, m)
      end
    end

    def self.gsub_internal_core(orig, pattern, tainted=false, untrusted=false)
      unless orig.valid_encoding?
        raise ArgumentError, "invalid byte sequence in #{orig.encoding}"
      end

      pattern = Rubinius::Type.coerce_to_regexp(pattern, true) unless pattern.kind_of? Regexp
      match = pattern.search_region(orig, 0, orig.bytesize, true)

      return nil unless match

      last_end = 0
      last_match = nil
      ret = orig.byteslice(0, 0) # Empty string and string subclass

      while match
        offset = match.byte_begin(0)

        str = match.pre_match_from(last_end)
        ret.append str if str

        val = yield ret, match, match.to_s
        untrusted ||= val.untrusted?
        val = val.to_s
        tainted ||= val.tainted?
        ret.append val

        if match.collapsing?
          if char = orig.find_character(offset)
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
  end
end
