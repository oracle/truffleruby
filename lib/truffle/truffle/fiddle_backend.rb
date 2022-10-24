# truffleruby_primitives: true

# Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::FiddleBackend

  SIZEOF_CHAR   = Primitive.pointer_find_type_size(:char)
  SIZEOF_SHORT  = Primitive.pointer_find_type_size(:short)
  SIZEOF_INT    = Primitive.pointer_find_type_size(:int)
  SIZEOF_LONG   = Primitive.pointer_find_type_size(:long)

  CHAR_NFI_TYPE  = "SINT#{SIZEOF_CHAR * 8}"
  UCHAR_NFI_TYPE  = "UINT#{SIZEOF_CHAR * 8}"
  SHORT_NFI_TYPE  = "SINT#{SIZEOF_SHORT * 8}"
  USHORT_NFI_TYPE  = "UINT#{SIZEOF_SHORT * 8}"
  INT_NFI_TYPE  = "SINT#{SIZEOF_INT * 8}"
  UINT_NFI_TYPE  = "UINT#{SIZEOF_INT * 8}"
  LONG_NFI_TYPE = "SINT#{SIZEOF_LONG * 8}"
  ULONG_NFI_TYPE = "UINT#{SIZEOF_LONG * 8}"

  SIZE_T_TYPEDEF = Truffle::Config.lookup('platform.typedef.size_t')

  case SIZE_T_TYPEDEF
  when 'ulong'
    SIGNEDNESS_OF_SIZE_T = 1
  else
    raise NotImplementedError, "#{SIZE_T_TYPEDEF} not recognised"
  end

  def self.type_to_nfi(type)
    case type
    when Fiddle::TYPE_VOID
      'VOID'
    when Fiddle::TYPE_VOIDP
      'POINTER'
    when Fiddle::TYPE_CHAR
      CHAR_NFI_TYPE
    when -Fiddle::TYPE_CHAR
      UCHAR_NFI_TYPE
    when Fiddle::TYPE_SHORT
      SHORT_NFI_TYPE
    when -Fiddle::TYPE_SHORT
      USHORT_NFI_TYPE
    when Fiddle::TYPE_INT
      INT_NFI_TYPE
    when -Fiddle::TYPE_INT
      UINT_NFI_TYPE
    when Fiddle::TYPE_LONG
      LONG_NFI_TYPE
    when -Fiddle::TYPE_LONG
      ULONG_NFI_TYPE
    when Fiddle::TYPE_FLOAT
      'FLOAT'
    when Fiddle::TYPE_DOUBLE
      'DOUBLE'
    else
      raise NotImplementedError, "#{type} not implemented"
    end
  end

  def self.type_from_config(type)
    case type
    when 'long'
      Fiddle::TYPE_LONG
    else
      raise NotImplementedError, "integer #{type} not known"
    end
  end

  def self.convert_ruby_to_native(type, val)
    case type
    when Fiddle::TYPE_CHAR
      Integer(val)
    when Fiddle::TYPE_VOIDP
      get_pointer_value(val)
    when Fiddle::TYPE_INT
      Integer(val)
    when -Fiddle::TYPE_LONG
      Integer(val)
    when Fiddle::TYPE_FLOAT, Fiddle::TYPE_DOUBLE
      Float(val)
    else
      raise NotImplementedError, "#{val.inspect} to type #{type}"
    end
  end

  def self.get_pointer_value(val)
    if val.is_a?(String)
      Truffle::CExt.string_to_ffi_pointer(val)
    elsif val.is_a?(Fiddle::Pointer)
      val.to_i
    elsif val.respond_to?(:to_ptr)
      val.to_ptr.to_i
    elsif val.nil?
      0
    elsif val.is_a?(Integer)
      val
    else
      raise NotImplementedError, "#{val.inspect} to pointer"
    end
  end

  def self.convert_native_to_ruby(type, val)
    case type
    when Fiddle::TYPE_VOID
      nil
    when Fiddle::TYPE_VOIDP
      Fiddle::Pointer.new(Truffle::Interop.as_pointer(val))
    when Fiddle::TYPE_INT, Fiddle::TYPE_FLOAT, Fiddle::TYPE_DOUBLE
      val
    else
      raise NotImplementedError, "#{val.inspect} from type #{type}"
    end
  end

  RTLD_NEXT    = Truffle::Config['platform.dlopen.RTLD_NEXT']
  RTLD_DEFAULT = Truffle::Config['platform.dlopen.RTLD_DEFAULT']

end

