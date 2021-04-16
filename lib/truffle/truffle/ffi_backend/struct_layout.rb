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
  class StructLayout < Type
    attr_reader :field_map

    def initialize(fields, size, alignment)
      size = ffi_align(size, alignment)
      alignment = alignment
      super(size, alignment, 'StructLayout')
      @field_map = {}

      @fields = fields.map.with_index do |field, i|
        unless FFI::StructLayout::Field === field
          raise TypeError, "wrong type for field #{i}."
        end
        type = field.type
        unless type
          raise RuntimeError, "type of field #{i} not supported"
        end
        if (type.size == 0) && (i < fields.size-1)
          raise TypeError, "type of field #{i} has zero size"
        end
        @field_map[field.name] = field
      end

      raise RuntimeError, 'Struct size is zero' if size == 0
    end

    private def ffi_align(size, alignment)
      ((size-1) | (alignment-1)) + 1
    end

    def [](field)
      @field_map[field]
    end

    def members
      @fields.map(&:name)
    end

    def __union!
      t = [
        Type::INT8, Type::INT16, Type::INT32, Type::INT64,
        Type::FLOAT, Type::DOUBLE, Type::LONGDOUBLE
      ].find { |type| type.alignment == @alignment }
      unless t
        raise RuntimeError, "cannot create libffi union representation for alignment #{@alignment}"
      end

      count = @size / t.size
      @ffi_types = ::Array.new(count, t)
    end

    class Field
      attr_reader :name, :offset, :type

      def initialize(name, offset, type)
        @name = name.to_sym
        @offset = offset.to_int
        @type = type
      end

      def size
        @type.size
      end

      def alignment
        @type.alignment
      end

      def get(struct_pointer)
        @type.get_at(struct_pointer, @offset)
      end

      def put(struct_pointer, value)
        @type.put_at(struct_pointer, @offset, value)
      end
    end

    class Number < Field
    end

    class String < Field
      def initialize(name, offset, type)
        super(name, offset, type)
      end

      def get(struct_pointer)
        string_pointer = struct_pointer.get_pointer(@offset)
        if string_pointer.address == 0
          nil
        else
          string_pointer.read_string
        end
      end

      def put(struct_pointer, value)
        raise 'unimplemented'
      end
    end

    class Pointer < Field
    end

    class Function < Field
      def get(struct_pointer)
        pointer = struct_pointer.get_pointer(@offset)
        FFI::Function.new(@type, nil, pointer)
      end

      def put(struct_pointer, value)
        if (nil == value) || (FFI::Function === value)
          function = value
        elsif ::Proc === value || value.respond_to?(:call)
          function = FFI::Function.new(@type, nil, value)
        else
          raise TypeError, 'wrong type (expected Proc or Function)'
        end

        struct_pointer.put_pointer(@offset, function)
      end
    end

    class Array < Field
      def get(struct_pointer)
        if @type.char_array?
          FFI::Struct::CharArray.new(struct_pointer, self)
        else
          FFI::Struct::InlineArray.new(struct_pointer, self)
        end
      end

      def put(struct_pointer, value)
        if @type.char_array?
          if value.bytesize < @type.length
            struct_pointer.put_string(@offset, value)
          elsif value.bytesize == @type.length
            struct_pointer.put_bytes(@offset, value)
          else
            raise IndexError, "String is longer (#{value.bytesize} bytes) than the char array (#{@type.length} bytes)"
          end
        else
          raise NotImplementedError, 'cannot set array field'
        end
      end
    end
  end
end
