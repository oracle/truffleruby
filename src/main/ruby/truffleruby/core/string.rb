# frozen_string_literal: true

# Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
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

  def byteslice(index_or_range, length=undefined)
    # Handles the (int index) and (int index, int length) forms.
    str = Primitive.string_byte_substring self, index_or_range, length
    return str unless Primitive.undefined?(str)

    # Convert to (int index, int length) form.
    if Range === index_or_range && Primitive.undefined?(length)
      index, length = Primitive.range_normalized_start_length(index_or_range, bytesize)
      return if index < 0 or index > bytesize
      return byteslice 0, 0 if length < 0
    else
      index = Primitive.rb_num2long(index_or_range)
      index += bytesize if index < 0

      if Primitive.undefined?(length)
        return if index == bytesize
        length = 1
      else
        length = Primitive.rb_num2long(length)
        return if length < 0
      end

      length = bytesize unless Primitive.integer_fits_into_int(index)
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
      match_data = Truffle::RegexpOperations.search_region(pattern, self, 0, bytesize, true)
      Primitive.regexp_last_match_set(Primitive.caller_special_variables, match_data)
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
    Primitive.check_frozen self
    prefix = Truffle::Type.coerce_to(prefix, String, :to_str)
    if !prefix.empty? && start_with?(prefix)
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
    Primitive.check_frozen self
    suffix = Truffle::Type.coerce_to(suffix, String, :to_str)
    if !suffix.empty? && end_with?(suffix)
      self[size - suffix.size, suffix.size] = ''
      self
    else
      nil
    end
  end

  def grapheme_clusters(&block)
    if block_given?
      each_grapheme_cluster(&block)
    else
      regex = Regexp.new('\X'.encode(encoding))
      scan(regex)
    end
  end

  def include?(needle)
    Primitive.as_boolean(Primitive.find_string(self, StringValue(needle), 0))
  end

  def lstrip
    str = dup
    str.lstrip! || str
  end

  def oct
    Primitive.string_to_inum(self, -8, false, true)
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
    Primitive.string_to_inum(self, 16, false, true)
  end

  def reverse
    dup.reverse!
  end

  def partition(pattern=nil)
    return super() if Primitive.nil?(pattern) && block_given?

    if Primitive.object_kind_of?(pattern, Regexp)
      if m = Truffle::RegexpOperations.match(pattern, self)
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, m)
        return [m.pre_match, m.to_s, m.post_match]
      end
    else
      pattern = StringValue(pattern)
      if i = index(pattern)
        post_start = i + pattern.length
        post_len = size - post_start

        return [Primitive.string_substring(self, 0, i),
                pattern,
                Primitive.string_substring(self, post_start, post_len)]
      end
    end

    # Nothing worked out, this is the default.
    empty = String.new(encoding: encoding)
    [self, empty, empty.dup]
  end

  def rpartition(pattern)
    if Primitive.object_kind_of?(pattern, Regexp)
      if m = Truffle::RegexpOperations.search_region(pattern, self, 0, bytesize, false)
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, m)
        return [m.pre_match, m[0], m.post_match]
      end
    else
      pattern = StringValue(pattern)
      if i = rindex(pattern)
        post_start = i + pattern.length
        post_len = size - post_start

        return [Primitive.string_substring(self, 0, i),
                pattern.dup,
                Primitive.string_substring(self, post_start, post_len)]
      end
    end

    # Nothing worked out, this is the default.
    empty = String.new(encoding: encoding)
    [empty, empty.dup, self.dup]
  end

  def rstrip
    str = dup
    str.rstrip! || str
  end

  def scan(pattern, &block)
    pattern = Truffle::Type.coerce_to_regexp(pattern, true)
    index = 0

    last_match = nil
    ret = block_given? ? self : []

    while match = Truffle::RegexpOperations.match_from(pattern, self, index)
      fin = Primitive.match_data_byte_end(match, 0)

      if Truffle::RegexpOperations.collapsing?(match)
        if char = Primitive.string_find_character(self, fin)
          index = fin + char.bytesize
        else
          index = fin + 1
        end
      else
        index = fin
      end

      last_match = match
      val = (match.length == 1 ? match[0] : match.captures)

      if block
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, match)
        yield(val)
      else
        ret << val
      end
    end

    Primitive.regexp_last_match_set(Primitive.caller_special_variables, last_match)
    ret
  end

  def split(pattern=nil, limit=undefined, &block)
    Truffle::Splitter.split(self, pattern, limit, &block)
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
    Primitive.string_swapcase! self, mapped_options
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

    Primitive.string_to_inum(self, base, false, true)
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

  def subpattern(pattern, capture)
    match = Truffle::RegexpOperations.match(pattern, self)

    return nil unless match

    if index = Truffle::Type.rb_check_convert_type(capture, Integer, :to_int)
      return nil if index >= match.size || -index >= match.size
      capture = index
    end

    str = match[capture]
    [match, str]
  end
  private :subpattern

  def each_codepoint
    return to_enum(:each_codepoint) { size } unless block_given?

    each_char { |c| yield c.ord }
    self
  end

  def each_grapheme_cluster
    return to_enum(:each_grapheme_cluster) { size } unless block_given?

    regex = Regexp.new('\X'.encode(encoding))
    # scan(regex, &block) would leak the $ vars in the user block which is probably unwanted
    scan(regex) { |e| yield e }
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
    Primitive.check_frozen self

    if Primitive.undefined?(to)
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

    from = encoding if Primitive.undefined?(from)
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

    if Primitive.undefined?(options)
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
      else
        force_encoding to_enc
      end
    end

    # TODO: replace this hack with transcoders
    if Primitive.object_kind_of?(options, Hash)
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
    if suffixes.size == 1 and suffix = suffixes[0] and String === suffix
      enc = Primitive.encoding_ensure_compatible self, suffix
      return Primitive.string_end_with?(self, suffix, enc)
    end

    suffixes.each do |original_suffix|
      suffix = Truffle::Type.rb_convert_type original_suffix, String, :to_str
      enc = Primitive.encoding_ensure_compatible self, suffix
      return true if Primitive.string_end_with?(self, suffix, enc)
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
    unicode = Primitive.encoding_is_unicode enc

    actual_encoding = Primitive.get_actual_encoding(self)
    if actual_encoding != enc
      enc = actual_encoding
      if unicode
        unicode = Primitive.encoding_is_unicode enc
      end
    end

    result = '"'.dup.force_encoding(result_encoding)

    index = 0
    total = bytesize
    while index < total
      char = Primitive.string_chr_at(self, index)

      if char
        index += inspect_char(enc, result_encoding, ascii, unicode, index, char, result)
      else
        result << "\\x#{getbyte(index).to_s(16).upcase}"
        index += 1
      end
    end

    result << '"'

    result.force_encoding(result_encoding)
  end

  # https://github.com/ruby/ruby/blob/12f7ba5ed4a07855d6a9429aa627211db3655ca7/string.c#L6049-L6050
  MAX_PRINTABLE_UNICODE_CHAR = 0x7F
  private_constant :MAX_PRINTABLE_UNICODE_CHAR

  private def inspect_char(enc, result_encoding, ascii, unicode, index, char, result)
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
          result << escaped
          return consumed
        end
      end
    end

    if Primitive.character_printable_p(char) && unicode && char.ord < MAX_PRINTABLE_UNICODE_CHAR
      result << char.encode(result_encoding)
    elsif Primitive.character_printable_p(char) && (enc == result_encoding || (ascii && char.ascii_only?))
      result << char
    else
      code = char.ord
      escaped = code.to_s(16).upcase

      if unicode
        if code < 0x10000
          pad = '0' * (4 - escaped.bytesize)
          result << "\\u#{pad}#{escaped}"
        else
          result << "\\u{#{escaped}}"
        end
      else
        if code < 0x100
          pad = '0' * (2 - escaped.bytesize)
          result << "\\x#{pad}#{escaped}"
        else
          result << "\\x{#{escaped}}"
        end
      end
    end

    consumed
  end

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

    if stop.bytesize == 1 && bytesize == 1 && self.ascii_only? && stop.ascii_only?
      enc = Primitive.encoding_ensure_compatible(self.encoding, stop.encoding)

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
          Primitive.string_to_inum(self, 10, true, true)
          Primitive.string_to_inum(stop, 10, true, true)
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
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, $~)
    s
  end

  def sub!(pattern, replacement=undefined, &block)
    # Because of the behavior of $~, this is duplicated from sub! because
    # if we call sub! from sub, the last_match can't be updated properly.

    unless valid_encoding?
      raise ArgumentError, "invalid byte sequence in #{encoding}"
    end

    if Primitive.undefined? replacement
      unless block_given?
        raise ArgumentError, "method '#{__method__}': given 1, expected 2"
      end
      Primitive.check_frozen self
      use_yield = true
    else
      Primitive.check_frozen self

      unless Primitive.object_kind_of?(replacement, String)
        hash = Truffle::Type.rb_check_convert_type(replacement, Hash, :to_hash)
        replacement = StringValue(replacement) unless hash
      end
      use_yield = false
    end

    pattern = Truffle::Type.coerce_to_regexp(pattern, true) unless Primitive.object_kind_of?(pattern, Regexp)
    match = Truffle::RegexpOperations.match_from(pattern, self, 0)

    Primitive.regexp_last_match_set(Primitive.proc_special_variables(block), match) if block
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, match)

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
        val = val.to_s unless Primitive.object_kind_of?(val, String)

        Primitive.string_append(ret, val)
      else
        Truffle::StringOperations.to_sub_replacement(replacement, ret, match)
      end

      Primitive.string_append(ret, match.post_match)

      replace(ret)
      self
    else
      nil
    end
  end

  def slice!(one, two=undefined)
    Primitive.check_frozen self
    # This is un-DRY, but it's a simple manual argument splitting. Keeps
    # the code fast and clean since the sequence are pretty short.
    #
    if Primitive.undefined?(two)
      result = slice(one)

      if Primitive.object_kind_of?(one, Regexp)
        lm = $~
        self[one] = '' if result
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, lm)
      else
        self[one] = '' if result
      end
    else
      result = slice(one, two)

      if Primitive.object_kind_of?(one, Regexp)
        lm = $~
        self[one, two] = '' if result
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, lm)
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
    Primitive.check_frozen self

    bytes = Primitive.string_previous_byte_index(self, bytesize)
    return unless bytes

    chr = Primitive.string_chr_at(self, bytes)
    if chr.ord == 10
      if i = Primitive.string_previous_byte_index(self, bytes)
        chr = Primitive.string_chr_at(self, i)

        bytes = i if chr.ord == 13
      end
    end

    Truffle::StringOperations.truncate(self, bytes)

    self
  end

  def chomp!(sep=undefined)
    Primitive.check_frozen self

    if Primitive.undefined?(sep)
      sep = $/
    elsif sep
      sep = StringValue(sep)
    end

    return if Primitive.nil? sep

    if sep == DEFAULT_RECORD_SEPARATOR
      return unless bytes = Primitive.string_previous_byte_index(self, bytesize)

      chr = Primitive.string_chr_at(self, bytes)

      case chr.ord
      when 13
        # do nothing
      when 10
        if j = Primitive.string_previous_byte_index(self, bytes)
          chr = Primitive.string_chr_at(self, j)

          if !Primitive.nil?(chr) && chr.ord == 13
            bytes = j
          end
        end
      else
        return
      end
    elsif sep.empty?
      return if empty?
      bytes = bytesize

      while i = Primitive.string_previous_byte_index(self, bytes)
        chr = Primitive.string_chr_at(self, i)
        break unless !Primitive.nil?(chr) && chr.ord == 10

        bytes = i

        if j = Primitive.string_previous_byte_index(self, i)
          chr = Primitive.string_chr_at(self, j)
          if !Primitive.nil?(chr) && chr.ord == 13
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

  def chr
    Primitive.string_substring(self, 0, 1)
  end

  def each_line(sep=$/, chomp: false)
    unless block_given?
      return to_enum(:each_line, sep, chomp: chomp)
    end

    # weird edge case.
    if Primitive.nil? sep
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
        nxt = Primitive.find_string(self, sep, pos)
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
        nxt = Primitive.find_string(unmodified_self, sep, pos)
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
    if Primitive.undefined?(replacement) && !block_given?
      return s.to_enum(:gsub, pattern, replacement)
    end
    if Primitive.undefined?(replacement)
      ret, match_data = Truffle::StringOperations.gsub_block_set_last_match(s, pattern, &block)
    else
      ret, match_data = Truffle::StringOperations.gsub_internal(s, pattern, replacement)
    end
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, match_data)
    s.replace(ret) if ret
    s
  end

  def gsub!(pattern, replacement=undefined, &block)
    if Primitive.undefined?(replacement) && !block_given?
      return to_enum(:gsub!, pattern, replacement)
    end
    Primitive.check_frozen self
    if Primitive.undefined?(replacement)
      ret, match_data = Truffle::StringOperations.gsub_block_set_last_match(self, pattern, &block)
    else
      ret, match_data = Truffle::StringOperations.gsub_internal(self, pattern, replacement)
    end
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, match_data)
    if ret
      replace(ret)
      self
    else
      nil
    end
  end

  def match(pattern, pos=0)
    pattern = Truffle::Type.coerce_to_regexp(pattern) unless Primitive.object_kind_of?(pattern, Regexp)

    result = if block_given?
               pattern.match self, pos do |match|
                 yield match
               end
             else
               pattern.match self, pos
             end
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, $~)
    result
  end

  def match?(pattern, pos=0)
    pattern = Truffle::Type.coerce_to_regexp(pattern) unless Primitive.object_kind_of?(pattern, Regexp)
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

    validate = -> str {
      str = StringValue(str)
      unless str.valid_encoding?
        raise ArgumentError, 'replacement must be valid byte sequence'
      end
      # Special encoding check for #scrub
      if str.ascii_only? ? !encoding.ascii_compatible? : encoding != str.encoding
        raise Encoding::CompatibilityError, 'incompatible character encodings'
      end
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

    Primitive.string_scrub(self, replace_block)
  end

  def scrub!(replace = nil, &block)
    return self if valid_encoding?
    replace(scrub(replace, &block))
    self
  end

  def []=(index, count_or_replacement, replacement=undefined)
    Primitive.check_frozen self

    if Primitive.undefined?(replacement)
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
      index = Primitive.rb_to_int index

      if count
        return self[index, count] = replacement
      else
        return self[index] = replacement
      end
    end

    replacement
  end

  def assign_index(index, count, replacement)
    index += size if index < 0

    if index < 0 or index > size
      raise IndexError, "index #{index} out of string"
    end

    unless bi = Primitive.string_byte_index_from_char_index(self, index)
      raise IndexError, "unable to find character at: #{index}"
    end

    if count
      count = Primitive.rb_to_int count

      if count < 0
        raise IndexError, 'count is negative'
      end

      total = index + count
      if total >= size
        bs = bytesize - bi
      else
        bs = Primitive.string_byte_index_from_char_index(self, total) - bi
      end
    else
      bs = index == size ? 0 : Primitive.string_byte_index_from_char_index(self, index + 1) - bi
    end

    replacement = StringValue replacement
    enc = Primitive.encoding_ensure_compatible self, replacement

    Primitive.string_splice(self, replacement, bi, bs, enc)
  end

  def assign_string(index, replacement)
    unless start = Primitive.find_string(self, index, 0)
      raise IndexError, 'string not matched'
    end

    replacement = StringValue replacement
    enc = Primitive.encoding_ensure_compatible self, replacement

    Primitive.string_splice(self, replacement, start, index.bytesize, enc)
  end

  def assign_range(index, replacement)
    start, length = Primitive.range_normalized_start_length(index, size)
    stop = start + length - 1

    raise RangeError, "#{index.first} is out of range" if start < 0 or start > size

    bi = Primitive.string_byte_index_from_char_index(self, start)
    raise IndexError, "unable to find character at: #{start}" unless bi

    if stop < start
      bs = 0
    elsif stop >= size
      bs = bytesize - bi
    else
      bs = Primitive.string_byte_index_from_char_index(self, stop + 1) - bi
    end

    replacement = StringValue replacement
    enc = Primitive.encoding_ensure_compatible self, replacement

    Primitive.string_splice(self, replacement, bi, bs, enc)
  end

  def assign_regexp(index, count, replacement)
    if count
      count = Primitive.rb_to_int count
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
    enc = Primitive.encoding_ensure_compatible self, replacement

    bi = Primitive.string_byte_index_from_char_index(self, match.begin(count))
    bs = Primitive.string_byte_index_from_char_index(self, match.end(count)) - bi

    Primitive.string_splice(self, replacement, bi, bs, enc)
  end

  private :assign_index, :assign_string, :assign_range, :assign_regexp

  def center(width, padding=' ')
    padding = StringValue(padding)
    raise ArgumentError, 'zero width padding' if padding.empty?

    enc = Primitive.encoding_ensure_compatible self, padding

    width = Primitive.rb_to_int width
    return dup if width <= size

    width -= size
    left = width / 2

    bs = bytesize
    pbs = padding.bytesize

    if pbs > 1
      ps = padding.size
      x = left / ps
      y = left % ps

      lpbi = Primitive.string_byte_index_from_char_index(padding, y)
      lbytes = x * pbs + lpbi

      right = left + (width & 0x1)
      x = right / ps
      y = right % ps

      rpbi = Primitive.string_byte_index_from_char_index(padding, y)
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

    str.force_encoding enc
  end

  def ljust(width, padding=' ')
    padding = StringValue(padding)
    raise ArgumentError, 'zero width padding' if padding.empty?

    enc = Primitive.encoding_ensure_compatible self, padding

    width = Primitive.rb_to_int width
    return dup if width <= size

    width -= size

    bs = bytesize
    pbs = padding.bytesize

    if pbs > 1
      ps = padding.size
      x = width / ps
      y = width % ps

      pbi = Primitive.string_byte_index_from_char_index(padding, y)
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

    str.force_encoding enc
  end

  def rjust(width, padding=' ')
    padding = StringValue(padding)
    raise ArgumentError, 'zero width padding' if padding.empty?

    enc = Primitive.encoding_ensure_compatible self, padding

    width = Primitive.rb_to_int width
    return dup if width <= size

    width -= size

    bs = bytesize
    pbs = padding.bytesize

    if pbs > 1
      ps = padding.size
      x = width / ps
      y = width % ps

      bytes = x * pbs + Primitive.string_byte_index_from_char_index(padding, y)
    else
      bytes = width
    end

    str = self.class.pattern bytes + bs, padding

    Truffle::StringOperations.copy_from(str, self, 0, bs, bytes)

    str.force_encoding enc
  end

  def index(str, start=undefined)
    if Primitive.undefined?(start)
      start = 0
    else
      start = Primitive.rb_to_int start

      start += size if start < 0
      if start < 0 or start > size
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil) if Primitive.object_kind_of?(str, Regexp)
        return
      end
    end

    if Primitive.object_kind_of?(str, Regexp)
      Primitive.encoding_ensure_compatible self, str

      start = Primitive.string_byte_index_from_char_index(self, start)
      if match = Truffle::RegexpOperations.match_from(str, self, start)
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, match)
        return match.begin(0)
      else
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil)
        return
      end
    end

    str = StringValue(str)
    return start if str == ''

    Primitive.encoding_ensure_compatible self, str

    return if str.size > size

    Primitive.string_character_index(self, str, start)
  end

  def initialize(other = undefined, capacity: nil, encoding: nil)
    unless Primitive.undefined?(other)
      Primitive.check_frozen self
      Primitive.string_initialize(self, other, encoding)
    end
    self.force_encoding(encoding) if encoding
    self
  end

  def rindex(sub, finish=undefined)
    if Primitive.undefined?(finish)
      finish = size
    else
      finish = Truffle::Type.coerce_to(finish, Integer, :to_int)
      finish += size if finish < 0
      return nil if finish < 0
      finish = size if finish >= size
    end

    byte_finish = Primitive.string_byte_index_from_char_index(self, finish)

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

      if byte_index = Primitive.find_string_reverse(self, str, byte_finish)
        return Primitive.string_byte_character_index(self, byte_index)
      end

    when Regexp
      Primitive.encoding_ensure_compatible self, sub

      match_data = Truffle::RegexpOperations.search_region(sub, self, 0, byte_finish, false)
      Primitive.regexp_last_match_set(Primitive.caller_special_variables, match_data)
      return match_data.begin(0) if match_data

    else
      needle = StringValue(sub)
      needle_size = needle.size

      # needle is bigger that haystack
      return nil if size < needle_size

      # Boundary case
      return finish if needle.empty?

      Primitive.encoding_ensure_compatible self, needle
      if byte_index = Primitive.find_string_reverse(self, needle, byte_finish)
        return Primitive.string_byte_character_index(self, byte_index)
      end
    end

    nil
  end

  def start_with?(*prefixes)
    if prefixes.size == 1 and prefix = prefixes[0] and String === prefix
      return self[0, prefix.length] == prefix
    end

    # This is the workaround because `Primitive.caller_special_variables` doesn't work inside blocks yet.
    storage = Primitive.caller_special_variables if prefixes.any?(Regexp)

    prefixes.each do |original_prefix|
      case original_prefix
      when Regexp
        Primitive.encoding_ensure_compatible(self, original_prefix)
        match_data = Truffle::RegexpOperations.match_onwards(original_prefix, self, 0, true)
        Primitive.regexp_last_match_set(storage, match_data)
        return true if match_data
      else
        prefix = Truffle::Type.rb_check_convert_type original_prefix, String, :to_str
        unless prefix
          raise TypeError, "no implicit conversion of #{original_prefix.class} into String"
        end
        return true if self[0, prefix.length] == prefix
      end
    end
    false
  end

  def insert(index, other)
    other = StringValue(other)

    index = Primitive.rb_to_int index
    index = length + 1 + index if index < 0

    if index > length or index < 0 then
      raise IndexError, "index #{index} out of string"
    end

    Primitive.check_frozen self

    if index == 0
      replace(other + self)
    elsif index == length
      self << other
    else
      left = self[0...index]
      right = self[index..-1]
      replace(left + other + right)
    end

    self
  end

  def %(args)
    if Primitive.object_kind_of?(args, Hash)
      sprintf(self, args)
    else
      result = Truffle::Type.rb_check_convert_type args, Array, :to_ary
      if Primitive.nil? result
        sprintf(self, args)
      else
        sprintf(self, *result)
      end
    end
  end
  Truffle::Graal.always_split instance_method(:%)

  def capitalize!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, false)
    Primitive.string_capitalize! self, mapped_options
  end

  def capitalize(*options)
    s = dup
    s.capitalize!(*options)
    s
  end

  def downcase!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, true)
    Primitive.string_downcase! self, mapped_options
  end

  def downcase(*options)
    s = dup
    s.downcase!(*options)
    s
  end

  def upcase!(*options)
    mapped_options = Truffle::StringOperations.validate_case_mapping_options(options, false)
    Primitive.string_upcase! self, mapped_options
  end

  def upcase(*options)
    s = dup
    s.upcase!(*options)
    s
  end

  def casecmp(other)
    other = Truffle::Type.rb_check_convert_type(other , String, :to_str)
    if Primitive.nil? other
      nil
    else
      Primitive.string_casecmp(self, other)
    end
  end

  def casecmp?(other)
    other = Truffle::Type.rb_check_convert_type(other , String, :to_str)
    return nil if Primitive.nil? other

    enc = Primitive.encoding_compatible?(encoding, other.encoding)
    if Primitive.nil? enc
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
    if Primitive.string_interned?(self)
      self
    elsif !(str.instance_variables).empty?
      str
    else
      Primitive.string_intern(str)
    end
  end

  def encoding
    Primitive.encoding_get_object_encoding self
  end

  def <=>(other)
    if String === other
      return Primitive.string_cmp self, other
    end

    Truffle::ThreadOperations.detect_pair_recursion self, other do
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
