# frozen_string_literal: true

# Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
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

module Truffle::FFI
  class AbstractMemory
  end

  class Pointer < AbstractMemory
    # Indicates how many bytes the chunk of memory that is pointed to takes up.
    attr_accessor :total
    alias_method :size, :total

    # Indicates how many bytes the type that the pointer is cast as uses.
    attr_accessor :type_size

    # NOTE: redefined in lib/truffle/ffi.rb for full FFI
    def self.find_type_size(type)
      TrufflePrimitive.pointer_find_type_size(type)
    end

    def self.size
      SIZE
    end

    def initialize(type, address = undefined)
      if undefined.equal? address
        address = type
        type = nil
      end

      if Truffle::Interop.pointer?(address)
        address = Truffle::Interop.as_pointer(address)
      end
      self.address = address

      @type_size = case type
                   when nil
                     1
                   when Integer
                     type
                   when Symbol
                     Pointer.find_type_size(type)
                   else
                     type.size
                   end
    end

    def initialize_copy(from)
      total = from.total
      raise RuntimeError, 'cannot duplicate unbounded memory area' unless total
      TrufflePrimitive.pointer_malloc self, total
      TrufflePrimitive.pointer_copy_memory address, from.address, total
      self
    end

    def clear
      raise RuntimeError, 'cannot clear unbounded memory area' unless @total
      TrufflePrimitive.pointer_clear self, @total
    end

    def inspect
      # Don't have this print the data at the location. It can crash everything.
      addr = address()

      if addr < 0
        sign = '-'
        addr = -addr
      else
        sign = ''
      end

      "#<#{self.class.name} address=#{sign}0x#{addr.to_s(16)}>"
    end

    def polyglot_pointer?
      true
    end

    # Every IS_POINTER object should also have TO_NATIVE
    def to_native
      self
    end

    # FFI's #to_ptr conversion
    def to_ptr
      self
    end

    def null?
      address == 0x0
    end

    def +(offset)
      ptr = Pointer.new(address + offset)
      if total
        ptr.total = total - offset
      end
      ptr
    end

    def slice(offset, length)
      ptr = Pointer.new(address + offset)
      ptr.total = length
      ptr
    end

    def [](idx)
      raise ArgumentError, 'unknown type size' unless @type_size
      self + (idx * @type_size)
    end

    def ==(other)
      return true if nil.equal?(other) && null?
      return false unless other.kind_of? Pointer
      address == other.address
    end

    def network_order(start, size)
      raise 'FFI::Pointer#network_order not yet implemented'
    end

    private def check_bounds(offset, length)
      if offset < 0 || (@total && length > @total)
        raise IndexError, "Memory access offset=#{offset} size=#{length} is out of bounds"
      end
    end

    def get_string(offset, length = nil)
      TrufflePrimitive.pointer_read_string_to_null address + offset, length
    end

    def put_string(offset, str)
      put_bytes(offset, str)
      put_char(offset + str.bytesize, 0)
    end

    def read_string(len = nil)
      if len
        return ''.b if len == 0
        get_bytes(0, len)
      else
        get_string(0)
      end
    end

    def read_string_length(len)
      get_bytes(0, len)
    end

    def read_string_to_null
      get_string(0)
    end

    def write_string_length(str, len)
      put_bytes(0, str, 0, len)
    end

    def write_string(str, len = nil)
      len = str.bytesize unless len
      # Write the string data without NUL termination
      put_bytes(0, str, 0, len)
    end

    def get_array_of_string(offset, count = nil)
      if count
        check_bounds(offset, count * SIZE)
        Array.new(count) do |i|
          ptr = get_pointer(offset + i * SIZE)
          if ptr.null?
            nil
          else
            ptr.read_string
          end
        end
      else
        check_bounds(offset, SIZE)
        strings = []
        i = 0
        loop do
          ptr = get_pointer(offset + i * SIZE)
          if ptr.null?
            return strings
          else
            strings << ptr.read_string
          end
          i += 1
        end
      end
    end

    def get_bytes(offset, length)
      TrufflePrimitive.pointer_read_bytes address + offset, length
    end

    def put_bytes(offset, str, index = 0, length = nil)
      raise RangeError, 'index cannot be less than zero' if index < 0
      if length
        if index + length > str.bytesize
          raise RangeError, 'index+length is greater than size of string'
        end
      else
        if index > str.bytesize
          raise IndexError, 'index is greater than size of string'
        end
        length = str.bytesize - index
      end
      TrufflePrimitive.pointer_write_bytes address + offset, str, index, length
      self
    end

    def read_bytes(length)
      get_bytes(0, length)
    end

    def write_bytes(str, index = 0, length = nil)
      put_bytes(0, str, index, length)
    end

    def __copy_from__(pointer, size)
      TrufflePrimitive.pointer_copy_memory address, pointer.address, size
    end

    def read_array_of_type(type, reader, length)
      ary = []
      size = FFI.type_size(type)
      tmp = self
      length.times do |j|
        ary << tmp.send(reader)
        tmp += size unless j == length-1 # avoid OOB
      end
      ary
    end

    def write_array_of_type(type, writer, ary)
      size = FFI.type_size(type)
      ary.each_with_index do |val, i|
        break unless i < self.size
        self.send(writer, i * size, val)
      end
      self
    end

    def read(type)
      get(type, 0)
    end

    def write(type, value)
      put(type, 0, value)
    end

    def get(type, offset)
      begin
        type = ::FFI.find_type(type)
      rescue TypeError => e
        raise ArgumentError, e.message
      end
      type.get_at(self, offset)
    end

    def put(type, offset, value)
      begin
        type = ::FFI.find_type(type)
      rescue TypeError => e
        raise ArgumentError, e.message
      end
      type.put_at(self, offset, value)
    end

    SIZE = 8
    NULL = Pointer.new(0x0)
  end

  class MemoryPointer < Pointer
    def initialize(type, count = 1, clear = true)
      super(type, 0)
      @total = @type_size * (count || 1)

      TrufflePrimitive.pointer_malloc self, @total
      TrufflePrimitive.pointer_clear self, @total if clear
    end

    def self.new(type, count = 1, clear = true)
      ptr = super(type, count, clear)

      if block_given?
        begin
          yield ptr
        ensure
          ptr.free
        end
      else
        ptr.autorelease = true
        ptr
      end
    end

    def self.from_string(str)
      str = StringValue(str)
      ptr = new(1, str.bytesize + 1, false)
      ptr.put_string(0, str)
      ptr
    end
  end
end
