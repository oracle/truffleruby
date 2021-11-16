# frozen_string_literal: true

# Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
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

class Regexp
  IGNORECASE         = 1
  EXTENDED           = 2
  MULTILINE          = 4
  FIXEDENCODING      = 16
  NOENCODING         = 32
  DONT_CAPTURE_GROUP = 128
  CAPTURE_GROUP      = 256

  KCODE_NONE = (1 << 9)
  KCODE_EUC  = (2 << 9)
  KCODE_SJIS = (3 << 9)
  KCODE_UTF8 = (4 << 9)
  KCODE_MASK = KCODE_NONE | KCODE_EUC | KCODE_SJIS | KCODE_UTF8

  OPTION_MASK = IGNORECASE | EXTENDED | MULTILINE | FIXEDENCODING | NOENCODING | DONT_CAPTURE_GROUP | CAPTURE_GROUP

  def self.try_convert(obj)
    Truffle::Type.try_convert obj, Regexp, :to_regexp
  end

  def self.convert(pattern)
    return pattern if Primitive.object_kind_of?(pattern, Regexp)
    if Primitive.object_kind_of?(pattern, Array)
      union(*pattern)
    else
      Regexp.quote(pattern.to_s)
    end
  end

  def self.compatible?(*patterns)
    encodings = patterns.map { |r| convert(r).encoding }
    last_enc = encodings.pop
    encodings.each do |encoding|
      raise ArgumentError, "incompatible encodings: #{encoding} and #{last_enc}" unless Primitive.encoding_compatible?(last_enc, encoding)
      last_enc = encoding
    end
  end

  def self.last_match(index=nil)
    match = Primitive.regexp_last_match_get(Primitive.caller_special_variables)
    if index
      if match
        # cannot delegate all parameter conversion to MatchData#[] due to differing parameter types allowed
        case index
        when Symbol, String
          # no conversion needed
        else
          index = Primitive.rb_to_int index
        end
        match[index]
      else
        nil
      end
    else
      match
    end
  end

  def self.union(*patterns)
    case patterns.size
    when 0
      return %r/(?!)/
    when 1
      pattern = patterns.first
      case pattern
      when Array
        return union(*pattern)
      else
        converted = Truffle::Type.rb_check_convert_type(pattern, Regexp, :to_regexp)
        if Primitive.nil? converted
          return Regexp.new(Regexp.quote(pattern))
        else
          return converted
        end
      end
    else
      compatible?(*patterns)
      enc = convert(patterns.first).encoding
    end

    sep = '|'.encode(enc)
    str = ''.encode(enc)

    patterns = patterns.map do |pat|
      if Primitive.object_kind_of?(pat, Regexp)
        pat
      else
        StringValue(pat)
      end
    end

    Truffle::RegexpOperations.union(str, sep, *patterns)
  end
  Truffle::Graal.always_split(method(:union))

  def self.new(pattern, opts=nil, lang=nil)
    if Primitive.object_kind_of?(pattern, Regexp)
      opts = pattern.options
      pattern = pattern.source
    elsif Primitive.object_kind_of?(pattern, Integer) or Primitive.object_kind_of?(pattern, Float)
      raise TypeError, "can't convert #{pattern.class} into String"
    elsif Primitive.object_kind_of?(opts, Integer)
      opts = opts & (OPTION_MASK | KCODE_MASK) if opts > 0
    elsif opts
      opts = IGNORECASE
    else
      opts = 0
    end

    code = lang[0] if lang
    opts |= NOENCODING if code == ?n or code == ?N

    Primitive.regexp_compile pattern, opts # may be overridden by subclasses
  end

  class << self
    alias_method :compile, :new
  end

  def =~(str)
    result = str ? Truffle::RegexpOperations.match(self, str, 0) : nil
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, result)

    result.begin(0) if result
  end

  def match(str, pos=0)
    unless str
      Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil)
      return nil
    end
    result = Truffle::RegexpOperations.match(self, str, pos)
    Primitive.regexp_last_match_set(Primitive.caller_special_variables, result)

    if result && block_given?
      yield result
    else
      result
    end
  end
  Truffle::Graal.always_split(instance_method(:match))

  def match?(str, pos = 0)
    Truffle::RegexpOperations.match?(self, str, pos)
  end
  Truffle::Graal.always_split(instance_method(:match?))

  def ===(other)
    if Primitive.object_kind_of?(other, Symbol)
      other = other.to_s
    elsif !Primitive.object_kind_of?(other, String)
      other = Truffle::Type.rb_check_convert_type other, String, :to_str
      unless other
        Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil)
        return false
      end
    end

    if match = Truffle::RegexpOperations.match_from(self, other, 0)
      Primitive.regexp_last_match_set(Primitive.caller_special_variables, match)
      true
    else
      Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil)
      false
    end
  end
  Truffle::Graal.always_split(instance_method(:===))

  def eql?(other)
    return false unless Primitive.object_kind_of?(other, Regexp)
    return false unless source == other.source
    (options & ~NOENCODING) == (other.options & ~NOENCODING)
  end

  alias_method :==, :eql?

  def inspect
    # the regexp matches any / that is after anything except for a \
    escape = source.gsub(%r!(\\.)|/!) { $1 || '\/' }
    str = "/#{escape}/#{Truffle::RegexpOperations.option_to_string(options)}"
    str << 'n' if (options & NOENCODING) > 0
    str
  end

  def encoding
    Primitive.encoding_get_object_encoding self
  end

  def ~
    line = Primitive.io_last_line_get(Primitive.caller_special_variables)

    unless Primitive.object_kind_of?(line, String)
      Primitive.regexp_last_match_set(Primitive.caller_special_variables, nil)
      return nil
    end

    res = match(line)
    res ? res.begin(0) : nil
  end

  def casefold?
    (options & IGNORECASE) > 0 ? true : false
  end

  #
  # call-seq:
  #    rxp.named_captures  => hash
  #
  # Returns a hash representing information about named captures of <i>rxp</i>.
  #
  # A key of the hash is a name of the named captures.
  # A value of the hash is an array which is list of indexes of corresponding
  # named captures.
  #
  #    /(?<foo>.)(?<bar>.)/.named_captures
  #    #=> {"foo"=>[1], "bar"=>[2]}
  #
  #    /(?<foo>.)(?<foo>.)/.named_captures
  #    #=> {"foo"=>[1, 2]}
  #
  # If there are no named captures, an empty hash is returned.
  #
  #    /(.)(.)/.named_captures
  #    #=> {}
  #
  def named_captures
    Hash[Primitive.regexp_names(self)].transform_keys!(&:to_s)
  end

  #
  # call-seq:
  #    rxp.names   => [name1, name2, ...]
  #
  # Returns a list of names of captures as an array of strings.
  #
  #     /(?<foo>.)(?<bar>.)(?<baz>.)/.names
  #     #=> ["foo", "bar", "baz"]
  #
  #     /(?<foo>.)(?<foo>.)/.names
  #     #=> ["foo"]
  #
  #     /(.)(.)/.names
  #     #=> []
  #
  def names
    Primitive.regexp_names(self).map { |x| x.first.to_s }
  end

