module Truffle
  class StringOps

    def self.gsub_internal(orig, pattern, replacement=undefined, &block)
      # Because of the behavior of $~, this is duplicated from gsub! because
      # if we call gsub! from gsub, the last_match can't be updated properly.

      unless orig.valid_encoding?
        raise ArgumentError, "invalid byte sequence in #{orig.encoding}"
      end

      if undefined.equal? replacement
        use_yield = true
        tainted = false
      else
        tainted = replacement.tainted?
        untrusted = replacement.untrusted?

        unless replacement.kind_of?(String)
          hash = Rubinius::Type.check_convert_type(replacement, Hash, :to_hash)
          replacement = StringValue(replacement) unless hash
          tainted ||= replacement.tainted?
          untrusted ||= replacement.untrusted?
        end
        use_yield = false
      end

      pattern = Rubinius::Type.coerce_to_regexp(pattern, true) unless pattern.kind_of? Regexp
      match = pattern.search_region(orig, 0, orig.bytesize, true)

      Regexp.set_block_last_match(block, match) if block_given?

      unless match
        return [nil, nil]
      end

      duped = orig.dup

      last_end = 0
      offset = nil

      last_match = nil

      ret = orig.byteslice(0, 0) # Empty string and string subclass
      offset = match.byte_begin(0)

      while match
        if str = match.pre_match_from(last_end)
          ret.append str
        end

        if use_yield || hash
          match_data =  match

          if use_yield
            val = yield match.to_s
          else
            val = hash[match.to_s]
          end
          untrusted = true if val.untrusted?
          val = val.to_s unless val.kind_of?(String)

          tainted ||= val.tainted?

          ret.append val

          if duped != orig.dup
            raise RuntimeError, "string modified"
          end
        else
          replacement.to_sub_replacement(ret, match)
        end

        tainted ||= val.tainted?

        last_end = match.byte_end(0)

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

        match = pattern.match_from orig, offset
        Regexp.set_block_last_match(block, match) if block_given?
        break unless match

        offset = match.byte_begin(0)
      end

      match_data = last_match

      str = orig.byteslice(last_end, orig.bytesize-last_end+1)
      if str
        ret.append str
      end

      ret.taint if tainted
      ret.untrust if untrusted

      [ret, match_data]
    end

  end
end
