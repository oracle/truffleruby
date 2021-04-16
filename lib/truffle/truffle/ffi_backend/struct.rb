#
# Copyright (C) 2008-2010 Wayne Meissner
#
# This file is part of ruby-ffi.
#
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
# * Neither the name of the Ruby FFI project nor the names of its contributors
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
#

module FFI
  class Struct
    class << self
      alias_method :alloc_in, :new
      alias_method :alloc_out, :new
      alias_method :alloc_inout, :new
      alias_method :new_in, :new
      alias_method :new_out, :new
      alias_method :new_inout, :new
    end

    attr_reader :pointer, :layout

    def initialize(pointer = nil, *args)
      if args.empty?
        @layout = self.class.instance_variable_get(:@layout)
      else
        @layout = self.class.layout(*args)
      end
      unless FFI::StructLayout === @layout
        raise RuntimeError, "invalid Struct layout for #{self.class}"
      end

      if pointer
        unless FFI::AbstractMemory === pointer
          raise ArgumentError, "Invalid Memory object: #{pointer.inspect}"
        end
        @pointer = pointer
      else
        @pointer = MemoryPointer.new(@layout.size, 1, true)
      end
    end

    def initialize_copy(other)
      return self if equal?(other)

      @layout = other.layout

      # A new MemoryPointer instance is allocated here instead of just calling
      # #dup on rbPointer, since the Pointer may not know its length, or may
      # be longer than just this struct.
      if other.pointer
        @pointer = MemoryPointer.new(@layout.size, 1, false)
        @pointer.__copy_from__(other.pointer, @layout.size)
      else
        @pointer = other.pointer
      end
    end

    private def pointer=(pointer)
      unless FFI::AbstractMemory === pointer
        raise TypeError, "wrong argument type #{pointer.class} (expected Pointer or Buffer)"
      end

      layout = get_layout
      if layout.size > pointer.size
        raise ArgumentError, "memory of #{pointer.size} bytes too small for struct" +
                             " #{self.class} (expected at least #{@layout.size})"
      end

      @pointer = pointer
    end

    private def layout=(layout)
      unless FFI::StructLayout === layout
        raise TypeError, "wrong argument type #{layout.class} (expected #{FFI::StructLayout})"
      end
      @layout = layout
    end

    def order(*args)
      if args.empty?
        @pointer.order
      else
        copy = dup
        pointer = @pointer.order(*args)
        self.pointer = pointer
        copy
      end
    end

    private def get_layout
      if defined?(@layout)
        @layout
      else
        layout = self.class.instance_variable_get(:@layout)
        unless FFI::StructLayout === layout
          raise RuntimeError, "invalid Struct layout for #{self.class}"
        end
        unless defined?(@pointer) && @pointer
          @pointer = MemoryPointer.new(layout.size, 1, true)
        end
        @layout = layout
      end
    end

    def [](field_name)
      field = lookup_field(field_name)
      field.get(@pointer)
    end

    def []=(field_name, value)
      field = lookup_field(field_name)
      field.put(@pointer, value)

      # Keep the Ruby object alive.
      # FFI optimizes this by only doing it for #reference_required?
      (@references ||= {})[field_name] = value
    end

    def null?
      @pointer.null?
    end

    private def lookup_field(field_name)
      get_layout.field_map[field_name]
    end

    class InlineArray
      include Enumerable

      def initialize(pointer, field)
        @pointer = pointer
        @offset = field.offset

        raise unless FFI::Type::ArrayType === field.type
        @array_type = field.type
        @length = @array_type.length
      end

      def size
        @length
      end

      def [](index)
        if (@length > 0) && ((index < 0) || (index >= @length))
          raise IndexError, "index #{index} out of bounds"
        end

        @array_type.get_element_at(@pointer, @offset, index)
      end

      def []=(index, value)
        if (@length > 0) && ((index < 0) || (index >= @length))
          raise IndexError, "index #{index} out of bounds"
        end

        @array_type.put_element_at(@pointer, @offset, index, value)
      end

      def each
        return to_enum(:each) unless block_given?
        @length.times { |i| yield self[i] }
      end

      def to_a
        Array.new(@length) { |i| self[i] }
      end

      def to_ptr
        @pointer.slice(@offset, @array_type.size)
      end
    end

    class CharArray < InlineArray
      def to_s
        @pointer.get_string(@offset, @length)
      end
      alias_method :to_str, :to_s
    end
  end
end
