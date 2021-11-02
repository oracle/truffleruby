# truffleruby_primitives: true

# Copyright (c) 2013, Brian Shirai
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
#    list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 3. Neither the name of the library nor the names of its contributors may be
#    used to endorse or promote products derived from this software without
#    specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
# OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
# EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Modifications made by the Truffle team are:
#
# Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.


class ScanError < StandardError
end

class StringScanner

  Id = 'None$Id'.freeze
  Version = '1.0.0'.freeze

  attr_reader :pos

  def initialize(string, dup=false)
    if string.instance_of? String
      @original = string
      @string = string
    else
      @original = StringValue(string)
      @string = String.new @original
    end

    reset_state
  end

  def pos=(n)
    n = Integer(n)

    n += @string.bytesize if n < 0

    if n < 0 or n > @string.bytesize
      raise RangeError, "index out of range (#{n})"
    end

    @pos = n
  end

  alias_method :pointer, :pos
  alias_method :pointer=, :pos=

  def [](n)
    if @match
      raise TypeError, "no implicit conversion of #{n.class} into Integer" if Range === n
      str = @match[n]
      str
    end
  end

  def bol?
    @pos == 0 or @string.getbyte(pos-1) == 10
  end

  alias_method :beginning_of_line?, :bol?

  def charpos
    @string.byteslice(0, @pos).length
  end

  def check(pattern)
    scan_internal pattern, false, true, true
  end

  def check_until(pattern)
    scan_internal pattern, false, true, false
  end

  def clear
    warn 'StringScanner#clear is obsolete; use #terminate instead' if $VERBOSE
    terminate
  end

  def concat(str)
    @string << StringValue(str)
    self
  end
  alias_method :<<, :concat

  def empty?
    warn 'StringScanner#empty? is obsolete; use #eos? instead?' if $VERBOSE
    eos?
  end

  def eos?
    raise ArgumentError, 'uninitialized StringScanner object' unless @string
    @pos >= @string.bytesize
  end

  def exist?(pattern)
    scan_internal pattern, false, false, false
  end

  def get_byte
    if eos?
      @match = nil
      return nil
    end

    # We need to match one byte, regardless of the string encoding
    @match = Primitive.matchdata_create_single_group(/./mn, @string, pos, pos+1)

    @prev_pos = @pos
    @pos += 1

    @string.byteslice(@prev_pos, 1)
  end

  def getbyte
    warn 'StringScanner#getbyte is obsolete; use #get_byte instead' if $VERBOSE
    get_byte
  end

  def getch
    scan(/./m)
  end

  def inspect
    if defined? @string
      if eos?
        str = "#<#{self.class} fin>"
      else
        if string.bytesize - pos > 5
          rest = "#{string[pos..pos+4]}..."
        else
          rest = string[pos..string.bytesize]
        end

        if pos > 0
          if pos > 5
            prev = "...#{string[pos-5...pos]}"
          else
            prev = string[0...pos]
          end

          str = "#<#{self.class} #{pos}/#{string.bytesize} #{prev.inspect} @ #{rest.inspect}>"
        else
          str = "#<#{self.class} #{pos}/#{string.bytesize} @ #{rest.inspect}>"
        end
      end

      str
    else
      "#<#{self.class} (uninitialized)>"
    end
  end

  def match?(pattern)
    scan_internal pattern, false, false, true
  end

  def matched
    if @match
      matched = @match.to_s
      matched
    end
  end

  def matched?
    Primitive.as_boolean(@match)
  end

  def matched_size
    Primitive.match_data_byte_end(@match, 0) - Primitive.match_data_byte_begin(@match, 0) if @match
  end

  def post_match
    @match.post_match if @match
  end

  def pre_match
    @string.byteslice(0, Primitive.match_data_byte_begin(@match, 0)) if @match
  end

  def reset_state
    @prev_pos = @pos = 0
    @match = nil
  end
  private :reset_state

  def reset
    reset_state
    self
  end

  def rest
    @string.byteslice(@pos, @string.bytesize - @pos)
  end

  def rest?
    !eos?
  end

  def rest_size
    @string.bytesize - @pos
  end

  def restsize
    warn 'StringScanner#restsize is obsolete; use #rest_size instead' if $VERBOSE
    rest_size
  end

  def scan(pattern)
    scan_internal pattern, true, true, true
  end

  def scan_until(pattern)
    scan_internal pattern, true, true, false
  end

  def scan_full(pattern, advance_pos, getstr)
    scan_internal pattern, advance_pos, getstr, true
  end

  def search_full(pattern, advance_pos, getstr)
    scan_internal pattern, advance_pos, getstr, false
  end

  def self.must_C_version
    self
  end

  def skip(pattern)
    scan_internal pattern, true, false, true
  end

  def skip_until(pattern)
    scan_internal pattern, true, false, false
  end

  def string
    @original
  end

  def string=(string)
    reset_state

    if string.instance_of? String
      @original = string
      @string = string
    else
      @original = StringValue(string)
      @string = String.new @original
    end
  end

  def terminate
    @match = nil
    @pos = string.bytesize
    self
  end

  def unscan
    raise ScanError if @match.nil?
    @pos = @prev_pos
    @prev_pos = nil
    @match = nil
    self
  end

  def peek(len)
    raise ArgumentError if len < 0
    return '' if len.zero?
    @string.byteslice(pos, len)
  end

  def peep(len)
    warn 'StringScanner#peep is obsolete; use #peek instead' if $VERBOSE
    peek len
  end

  private def scan_check_args(pattern, headonly)
    case pattern
    when String
      raise TypeError, "bad pattern argument: #{pattern.inspect}" unless headonly
    when Regexp
    else
      raise TypeError, "bad pattern argument: #{pattern.inspect}"
    end
    raise ArgumentError, 'uninitialized StringScanner object' unless @string
  end

  # This method is kept very small so that it should fit within 100
  # AST nodes and can be split. This is done to avoid indirect calls
  # to TRegex.
  private def scan_internal(pattern, advance_pos, getstr, headonly)
    scan_check_args(pattern, headonly)

    if Primitive.object_kind_of?(pattern, String)
      md = scan_internal_string_pattern(pattern, headonly)
    else
      md = Truffle::RegexpOperations.match_in_region pattern, @string, pos, @string.bytesize, headonly, pos
    end
    if md
      Primitive.matchdata_fixup_positions(md, pos)
      @match = md
      scan_internal_set_pos_and_str(advance_pos, getstr, md)
    else
      @match = nil
    end
  end

  private def scan_internal_string_pattern(pattern, headonly)
    if @string.byteslice(pos..).start_with?(pattern)
      Primitive.matchdata_create_single_group(pattern, @string.dup, 0, pattern.bytesize)
    else
      nil
    end
  end

  private def scan_internal_set_pos_and_str(advance_pos, getstr, md)
    fin = Primitive.match_data_byte_end(md, 0)

    @prev_pos = @pos
    @pos = fin if advance_pos

    width = fin - @prev_pos
    return width unless getstr

    @string.byteslice(@prev_pos, width)
  end

end