end

class MatchData
  class << self
    # Prevent allocating MatchData, like MRI 2.7+, so we don't need to check if it's initialized
    undef_method :allocate
  end

  def offset(idx)
    [self.begin(idx), self.end(idx)]
  end

  def ==(other)
    return true if equal?(other)

    Primitive.object_kind_of?(other, MatchData) &&
      string == other.string  &&
      regexp == other.regexp  &&
      captures == other.captures
  end
  alias_method :eql?, :==

  def string
    Primitive.match_data_get_source(self).dup.freeze
  end

  def captures
    to_a[1..-1]
  end

  def names
    regexp.names
  end

  def named_captures
    names.collect { |name| [name, self[name]] }.to_h
  end

  def begin(index)
    backref = if String === index || Symbol === index
                names_to_backref = Hash[Primitive.regexp_names(self.regexp)]
                names_to_backref[index.to_sym].last
              else
                Truffle::Type.coerce_to(index, Integer, :to_int)
              end


    Primitive.match_data_begin(self, backref)
  end

  def end(index)
    backref = if String === index || Symbol === index
                names_to_backref = Hash[Primitive.regexp_names(self.regexp)]
                names_to_backref[index.to_sym].last
              else
                Truffle::Type.coerce_to(index, Integer, :to_int)
              end


    Primitive.match_data_end(self, backref)
  end

  def inspect
    str = "#<MatchData \"#{self[0]}\""
    idx = 0
    captures.zip(names) do |capture, name|
      idx += 1
      str << " #{name || idx}:#{capture.inspect}"
    end
    "#{str}>"
  end

  def values_at(*indexes)
    indexes.map { |i| self[i] }.flatten(1)
  end

  def to_s
    self[0]
  end
end

Truffle::KernelOperations.define_hooked_variable(
  :$~,
  -> s { Primitive.regexp_last_match_get(s) },
  Truffle::RegexpOperations::LAST_MATCH_SET)

Truffle::KernelOperations.define_hooked_variable(
  :'$`',
  -> s { match = Primitive.regexp_last_match_get(s)
         match.pre_match if match },
  -> { raise SyntaxError, "Can't set variable $`" },
  -> s { 'global-variable' if Primitive.regexp_last_match_get(s) })

Truffle::KernelOperations.define_hooked_variable(
  :"$'",
  -> s { match = Primitive.regexp_last_match_get(s)
         match.post_match if match },
  -> { raise SyntaxError, "Can't set variable $'" },
  -> s { 'global-variable' if Primitive.regexp_last_match_get(s) })

Truffle::KernelOperations.define_hooked_variable(
  :'$&',
  -> s { match = Primitive.regexp_last_match_get(s)
         match[0] if match },
  -> { raise SyntaxError, "Can't set variable $&" },
  -> s { 'global-variable' if Primitive.regexp_last_match_get(s) })

Truffle::KernelOperations.define_hooked_variable(
  :'$+',
  -> s { match = Primitive.regexp_last_match_get(s)
         match.captures.reverse.find { |m| !Primitive.nil?(m) } if match },
  -> { raise SyntaxError, "Can't set variable $+" },
  -> s { 'global-variable' if Primitive.regexp_last_match_get(s) })