module Fiddle

  class DLError < StandardError
  end

  TYPE_VOID         = 0
  TYPE_VOIDP        = 1
  TYPE_CHAR         = 2
  TYPE_SHORT        = 3
  TYPE_INT          = 4
  TYPE_LONG         = 5
  TYPE_LONG_LONG    = 6
  TYPE_FLOAT        = 7
  TYPE_DOUBLE       = 8

  SIZEOF_VOIDP      = Primitive.pointer_find_type_size(:pointer)
  SIZEOF_CHAR       = Primitive.pointer_find_type_size(:char)
  SIZEOF_SHORT      = Primitive.pointer_find_type_size(:short)
  SIZEOF_INT        = Truffle::FiddleBackend::SIZEOF_INT
  SIZEOF_LONG       = Truffle::FiddleBackend::SIZEOF_LONG
  SIZEOF_LONG_LONG  = Primitive.pointer_find_type_size(:long_long)
  SIZEOF_FLOAT      = Primitive.pointer_find_type_size(:float)
  SIZEOF_DOUBLE     = Primitive.pointer_find_type_size(:double)

  TYPE_INT8_T  = TYPE_CHAR
  TYPE_INT16_T = TYPE_SHORT
  TYPE_INT32_T = TYPE_INT
  TYPE_INT64_T = TYPE_LONG

  SIZEOF_INT8_T  = 1
  SIZEOF_INT16_T = 2
  SIZEOF_INT32_T = 4
  SIZEOF_INT64_T = 8

  TYPE_SSIZE_T      = Truffle::FiddleBackend.type_from_config(Truffle::Config.lookup('platform.typedef.ssize_t'))
  TYPE_SIZE_T       = -1 * Truffle::FiddleBackend::SIGNEDNESS_OF_SIZE_T * TYPE_SSIZE_T
  TYPE_PTRDIFF_T    = Truffle::FiddleBackend.type_from_config(Truffle::Config.lookup('platform.typedef.ptrdiff_t'))
  TYPE_INTPTR_T     = Truffle::FiddleBackend.type_from_config(Truffle::Config.lookup('platform.typedef.intptr_t'))
  TYPE_UINTPTR_T    = -TYPE_INTPTR_T

  SIZEOF_SSIZE_T    = Primitive.pointer_find_type_size(:ssize_t)
  SIZEOF_SIZE_T     = Primitive.pointer_find_type_size(:size_t)
  SIZEOF_PTRDIFF_T  = Primitive.pointer_find_type_size(:ptrdiff_t)
  SIZEOF_INTPTR_T   = Primitive.pointer_find_type_size(:intptr_t)
  SIZEOF_UINTPTR_T  = Primitive.pointer_find_type_size(:uintptr_t)

  # Alignment assumed to be the same as size

  ALIGN_VOIDP       = SIZEOF_VOIDP
  ALIGN_CHAR        = SIZEOF_CHAR
  ALIGN_SHORT       = SIZEOF_SHORT
  ALIGN_INT         = SIZEOF_INT
  ALIGN_LONG        = SIZEOF_LONG
  ALIGN_LONG_LONG   = SIZEOF_LONG_LONG
  ALIGN_FLOAT       = SIZEOF_FLOAT
  ALIGN_DOUBLE      = SIZEOF_DOUBLE
  ALIGN_SIZE_T      = SIZEOF_SIZE_T
  ALIGN_SSIZE_T     = SIZEOF_SSIZE_T
  ALIGN_PTRDIFF_T   = SIZEOF_PTRDIFF_T
  ALIGN_INTPTR_T    = SIZEOF_INTPTR_T
  ALIGN_UINTPTR_T   = SIZEOF_UINTPTR_T

  WINDOWS             = Truffle::Platform.windows?
  BUILD_RUBY_PLATFORM = RUBY_PLATFORM

  def self.dlwrap(*args)
    raise NotImplementedError
  end

  def self.dlunwrap(*args)
    raise NotImplementedError
  end

  def self.malloc(size)
    Primitive.pointer_raw_malloc size
  end

  def self.realloc(address, size)
    Primitive.pointer_raw_realloc address, size
  end

  def self.free(address)
    Primitive.pointer_raw_free address
  end

  class Function

    DEFAULT = :default_abi

    def initialize(ptr, args, ret_type, abi = DEFAULT, name: nil)
      @arg_types = args
      @ret_type = ret_type
      args = args.map { |arg| Truffle::FiddleBackend.type_to_nfi(arg) }
      ret_type = Truffle::FiddleBackend.type_to_nfi(ret_type)
      signature = "(#{args.join(',')}):#{ret_type}"

      if ptr.is_a?(Closure)
        @function = ptr.method(:call)
      else
        ptr = Truffle::FFI::Pointer.new(ptr)
        @function = Primitive.interop_eval_nfi(signature).bind(ptr)
      end
    end

    def call(*args)
      args = args.zip(@arg_types).map { |arg, type| Truffle::FiddleBackend.convert_ruby_to_native(type, arg) }
      ret = @function.call(*args)
      Truffle::FiddleBackend.convert_native_to_ruby(@ret_type, ret)
    end

  end

  class Closure

    def initialize(ret, args, abi = Function::DEFAULT)
      # does nothing - 'not implemented' when used
    end

    def to_i(*args)
      raise NotImplementedError
    end

  end

  class Handle

    def self.sym(*args)
      raise NotImplementedError
    end

    def self.[](*args)
      raise NotImplementedError
    end

    RTLD_LAZY    = Truffle::Config['platform.dlopen.RTLD_LAZY']
    RTLD_NOW     = Truffle::Config['platform.dlopen.RTLD_NOW']
    RTLD_GLOBAL  = Truffle::Config['platform.dlopen.RTLD_GLOBAL']

    def initialize(library = nil, flags = RTLD_LAZY | RTLD_GLOBAL)
      raise DLError, 'unsupported dlopen flags' if flags != (RTLD_LAZY | RTLD_GLOBAL)

      if library == Truffle::FiddleBackend::RTLD_NEXT
        @handle = :rtld_next
      else
        library = nil if library == Truffle::FiddleBackend::RTLD_DEFAULT
        begin
          @handle = Primitive.interop_eval_nfi(library ? "load '#{library}'" : 'default')
        rescue Polyglot::ForeignException => e
          raise DLError, "#{e.message}"
        end
      end
    end

    def to_i(*args)
      raise NotImplementedError
    end

    def close(*args)
      raise NotImplementedError
    end

    def sym(name)
      if :rtld_next == @handle
        raise DLError, 'RTLD_NEXT is not supported'
      else
        sym = @handle[name]
        raise DLError, "unknown symbol \"#{name}\"" unless sym
        Truffle::Interop.as_pointer(sym)
      end
    end

    alias_method :[], :sym

    def disable_close(*args)
      raise NotImplementedError
    end

    def enable_close(*args)
      raise NotImplementedError
    end

    def close_enabled?(*args)
      raise NotImplementedError
    end

    DEFAULT = Handle.new(Truffle::FiddleBackend::RTLD_DEFAULT)
    NEXT    = Handle.new(Truffle::FiddleBackend::RTLD_NEXT)

  end

  class Pointer

    def self.malloc(*args)
      raise NotImplementedError
    end

    def self.to_ptr(val)
      if val.is_a?(IO)
        raise NotImplementedError
      elsif val.is_a?(String)
        ptr = Pointer.new(Truffle::CExt.string_pointer_to_native(val), val.bytesize)
      elsif val.respond_to?(:to_ptr)
        raise NotImplementedError
      else
        ptr = Pointer.new(Integer(val))
      end
      ptr
    end

    class << self
      alias_method :[], :to_ptr
    end

    def initialize(address, size = 0, freefunc = nil)
      @size = size
      raise unless freefunc == nil
      @pointer = Truffle::FFI::Pointer.new(address)
    end

    def free=(*args)
      raise NotImplementedError
    end

    def free(*args)
      raise NotImplementedError
    end

    def to_i(*args)
      @pointer.address
    end

    def to_int(*args)
      raise NotImplementedError
    end

    def to_value(*args)
      raise NotImplementedError
    end

    def ptr(*args)
      raise NotImplementedError
    end

    def +@(*args)
      raise NotImplementedError
    end

    def ref(*args)
      raise NotImplementedError
    end

    def -@(*args)
      raise NotImplementedError
    end

    def null?(*args)
      raise NotImplementedError
    end

    def to_s(*args)
      raise NotImplementedError
    end

    def to_str(*args)
      raise NotImplementedError
    end

    def inspect(*args)
      raise NotImplementedError
    end

    def <=>(*args)
      raise NotImplementedError
    end

    def ==(*args)
      raise NotImplementedError
    end

    def eql?(*args)
      raise NotImplementedError
    end

    def +(*args)
      raise NotImplementedError
    end

    def -(*args)
      raise NotImplementedError
    end

    def [](start, length = nil)
      if length
        (@pointer + start).read_string(length)
      else
        (@pointer + start).read_int8
      end
    end

    def []=(*args)
      raise NotImplementedError
    end

    def size(*args)
      raise NotImplementedError
    end

    def size=(*args)
      raise NotImplementedError
    end

    NULL = Pointer.new(0)

  end

  RUBY_FREE = Handle.new.sym('free')

end
