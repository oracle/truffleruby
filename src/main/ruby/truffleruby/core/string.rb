# frozen_string_literal: true

# Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

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

# Default Ruby Record Separator
# Used in this file and by various methods that need to ignore $/
DEFAULT_RECORD_SEPARATOR = "\n"

class String
  include Comparable

  Truffle::Graal.always_split(instance_method(:to_sym))

  def byteslice(index_or_range, length=undefined)
    str = TrufflePrimitive.string_byte_substring self, index_or_range, length
    return str unless undefined.equal?(str)

    if index_or_range.kind_of? Range
      index = Truffle::Type.rb_num2int(index_or_range.begin)
      index += bytesize if index < 0
      return if index < 0 or index > bytesize

      finish = Truffle::Type.rb_num2int(index_or_range.end)
      finish += bytesize if finish < 0

      finish += 1 unless index_or_range.exclude_end?
      length = finish - index

      return byteslice 0, 0 if length < 0
    else
      index = Truffle::Type.rb_num2int(index_or_range)
      index += bytesize if index < 0

      if undefined.equal?(length)
        return if index == bytesize
        length = 1
      else
        length = Truffle::Type.rb_num2int(length)
        return if length < 0
      end

      return if index < 0 or index > bytesize
    end

    byteslice index, length
  end

  def self.try_convert(obj)
    Truffle::Type.try_convert obj, String, :to_str
  end

  def =~(pattern)
    case pattern
    when Regexp
      match_data = pattern.search_region(self, 0, bytesize, true)
      Truffle::RegexpOperations.set_last_match(match_data, TrufflePrimitive.caller_binding)
      return match_data.begin(0) if match_data
    when String
      raise TypeError, 'type mismatch: String given'
    else
      pattern =~ self
    end
  end

  def empty?
    bytesize == 0
  end

  def chomp(separator=$/)
    str = dup
    str.chomp!(separator) || str
  end

  def chop
    str = dup
    str.chop! || str
  end

  def delete(*strings)
    str = dup
    str.delete!(*strings) || str
  end

  def delete_prefix(prefix)
    str = dup
    str.delete_prefix!(prefix) || str
  end

  def delete_prefix!(prefix)
    Truffle.check_frozen
    prefix = Truffle::Type.coerce_to(prefix, String, :to_str)
    if !prefix.empty? && start_with?(prefix)
      Truffle::Type.infect self, prefix
      self[0, prefix.size] = ''
      self
    else
      nil
    end
  end

  def delete_suffix(suffix)
    str = dup
    str.delete_suffix!(suffix) || str
  end

  def delete_suffix!(suffix)
    Truffle.check_frozen
    suffix = Truffle::Type.coerce_to(suffix, String, :to_str)
    if !suffix.empty? && end_with?(suffix)
      Truffle::Type.infect self, suffix
      self[size - suffix.size, suffix.size] = ''
      self
    else
      nil
    end
  end

  def include?(needle)
    !!TrufflePrimitive.find_string(self, StringValue(needle), 0)
  end

  def lstrip
    str = dup
    str.lstrip! || str
  end

  def oct
    TrufflePrimitive.string_to_inum(self, -8, false)
  end

  # Treats leading characters from <i>self</i> as a string of hexadecimal digits
  # (with an optional sign and an optional <code>0x</code>) and returns the
  # corresponding number. Zero is returned on error.
  #
  #    "0x0a".hex     #=> 10
  #    "-1234".hex    #=> -4660
  #    "0".hex        #=> 0
  #    "wombat".hex   #=> 0
  def hex
    TrufflePrimitive.string_to_inum(self, 16, false)
  end

  def reverse
    dup.reverse!
  end

  def partition(pattern=nil)
    return super() if pattern == nil && block_given?

    if pattern.kind_of? Regexp
      if m = Truffle::RegexpOperations.match(pattern, self)
        Truffle::RegexpOperations.set_last_match(m, TrufflePrimitive.caller_binding)
        return [m.pre_match, m.to_s, m.post_match]
      end
    else
      pattern = StringValue(pattern)
      if i = index(pattern)
        post_start = i + pattern.length
        post_len = size - post_start

        return [substring(0, i),
                pattern,
                substring(post_start, post_len)]
      end
    end

    # Nothing worked out, this is the default.
    empty = String.new(encoding: encoding)
    [self, empty, empty.dup]
  end

  def rpartition(pattern)
    if pattern.kind_of? Regexp
      if m = pattern.search_region(self, 0, size, false)
        Truffle::RegexpOperations.set_last_match(m, TrufflePrimitive.caller_binding)
        [m.pre_match, m[0], m.post_match]
      end
    else
      pattern = StringValue(pattern)
      if i = rindex(pattern)
        post_start = i + pattern.length
        post_len = size - post_start

        return [substring(0, i),
                pattern.dup,
                substring(post_start, post_len)]
      end

      # Nothing worked out, this is the default.
      empty = String.new(encoding: encoding)
      [empty, empty.dup, self]
    end
  end

  def rstrip
    str = dup
    str.rstrip! || str
  end

  def scan(pattern, &block)
    taint = tainted? || pattern.tainted?
    pattern = Truffle::Type.coerce_to_regexp(pattern, true)
    index = 0

    last_match = nil
    ret = block_given? ? self : []

    while match = pattern.match_from(self, index)
      fin = match.byte_end(0)

      if match.collapsing?
        if char = find_character(fin)
          index = fin + char.bytesize
        else
          index = fin + 1
        end
      else
        index = fin
      end

      last_match = match
      val = (match.length == 1 ? match[0] : match.captures)
      val.taint if taint

      if block
        Truffle::RegexpOperations.set_last_match(match, TrufflePrimitive.caller_binding)
        yield(val)
      else
        ret << val
      end
    end

    Truffle::RegexpOperations.set_last_match(last_match, TrufflePrimitive.caller_binding)
    ret
  end

  def split(pattern=nil, limit=undefined)
    Truffle::Splitter.split(self, pattern, limit)
  end

  def squeeze(*strings)
    str = dup
    str.squeeze!(*strings) || str
  end

  def strip
    str = dup
    str.strip! || str
  end

  def strip!
    right = rstrip! # Process rstrip! first because it must perform an encoding compatibility check that lstrip! does not.
    left = lstrip!
    left.nil? && right.nil? ? nil : self
  end

  def succ
    dup.succ!
  end

  def swapcase!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, false)
    TrufflePrimitive.swapcase! self, mapped_options
  end

  def swapcase(*options)
    str = dup
    str.swapcase!(*options) || str
  end

  def to_i(base=10)
    base = Truffle::Type.coerce_to base, Integer, :to_int

    if base < 0 || base == 1 || base > 36
      raise ArgumentError, "illegal radix #{base}"
    end

    TrufflePrimitive.string_to_inum(self, base, false)
  end

  def tr(source, replacement)
    str = dup
    str.tr!(source, replacement) || str
  end
  Truffle::Graal.always_split(instance_method(:tr))

  def tr_s(source, replacement)
    str = dup
    str.tr_s!(source, replacement) || str
  end

  def to_sub_replacement(result, match)
    index = 0
    while index < bytesize
      current = TrufflePrimitive.find_string(self, '\\', index)
      current = bytesize if current.nil?

      result.append(byteslice(index, current - index))
      break if current == bytesize

      # found backslash escape, looking next
      if current == bytesize - 1
        result.append('\\') # backslash at end of string
        break
      end
      index = current + 1

      cap = getbyte(index)

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
                     if getbyte(index + 1) == 60
                       name = +''
                       i = index + 2
                       data = bytes
                       while i < bytesize && data[i] != 62
                         name << data[i]
                         i += 1
                       end
                       if i >= bytesize
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
      result.append(additional)
      index += 1
    end
  end

  def subpattern(pattern, capture)
    match = Truffle::RegexpOperations.match(pattern, self)

    return nil unless match

    if index = Truffle::Type.rb_check_convert_type(capture, Integer, :to_int)
      return nil if index >= match.size || -index >= match.size
      capture = index
    end

    str = match[capture]
    Truffle::Type.infect str, pattern
    [match, str]
  end
  private :subpattern

  def shorten!(size)
    return if empty?
    Truffle::StringOperations.truncate(self, bytesize - size)
  end

  def each_codepoint
    return to_enum(:each_codepoint) { size } unless block_given?

    each_char { |c| yield c.ord }
    self
  end

  def chars(&block)
    if block_given?
      each_char(&block)
    else
      each_char.to_a
    end
  end

  def codepoints(&block)
    if block_given?
      each_codepoint(&block)
    else
      each_codepoint.to_a
    end
  end

  def encode!(to=undefined, from=undefined, options=undefined)
    Truffle.check_frozen

    if undefined.equal?(to)
      to_enc = Encoding.default_internal
      return self unless to_enc
    else
      case to
      when Encoding
        to_enc = to
      when Hash
        options = to
        to_enc = Encoding.default_internal
      else
        opts = Truffle::Type.rb_check_convert_type to, Hash, :to_hash

        if opts
          options = opts
          to_enc = Encoding.default_internal
        else
          to_enc = Encoding.try_convert(to)
        end
      end
    end

    from = encoding if undefined.equal?(from)
    case from
    when Encoding
      from_enc = from
    when Hash
      options = from
      from_enc = encoding
    else
      opts = Truffle::Type.rb_check_convert_type from, Hash, :to_hash

      if opts
        options = opts
        from_enc = encoding
      else
        from_enc = Truffle::Type.coerce_to_encoding from
      end
    end

    if false == to_enc
      raise Encoding::ConverterNotFoundError, "undefined code converter (#{from} to #{to})"
    end

    if undefined.equal?(options)
      options = 0
    else
      case options
      when Hash
        # do nothing
      else
        options = Truffle::Type.coerce_to options, Hash, :to_hash
      end
    end

    if ascii_only? and from_enc.ascii_compatible? and to_enc and to_enc.ascii_compatible?
      force_encoding to_enc
    elsif to_enc
      if from_enc != to_enc
        ec = Encoding::Converter.new from_enc, to_enc, options
        dest = +''
        status = ec.primitive_convert self.dup, dest, nil, nil, ec.options
        raise ec.last_error unless status == :finished
        return replace(dest)
      elsif options == 0
        force_encoding to_enc
      end
    end

    # TODO: replace this hack with transcoders
    if options.kind_of? Hash
      case options[:invalid]
      when :replace
        self.scrub!
      end
      case xml = options[:xml]
      when :text
        gsub!(/[&><]/, '&' => '&amp;', '>' => '&gt;', '<' => '&lt;')
      when :attr
        gsub!(/[&><"]/, '&' => '&amp;', '>' => '&gt;', '<' => '&lt;', '"' => '&quot;')
        insert(0, '"')
        insert(-1, '"')
      when nil
        # nothing
      else
        raise ArgumentError, "unexpected value for xml option: #{xml.inspect}"
      end

      if options[:universal_newline]
        gsub!(/\r\n|\r/, "\r\n" => "\n", "\r" => "\n")
      end
    end

    self
  end

  def b
    dup.force_encoding(Encoding::BINARY)
  end

  def encode(to=undefined, from=undefined, options=undefined)
    dup.encode! to, from, options
  end

  def end_with?(*suffixes)
    suffixes.each do |original_suffix|
      suffix = Truffle::Type.rb_convert_type original_suffix, String, :to_str
      Truffle::Type.compatible_encoding self, suffix

      return true if self[-suffix.length, suffix.length] == suffix
    end

    false
  end

  def inspect
    result_encoding = Encoding.default_internal || Encoding.default_external
    unless result_encoding.ascii_compatible?
      result_encoding = Encoding::US_ASCII
    end

    enc = encoding
    ascii = enc.ascii_compatible?
    unicode = TrufflePrimitive.encoding_is_unicode enc

    actual_encoding = TrufflePrimitive.get_actual_encoding(self)
    if actual_encoding != enc
      enc = actual_encoding
      if unicode
        unicode = TrufflePrimitive.encoding_is_unicode enc
      end
    end

    array = []

    index = 0
    total = bytesize
    while index < total
      char = chr_at index

      if char
        index += inspect_char(enc, result_encoding, ascii, unicode, index, char, array)
      else
        array << "\\x#{getbyte(index).to_s(16)}"
        index += 1
      end
    end

    size = array.inject(0) { |s, chr| s + chr.bytesize }
    result = String.pattern size + 2, ?".ord

    index = 1
    array.each do |chr|
      Truffle::StringOperations.copy_from(result, chr, 0, chr.bytesize, index)
      index += chr.bytesize
    end

    Truffle::Type.infect result, self
    result.force_encoding(result_encoding)
  end

  def inspect_char(enc, result_encoding, ascii, unicode, index, char, array)
    consumed = char.bytesize

    if (ascii or unicode) and consumed == 1
      escaped = nil

      byte = getbyte(index)
      if byte >= 7 and byte <= 92
        case byte
        when 7  # \a
          escaped = '\a'
        when 8  # \b
          escaped = '\b'
        when 9  # \t
          escaped = '\t'
        when 10 # \n
          escaped = '\n'
        when 11 # \v
          escaped = '\v'
        when 12 # \f
          escaped = '\f'
        when 13 # \r
          escaped = '\r'
        when 27 # \e
          escaped = '\e'
        when 34 # \"
          escaped = '\"'
        when 35 # #
          case getbyte(index + 1)
          when 36   # $
            escaped = '\#$'
            consumed += 1
          when 64   # @
            escaped = '\#@'
            consumed += 1
          when 123  # {
            escaped = '\#{'
            consumed += 1
          end
        when 92 # \\
          escaped = '\\\\'
        end

        if escaped
          array << escaped
          return consumed
        end
      end
    end

    if TrufflePrimitive.character_printable_p(char) && (enc == result_encoding || (ascii && char.ascii_only?))
      array << char
    else
      code = char.ord
      escaped = code.to_s(16).upcase

      if unicode
        if code < 0x10000
          pad = '0' * (4 - escaped.bytesize)
          array << "\\u#{pad}#{escaped}"
        else
          array << "\\u{#{escaped}}"
        end
      else
        if code < 0x100
          pad = '0' * (2 - escaped.bytesize)
          array << "\\x#{pad}#{escaped}"
        else
          array << "\\x{#{escaped}}"
        end
      end
    end

    consumed
  end
  private :inspect_char

  def prepend(*others)
    if others.size == 1
      replace(StringValue(others.first) + self)
    else
      reduced = others.reduce(''.encode(self.encoding)) { |memo, other| memo + StringValue(other) }
      replace(StringValue(reduced) + self)
    end
  end

  def upto(stop, exclusive=false)
    return to_enum :upto, stop, exclusive unless block_given?
    stop = StringValue(stop)

    if stop.size == 1 && size == 1
      enc = Truffle::Type.compatible_encoding(self.encoding, stop.encoding)

      return self if self > stop
      after_stop = stop.getbyte(0) + (exclusive ? 0 : 1)
      current = getbyte(0)
      until current == after_stop
        yield current.chr(enc)
        current += 1
      end
    else
      unless stop.size < size
        after_stop = exclusive ? stop : stop.succ
        current = self

        begin
          # If both the start and end values are strings representing integer values, the encoding must be US-ASCII.
          TrufflePrimitive.string_to_inum(self, 10, true)
          TrufflePrimitive.string_to_inum(stop, 10, true)
          enc = Encoding::US_ASCII
        rescue ArgumentError
          enc = self.encoding
        end

        until current == after_stop
          yield current
          current = StringValue(current.succ).force_encoding(enc)
          break if current.size > stop.size || current.empty?
        end
      end
    end
    self
  end

  def sub(pattern, replacement=undefined, &block)
    s = dup
    s.sub!(pattern, replacement, &block)
    Truffle::RegexpOperations.set_last_match($~, TrufflePrimitive.caller_binding)
    s
  end

  def sub!(pattern, replacement=undefined, &block)
    # Because of the behavior of $~, this is duplicated from sub! because
    # if we call sub! from sub, the last_match can't be updated properly.

    unless valid_encoding?
      raise ArgumentError, "invalid byte sequence in #{encoding}"
    end

    if undefined.equal? replacement
      unless block_given?
        raise ArgumentError, "method '#{__method__}': given 1, expected 2"
      end
      Truffle.check_frozen
      use_yield = true
      tainted = false
    else
      Truffle.check_frozen
      tainted = replacement.tainted?
      untrusted = replacement.untrusted?

      unless replacement.kind_of?(String)
        hash = Truffle::Type.rb_check_convert_type(replacement, Hash, :to_hash)
        replacement = StringValue(replacement) unless hash
        tainted ||= replacement.tainted?
        untrusted ||= replacement.untrusted?
      end
      use_yield = false
    end

    pattern = Truffle::Type.coerce_to_regexp(pattern, true) unless pattern.kind_of? Regexp
    match = pattern.match_from(self, 0)

    Truffle::RegexpOperations.set_last_match(match, block.binding) if block
    Truffle::RegexpOperations.set_last_match(match, TrufflePrimitive.caller_binding)

    if match
      ret = match.pre_match

      if use_yield || hash
        duped = dup
        if use_yield
          val = yield match.to_s
        else
          val = hash[match.to_s]
        end
        if duped != self
          raise RuntimeError, 'string modified'
        end
        untrusted = true if val.untrusted?
        val = val.to_s unless val.kind_of?(String)

        tainted ||= val.tainted?

        ret.append val
      else
        replacement.to_sub_replacement(ret, match)
      end

      ret.append(match.post_match)
      tainted ||= val.tainted?

      ret.taint if tainted
      ret.untrust if untrusted

      replace(ret)
      self
    else
      nil
    end
  end
  Truffle::Graal.always_split instance_method(:sub!)

  def slice!(one, two=undefined)
    Truffle.check_frozen
    # This is un-DRY, but it's a simple manual argument splitting. Keeps
    # the code fast and clean since the sequence are pretty short.
    #
    if undefined.equal?(two)
      result = slice(one)

      if one.kind_of? Regexp
        lm = $~
        self[one] = '' if result
        Truffle::RegexpOperations.set_last_match(lm, TrufflePrimitive.caller_binding)
      else
        self[one] = '' if result
      end
    else
      result = slice(one, two)

      if one.kind_of? Regexp
        lm = $~
        self[one, two] = '' if result
        Truffle::RegexpOperations.set_last_match(lm, TrufflePrimitive.caller_binding)
      else
        self[one, two] = '' if result
      end
    end

    result
  end

  alias_method :next, :succ
  alias_method :next!, :succ!

  def to_c
    Complexifier.new(self).convert
  end

  def to_r
    Rationalizer.new(self).convert
  end

  def chop!
    Truffle.check_frozen

    bytes = TrufflePrimitive.string_previous_byte_index(self, bytesize)
    return unless bytes

    chr = chr_at bytes
    if chr.ord == 10
      if i = TrufflePrimitive.string_previous_byte_index(self, bytes)
        chr = chr_at i

        bytes = i if chr.ord == 13
      end
    end

    Truffle::StringOperations.truncate(self, bytes)

    self
  end

  def chomp!(sep=undefined)
    Truffle.check_frozen

    if undefined.equal?(sep)
      sep = $/
    elsif sep
      sep = StringValue(sep)
    end

    return if sep.nil?

    if sep == DEFAULT_RECORD_SEPARATOR
      return unless bytes = TrufflePrimitive.string_previous_byte_index(self, bytesize)

      chr = chr_at bytes

      case chr.ord
      when 13
        # do nothing
      when 10
        if j = TrufflePrimitive.string_previous_byte_index(self, bytes)
          chr = chr_at j

          if chr.ord == 13
            bytes = j
          end
        end
      else
        return
      end
    elsif sep.empty?
      return if empty?
      bytes = bytesize

      while i = TrufflePrimitive.string_previous_byte_index(self, bytes)
        chr = chr_at i
        break unless chr.ord == 10

        bytes = i

        if j = TrufflePrimitive.string_previous_byte_index(self, i)
          chr = chr_at j
          if chr.ord == 13
            bytes = j
          end
        end
      end

      return if bytes == bytesize
    else
      sep_bytesize = sep.bytesize
      return if sep_bytesize > bytesize

      return unless self.end_with?(sep)
      bytes = bytesize - sep_bytesize
    end

    Truffle::StringOperations.truncate(self, bytes)

    self
  end

  def concat_internal(other)
    Truffle.check_frozen

    unless other.kind_of? String
      if other.kind_of? Integer
        if encoding == Encoding::US_ASCII and other >= 128 and other < 256
          force_encoding(Encoding::ASCII_8BIT)
        end

        other = other.chr(encoding)
      else
        other = StringValue(other)
      end
    end

    Truffle::Type.infect(self, other)
    append(other)
  end

  def chr
    substring 0, 1
  end

  def each_line(sep=$/, chomp: false)
    unless block_given?
      return to_enum(:each_line, sep, chomp: chomp)
    end

    # weird edge case.
    if sep.nil?
      yield self
      return self
    end

    maybe_chomp = ->(str) { chomp ? str.chomp(sep) : str }

    sep = StringValue(sep)

    pos = 0

    duped = dup

    # If the separator is empty, we're actually in paragraph mode. This
    # is used so infrequently, we'll handle it completely separately from
    # normal line breaking.
    if sep.empty?
      sep = "\n\n"
      data = bytes

      while pos < bytesize
        nxt = TrufflePrimitive.find_string(self, sep, pos)
        break unless nxt

        while data[nxt] == 10 and nxt < bytesize
          nxt += 1
        end

        match_size = nxt - pos

        # string ends with \n's
        break if pos == bytesize

        str = byteslice pos, match_size
        yield maybe_chomp.call(str) unless str.empty?

        # detect mutation within the block
        if duped != self
          raise RuntimeError, 'string modified while iterating'
        end

        pos = nxt
      end

      # No more separates, but we need to grab the last part still.
      fin = byteslice pos, bytesize - pos
      yield maybe_chomp.call(fin) if fin and !fin.empty?
    else

      # This is the normal case.
      pat_size = sep.bytesize
      unmodified_self = clone

      while pos < bytesize
        nxt = TrufflePrimitive.find_string(unmodified_self, sep, pos)
        break unless nxt

        match_size = nxt - pos
        str = unmodified_self.byteslice pos, match_size + pat_size
        yield maybe_chomp.call(str) unless str.empty?

        pos = nxt + pat_size
      end

      # No more separates, but we need to grab the last part still.
      fin = unmodified_self.byteslice pos, bytesize - pos
      yield maybe_chomp.call(fin) unless fin.empty?
    end

    self
  end

  def lines(sep=$/, chomp: false, &block)
    if block_given?
      each_line(sep, chomp: chomp, &block)
    else
      each_line(sep, chomp: chomp).to_a
    end
  end


  def gsub(pattern, replacement=undefined, &block)
    s = dup
    if undefined.equal?(replacement) && !block_given?
      return s.to_enum(:gsub, pattern, replacement)
    end
    if undefined.equal?(replacement)
      ret, match_data = Truffle::StringOperations.gsub_block_set_last_match(s, pattern, &block)
    else
      ret, match_data = Truffle::StringOperations.gsub_internal(s, pattern, replacement)
    end
    Truffle::RegexpOperations.set_last_match(match_data, TrufflePrimitive.caller_binding)
    s.replace(ret) if ret
    s
  end

  def gsub!(pattern, replacement=undefined, &block)
    if undefined.equal?(replacement) && !block_given?
      return to_enum(:gsub!, pattern, replacement)
    end
    Truffle.check_frozen
    if undefined.equal?(replacement)
      ret, match_data = Truffle::StringOperations.gsub_block_set_last_match(self, pattern, &block)
    else
      ret, match_data = Truffle::StringOperations.gsub_internal(self, pattern, replacement)
    end
    Truffle::RegexpOperations.set_last_match(match_data, TrufflePrimitive.caller_binding)
    if ret
      replace(ret)
      self
    else
      nil
    end
  end

  def match(pattern, pos=0)
    pattern = Truffle::Type.coerce_to_regexp(pattern) unless pattern.kind_of? Regexp

    result = if block_given?
               pattern.match self, pos do |match|
                 yield match
               end
             else
               pattern.match self, pos
             end
    Truffle::RegexpOperations.set_last_match($~, TrufflePrimitive.caller_binding)
    result
  end

  def match?(pattern, pos=0)
    pattern = Truffle::Type.coerce_to_regexp(pattern) unless pattern.kind_of? Regexp
    pattern.match? self, pos
  end

  def scrub(replace = nil, &block)
    return dup if valid_encoding?

    if !replace and !block
      # The unicode replacement character or '?''
      begin
        replace = "\xEF\xBF\xBD".encode(self.encoding, :undef => :replace, :replace => '?')
      rescue Encoding::ConverterNotFoundError
        replace = '?'.encode(encoding)
      end
    end

    taint = tainted?

    validate = -> str {
      str = StringValue(str)
      unless str.valid_encoding?
        raise ArgumentError, 'replacement must be valid byte sequence'
      end
      # Special encoding check for #scrub
      if str.ascii_only? ? !encoding.ascii_compatible? : encoding != str.encoding
        raise Encoding::CompatibilityError, 'incompatible character encodings'
      end
      # Modifies the outer taint variable
      taint = true if str.tainted?
      str
    }

    if replace
      replace = validate.call(replace)
      replace_block = Proc.new { |_broken| replace }
    else
      replace_block = Proc.new do |broken|
        validate.call(block.call(broken))
      end
    end

    val = TrufflePrimitive.string_scrub(self, replace_block)
    val.taint if taint
    val
  end

  def scrub!(replace = nil, &block)
    replace(scrub(replace, &block))
    self
  end

  def []=(index, count_or_replacement, replacement=undefined)
    Truffle.check_frozen

    if undefined.equal?(replacement)
      replacement = count_or_replacement
      count = nil
    else
      count = count_or_replacement
    end

    case index
    when Integer
      assign_index(index, count, replacement)
    when String
      assign_string(index, replacement)
    when Range
      assign_range(index, replacement)
    when Regexp
      assign_regexp(index, count, replacement)
    else
      index = Truffle::Type.coerce_to_int index

      if count
        return self[index, count] = replacement
      else
        return self[index] = replacement
      end
    end

    Truffle::Type.infect self, replacement

    replacement
  end

  def assign_index(index, count, replacement)
    index += size if index < 0

    if index < 0 or index > size
      raise IndexError, "index #{index} out of string"
    end

    unless bi = TrufflePrimitive.string_byte_index_from_char_index(self, index)
      raise IndexError, "unable to find character at: #{index}"
    end

    if count
      count = Truffle::Type.coerce_to_int count

      if count < 0
        raise IndexError, 'count is negative'
      end

      total = index + count
      if total >= size
        bs = bytesize - bi
      else
        bs = TrufflePrimitive.string_byte_index_from_char_index(self, total) - bi
      end
    else
      bs = index == size ? 0 : TrufflePrimitive.string_byte_index_from_char_index(self, index + 1) - bi
    end

    replacement = StringValue replacement
    enc = Truffle::Type.compatible_encoding self, replacement

    TrufflePrimitive.string_splice(self, replacement, bi, bs, enc)
  end

  def assign_string(index, replacement)
    unless start = TrufflePrimitive.find_string(self, index, 0)
      raise IndexError, 'string not matched'
    end

    replacement = StringValue replacement
    enc = Truffle::Type.compatible_encoding self, replacement

    TrufflePrimitive.string_splice(self, replacement, start, index.bytesize, enc)
  end

  def assign_range(index, replacement)
    start = Truffle::Type.coerce_to_int index.first

    start += size if start < 0

    if start < 0 or start > size
      raise RangeError, "#{index.first} is out of range"
    end

    unless bi = TrufflePrimitive.string_byte_index_from_char_index(self, start)
      raise IndexError, "unable to find character at: #{start}"
    end

    stop = Truffle::Type.coerce_to_int index.last
    stop += size if stop < 0
    stop -= 1 if index.exclude_end?

    if stop < start
      bs = 0
    elsif stop >= size
      bs = bytesize - bi
    else
      bs = TrufflePrimitive.string_byte_index_from_char_index(self, stop + 1) - bi
    end

    replacement = StringValue replacement
    enc = Truffle::Type.compatible_encoding self, replacement

    TrufflePrimitive.string_splice(self, replacement, bi, bs, enc)
  end

  def assign_regexp(index, count, replacement)
    if count
      count = Truffle::Type.coerce_to_int count
    else
      count = 0
    end

    if match = Truffle::RegexpOperations.match(index, self)
      ms = match.size
    else
      raise IndexError, 'regexp does not match'
    end

    count += ms if count < 0 and -count < ms
    unless count < ms and count >= 0
      raise IndexError, "index #{count} out of match bounds"
    end

    unless match[count]
      raise IndexError, "regexp group #{count} not matched"
    end

    replacement = StringValue replacement
    enc = Truffle::Type.compatible_encoding self, replacement

    bi = TrufflePrimitive.string_byte_index_from_char_index(self, match.begin(count))
    bs = TrufflePrimitive.string_byte_index_from_char_index(self, match.end(count)) - bi

    TrufflePrimitive.string_splice(self, replacement, bi, bs, enc)
  end

  private :assign_index, :assign_string, :assign_range, :assign_regexp

  def center(width, padding=' ')
    padding = StringValue(padding)
    raise ArgumentError, 'zero width padding' if padding.empty?

    enc = Truffle::Type.compatible_encoding self, padding

    width = Truffle::Type.coerce_to_int width
    return dup if width <= size

    width -= size
    left = width / 2

    bs = bytesize
    pbs = padding.bytesize

    if pbs > 1
      ps = padding.size
      x = left / ps
      y = left % ps

      lpbi = TrufflePrimitive.string_byte_index_from_char_index(padding, y)
      lbytes = x * pbs + lpbi

      right = left + (width & 0x1)
      x = right / ps
      y = right % ps

      rpbi = TrufflePrimitive.string_byte_index_from_char_index(padding, y)
      rbytes = x * pbs + rpbi

      pad = self.class.pattern rbytes, padding
      str = self.class.pattern lbytes + bs + rbytes, ''

      Truffle::StringOperations.copy_from(str, self, 0, bs, lbytes)
      Truffle::StringOperations.copy_from(str, pad, 0, lbytes, 0)
      Truffle::StringOperations.copy_from(str, pad, 0, rbytes, lbytes + bs)
    else
      str = self.class.pattern width + bs, padding
      Truffle::StringOperations.copy_from(str, self, 0, bs, left)
    end

    str.taint if tainted? or padding.tainted?
    str.force_encoding enc
  end

  def ljust(width, padding=' ')
    padding = StringValue(padding)
    raise ArgumentError, 'zero width padding' if padding.empty?

    enc = Truffle::Type.compatible_encoding self, padding

    width = Truffle::Type.coerce_to_int width
    return dup if width <= size

    width -= size

    bs = bytesize
    pbs = padding.bytesize

    if pbs > 1
      ps = padding.size
      x = width / ps
      y = width % ps

      pbi = TrufflePrimitive.string_byte_index_from_char_index(padding, y)
      bytes = x * pbs + pbi

      str = self.class.pattern bytes + bs, self

      i = 0
      bi = bs

      while i < x
        Truffle::StringOperations.copy_from(str, padding, 0, pbs, bi)

        bi += pbs
        i += 1
      end

      Truffle::StringOperations.copy_from(str, padding, 0, pbi, bi)
    else
      str = self.class.pattern width + bs, padding
      Truffle::StringOperations.copy_from(str, self, 0, bs, 0)
    end

    str.taint if tainted? or padding.tainted?
    str.force_encoding enc
  end

  def rjust(width, padding=' ')
    padding = StringValue(padding)
    raise ArgumentError, 'zero width padding' if padding.empty?

    enc = Truffle::Type.compatible_encoding self, padding

    width = Truffle::Type.coerce_to_int width
    return dup if width <= size

    width -= size

    bs = bytesize
    pbs = padding.bytesize

    if pbs > 1
      ps = padding.size
      x = width / ps
      y = width % ps

      bytes = x * pbs + TrufflePrimitive.string_byte_index_from_char_index(padding, y)
    else
      bytes = width
    end

    str = self.class.pattern bytes + bs, padding

    Truffle::StringOperations.copy_from(str, self, 0, bs, bytes)

    str.taint if tainted? or padding.tainted?
    str.force_encoding enc
  end

  def index(str, start=undefined)
    if undefined.equal?(start)
      start = 0
    else
      start = Truffle::Type.coerce_to_int start

      start += size if start < 0
      if start < 0 or start > size
        Truffle::RegexpOperations.set_last_match(nil, TrufflePrimitive.caller_binding) if str.kind_of? Regexp
        return
      end
    end

    if str.kind_of? Regexp
      Truffle::Type.compatible_encoding self, str

      start = TrufflePrimitive.string_byte_index_from_char_index(self, start)
      if match = str.match_from(self, start)
        Truffle::RegexpOperations.set_last_match(match, TrufflePrimitive.caller_binding)
        return match.begin(0)
      else
        Truffle::RegexpOperations.set_last_match(nil, TrufflePrimitive.caller_binding)
        return
      end
    end

    str = StringValue(str)
    return start if str == ''

    Truffle::Type.compatible_encoding self, str

    return if str.size > size

    TrufflePrimitive.string_character_index(self, str, start)
  end

  def initialize(other = undefined, capacity: nil, encoding: nil)
    unless undefined.equal?(other)
      Truffle.check_frozen
      TrufflePrimitive.string_initialize(self, other, encoding)
      taint if other.tainted?
    end
    self.force_encoding(encoding) if encoding
    self
  end

  def rindex(sub, finish=undefined)
    if undefined.equal?(finish)
      finish = size
    else
      finish = Truffle::Type.coerce_to(finish, Integer, :to_int)
      finish += size if finish < 0
      return nil if finish < 0
      finish = size if finish >= size
    end

    byte_finish = TrufflePrimitive.string_byte_index_from_char_index(self, finish)

    case sub
    when Integer
      if finish == size
        return nil if finish == 0
      end

      begin
        str = sub.chr
      rescue RangeError
        return nil
      end

      if byte_index = TrufflePrimitive.find_string_reverse(self, str, byte_finish)
        return TrufflePrimitive.string_byte_character_index(self, byte_index)
      end

    when Regexp
      Truffle::Type.compatible_encoding self, sub

      match_data = sub.search_region(self, 0, byte_finish, false)
      Truffle::RegexpOperations.set_last_match(match_data, TrufflePrimitive.caller_binding)
      return match_data.begin(0) if match_data

    else
      needle = StringValue(sub)
      needle_size = needle.size

      # needle is bigger that haystack
      return nil if size < needle_size

      # Boundary case
      return finish if needle.empty?

      Truffle::Type.compatible_encoding self, needle
      if byte_index = TrufflePrimitive.find_string_reverse(self, needle, byte_finish)
        return TrufflePrimitive.string_byte_character_index(self, byte_index)
      end
    end

    nil
  end

  def start_with?(*prefixes)
    prefixes.each do |original_prefix|
      prefix = Truffle::Type.rb_check_convert_type original_prefix, String, :to_str
      unless prefix
        raise TypeError, "no implicit conversion of #{original_prefix.class} into String"
      end
      return true if self[0, prefix.length] == prefix
    end
    false
  end

  def insert(index, other)
    other = StringValue(other)

    index = Truffle::Type.coerce_to_int index
    index = length + 1 + index if index < 0

    if index > length or index < 0 then
      raise IndexError, "index #{index} out of string"
    end

    Truffle.check_frozen

    if index == 0
      replace(other + self)
    elsif index == length
      self << other
    else
      left = self[0...index]
      right = self[index..-1]
      replace(left + other + right)
    end

    Truffle::Type.infect self, other
    self
  end

  def %(args)
    if args.is_a? Hash
      sprintf(self, args)
    else
      result = Truffle::Type.rb_check_convert_type args, Array, :to_ary
      if result.nil?
        sprintf(self, args)
      else
        sprintf(self, *result)
      end
    end
  end
  Truffle::Graal.always_split instance_method(:%)

  def capitalize!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, false)
    TrufflePrimitive.capitalize! self, mapped_options
  end

  def capitalize(*options)
    s = dup
    s.capitalize!(*options)
    s
  end

  def downcase!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, true)
    TrufflePrimitive.downcase! self, mapped_options
  end

  def downcase(*options)
    s = dup
    s.downcase!(*options)
    s
  end

  def upcase!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, false)
    TrufflePrimitive.upcase! self, mapped_options
  end

  def upcase(*options)
    s = dup
    s.upcase!(*options)
    s
  end

  def casecmp?(other)
    other = StringValue(other)

    enc = Encoding.compatible?(encoding, other.encoding)
    if enc.nil?
      return nil
    end

    if ascii_only? && other.ascii_only?
      casecmp(other) == 0
    else
      downcase(:fold).casecmp(other.downcase(:fold)) == 0
    end
  end

  def +@
    frozen? ? dup : self
  end

  def -@
    str = frozen? ? self : dup.freeze
    if str.tainted? || !(str.instance_variables).empty?
      str
    else
      Truffle::Ropes.flatten_rope(str)
      TrufflePrimitive.string_intern(str)
    end
  end

  def encoding
    TrufflePrimitive.encoding_get_object_encoding self
  end

  def <=>(other)
    if String === other
      return TrufflePrimitive.string_cmp self, other
    end

    Thread.detect_recursion self, other do
      if other.respond_to?(:<=>) && !other.respond_to?(:to_str)
        return nil unless tmp = (other <=> self)
      elsif other.respond_to?(:to_str)
        return nil unless tmp = (other.to_str <=> self)
      else
        return nil
      end

      # Normalize the results to be in {-1, 0, 1}. Also, since at this point we've inverted the objects being compared,
      # we must now invert the results.
      return -1 if tmp > 0
      return 1  if tmp < 0
      return 0
    end

    nil # Fallback value if recursive calls are detected.
  end

  def crypt(salt)
    salt = StringValue(salt)
    raise ArgumentError, 'salt too short (need >= 2 bytes)' if salt.bytesize < 2 || salt[0] == "\0" || salt[1] == "\0"
    raise ArgumentError, 'string contains null byte' if include?("\0")
    crypted = Truffle::POSIX.crypt(self, salt)
    Errno.handle unless crypted
    if tainted? || salt.tainted?
      crypted.taint
    else
      # FFI taints returned strings
      crypted.untaint
    end
    crypted
  end

  def unpack1(format)
    unpack(format).first
  end

  def unicode_normalize(form = :nfc)
    require 'unicode_normalize/normalize.rb' unless defined? UnicodeNormalize
    UnicodeNormalize.normalize(self, form)
  end

  def unicode_normalize!(form = :nfc)
    replace(unicode_normalize(form))
  end

  def unicode_normalized?(form = :nfc)
    require 'unicode_normalize/normalize.rb' unless defined? UnicodeNormalize
    UnicodeNormalize.normalized?(self, form)
  end
end
