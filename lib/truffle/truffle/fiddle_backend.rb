# Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::Fiddle

  SIZEOF_INT    = Truffle::FFI::Pointer.find_type_size(:int)
  SIZEOF_LONG   = Truffle::FFI::Pointer.find_type_size(:long)

  INT_NFI_TYPE  = "SINT#{SIZEOF_INT * 8}"
  UINT_NFI_TYPE  = "UINT#{SIZEOF_INT * 8}"
  LONG_NFI_TYPE = "SINT#{SIZEOF_LONG * 8}"
  ULONG_NFI_TYPE = "UINT#{SIZEOF_LONG * 8}"

  def self.type_to_nfi(type)
    case type
    when Fiddle::TYPE_VOID
      'VOID'
    when Fiddle::TYPE_VOIDP
      'POINTER'
    when Fiddle::TYPE_CHAR
      'CHAR'
    when Fiddle::TYPE_SHORT
      'SHORT'
    when Fiddle::TYPE_INT
      INT_NFI_TYPE
    when Fiddle::TYPE_LONG
      LONG_NFI_TYPE
    when -Fiddle::TYPE_LONG
      ULONG_NFI_TYPE
    when Fiddle::TYPE_FLOAT
      'FLOAT'
    when Fiddle::TYPE_DOUBLE
      'DOUBLE'
    else
      raise "#{type} not implemented"
    end
  end

  def self.int_type(size)
    case size
    when SIZEOF_INT
      Fiddle::TYPE_INT
    when SIZEOF_LONG
      Fiddle::TYPE_LONG
    else
      raise
    end
  end

  def self.convert_ruby_to_native(type, val)
    case type
    when Fiddle::TYPE_VOIDP
      if val.is_a?(String)
        Truffle::CExt.string_pointer_to_native(val)
      elsif val.is_a?(Fiddle::Pointer)
        val.to_i
      elsif val.respond_to?(:to_ptr)
        val.to_ptr.to_i
      elsif val.nil?
        0
      elsif val.is_a?(Integer)
        val
      else
        raise "#{val.inspect} to pointer"
      end
    when Fiddle::TYPE_INT
      Integer(val)
    when -Fiddle::TYPE_LONG
      Integer(val)
    when Fiddle::TYPE_FLOAT, Fiddle::TYPE_DOUBLE
      Float(val)
    else 
      raise "#{val.inspect} to type #{type}"
    end
  end

  def self.convert_native_to_ruby(type, val)
    case type
    when Fiddle::TYPE_VOID
      nil
    when Fiddle::TYPE_VOIDP
      Fiddle::Pointer.new(Truffle::Interop.to_native(val))
    when Fiddle::TYPE_INT, Fiddle::TYPE_FLOAT, Fiddle::TYPE_DOUBLE
      val
    else 
      raise "#{val.inspect} from type #{type}"
    end
  end

  SIGNEDNESS_OF_SIZE_T = 1 # platform specific!

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

  SIZEOF_VOIDP      = Truffle::FFI::Pointer.find_type_size(:pointer)
  SIZEOF_CHAR       = Truffle::FFI::Pointer.find_type_size(:char)
  SIZEOF_SHORT      = Truffle::FFI::Pointer.find_type_size(:short)
  SIZEOF_INT        = Truffle::Fiddle::SIZEOF_INT
  SIZEOF_LONG       = Truffle::Fiddle::SIZEOF_LONG
  SIZEOF_LONG_LONG  = Truffle::FFI::Pointer.find_type_size(:long_long)
  SIZEOF_FLOAT      = Truffle::FFI::Pointer.find_type_size(:float)
  SIZEOF_DOUBLE     = Truffle::FFI::Pointer.find_type_size(:double)

  SIZEOF_SIZE_T     = Truffle::FFI::Pointer.find_type_size(:size_t)
  SIZEOF_SSIZE_T    = Truffle::FFI::Pointer.find_type_size(:ssize_t)
  SIZEOF_PTRDIFF_T  = Truffle::FFI::Pointer.find_type_size(:ptrdiff_t)
  SIZEOF_INTPTR_T   = Truffle::FFI::Pointer.find_type_size(:intptr_t)
  SIZEOF_UINTPTR_T  = Truffle::FFI::Pointer.find_type_size(:uintptr_t)

  TYPE_SSIZE_T      = Truffle::Fiddle.int_type(SIZEOF_SIZE_T)
  TYPE_SIZE_T       = -1 * Truffle::Fiddle::SIGNEDNESS_OF_SIZE_T * TYPE_SSIZE_T
  TYPE_PTRDIFF_T    = Truffle::Fiddle.int_type(SIZEOF_PTRDIFF_T)
  TYPE_INTPTR_T     = Truffle::Fiddle.int_type(SIZEOF_INTPTR_T)
  TYPE_UINTPTR_T    = -TYPE_INTPTR_T

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
    raise 'not implemented'
  end

  def self.dlunwrap(*args)
    raise 'not implemented'
  end

  def self.malloc(size)
    Truffle.invoke_primitive :pointer_raw_malloc, size
  end

  def self.realloc(address, size)
    Truffle.invoke_primitive :pointer_raw_realloc, address, size
  end

  def self.free(address)
    Truffle.invoke_primitive :pointer_raw_free, address
  end

  class Function

    DEFAULT = :default_abi

    def initialize(ptr, args, ret_type, abi = DEFAULT, name: nil)
      @arg_types = args
      @ret_type = ret_type
      args = args.map { |arg| Truffle::Fiddle.type_to_nfi(arg) }
      ret_type = Truffle::Fiddle.type_to_nfi(ret_type)
      signature = "(#{args.join(',')}):#{ret_type}"

      if ptr.is_a?(Closure)
        @function = ptr.method(:call)
      else
        ptr = Truffle::POSIX.nfi_symbol_from_pointer(ptr, signature)
        @function = ptr.bind(signature)
      end
    end

    def call(*args)
      args = (args.zip(@arg_types)).map { |arg, type| Truffle::Fiddle.convert_ruby_to_native(type, arg) }
      ret = @function.call(*args)
      Truffle::Fiddle.convert_native_to_ruby(@ret_type, ret)
    end

  end

  class Closure

    def initialize(ret, args, abi = Function::DEFAULT)
      # does nothing - 'not implemented' when used
    end

    def to_i(*args)
      raise 'not implemented'
    end

  end

  class Handle

    def self.sym(*args)
      raise 'not implemented'
    end

    def self.[](*args)
      raise 'not implemented'
    end
    
    RTLD_LAZY    = Truffle::Config['platform.dlopen.RTLD_LAZY']
    RTLD_NOW     = Truffle::Config['platform.dlopen.RTLD_NOW']
    RTLD_GLOBAL  = Truffle::Config['platform.dlopen.RTLD_GLOBAL']
    RTLD_NEXT    = Truffle::Config['platform.dlopen.RTLD_NEXT']
    RTLD_DEFAULT = Truffle::Config['platform.dlopen.RTLD_DEFAULT']

    def initialize(library = nil, flags = RTLD_LAZY | RTLD_GLOBAL)
      raise DLError, 'unsupported dlopen flags' if flags != RTLD_LAZY | RTLD_GLOBAL
      @handle = Polyglot.eval('nfi', library ? "load #{library}" : 'default')
    rescue RuntimeError
      raise DLError, "#{library}: cannot open shared object file: No such file or directory"
    end

    def to_i(*args)
      raise 'not implemented'
    end

    def close(*args)
      raise 'not implemented'
    end

    def sym(name)
      sym = @handle[name]
      raise DLError, "unknown symbol \"#{name}\"" unless sym
      Truffle::Interop.as_pointer(sym)
    end

    alias_method :[], :sym

    def disable_close(*args)
      raise 'not implemented'
    end

    def enable_close(*args)
      raise 'not implemented'
    end

    def close_enabled?(*args)
      raise 'not implemented'
    end

  end

  class Pointer

    def self.malloc(*args)
      raise 'not implemented'
    end

    def self.to_ptr(val)
      if val.is_a?(IO)
        raise 'not implemented'
      elsif val.is_a?(String)
        ptr = Pointer.new(Truffle::CExt.string_pointer_to_native(val), val.bytesize)
      elsif val.respond_to?(:to_ptr)
        raise 'not implemented'
      else
        ptr = Pointer.new(Integer(val))
      end
      Truffle::Type.infect ptr, val
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
      raise 'not implemented'
    end

    def free(*args)
      raise 'not implemented'
    end

    def to_i(*args)
      @pointer.address
    end

    def to_int(*args)
      raise 'not implemented'
    end

    def to_value(*args)
      raise 'not implemented'
    end

    def ptr(*args)
      raise 'not implemented'
    end

    def +@(*args)
      raise 'not implemented'
    end

    def ref(*args)
      raise 'not implemented'
    end

    def -@(*args)
      raise 'not implemented'
    end

    def null?(*args)
      raise 'not implemented'
    end

    def to_s(*args)
      raise 'not implemented'
    end

    def to_str(*args)
      raise 'not implemented'
    end

    def inspect(*args)
      raise 'not implemented'
    end

    def <=>(*args)
      raise 'not implemented'
    end

    def ==(*args)
      raise 'not implemented'
    end

    def eql?(*args)
      raise 'not implemented'
    end

    def +(*args)
      raise 'not implemented'
    end

    def -(*args)
      raise 'not implemented'
    end

    def [](start, length = nil)
      if length
        (@pointer + start).read_string(length)
      else
        (@pointer + start).read_int8
      end
    end

    def []=(*args)
      raise 'not implemented'
    end

    def size(*args)
      raise 'not implemented'
    end

    def size=(*args)
      raise 'not implemented'
    end

    NULL = Pointer.new(0)

  end

  RUBY_FREE = Handle.new.sym('free')

end
