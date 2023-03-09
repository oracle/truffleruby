# frozen_string_literal: true

# Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
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
    out = []
    size = self.size

    indexes.each do |elem|
      if Primitive.object_kind_of?(elem, String) || Primitive.object_kind_of?(elem, Symbol)
        out << self[elem]
      elsif Primitive.object_kind_of?(elem, Range)
        start, length = Primitive.range_normalized_start_length(elem, size)
        finish = start + length - 1

        raise RangeError, "#{elem} out of range" if start < 0
        next if finish < start # ignore empty ranges

        finish_in_bounds = [finish, size - 1].min
        start.upto(finish_in_bounds) do |index|
          out << self[index]
        end

        (finish_in_bounds + 1).upto(finish) { out << nil }
      else
        index = Primitive.rb_num2int(elem)
        if index >= size || index < -size
          out << nil
        else
          out << self[index]
        end
      end
    end

    out
  end

  def match(n)
    # Similar, but #match accepts only single index/name, but not a range or an optional length.
    number = Truffle::Type.rb_check_convert_type(n, Integer, :to_int)
    return self[number] if number
    # To convert the last type (String) we used rb_convert_type instead of rb_check_convert_type which throws an exception
    name = Truffle::Type.rb_check_convert_type(n, Symbol, :to_sym) || Truffle::Type.rb_convert_type(n, String, :to_str)
    self[name]
  end

  def match_length(n)
    match(n)&.length
  end

  def to_s
    self[0]
  end
end
