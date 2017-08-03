# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Minimal support needed to run ffi/library
module FFI
  Platform = Rubinius::FFI::Platform

  class DynamicLibrary
    RTLD_LAZY   = Rubinius::Config['rbx.platform.dlopen.RTLD_LAZY']
    RTLD_NOW    = Rubinius::Config['rbx.platform.dlopen.RTLD_NOW']
    RTLD_GLOBAL = Rubinius::Config['rbx.platform.dlopen.RTLD_GLOBAL']
    RTLD_LOCAL  = Rubinius::Config['rbx.platform.dlopen.RTLD_LOCAL']

    def self.open(libname, flags)
      code = libname ? "load #{libname}" : 'default'
      handle = Truffle::Interop.eval('application/x-native', code)
      DynamicLibrary.new(libname, handle)
    end

    def initialize(name, handle)
      @name = name
      @handle = handle
    end

    def find_symbol(name)
      @handle[name]
    end

    def inspect
      "\#<#{self.class} @name=#{@name.inspect}>"
    end
  end

  def self.find_type(*args)
    Rubinius::FFI.find_type(*args)
  end
end

require_relative 'ffi/library'

module FFI
  module Library
    LIBC = Rubinius::LIBC

    # Indicies are based on RubiniusTypes
    TO_NFI_TYPE = [
      'SINT8',  # char
      'UINT8',  # uchar
      'UINT8',  # bool
      'SINT16', # short
      'UINT16', # ushort
      'SINT32', # int
      'UINT32', # uint
      'SINT64', # long
      'UINT64', # ulong
      'SINT64', # ll
      'UINT64', # ull
      'FLOAT',  # float
      'DOUBLE', # double
      'POINTER',# ptr
      'VOID',   # void
      'STRING', # string
      # strptr
      # chararr
      # enum
      # varargs
    ]

    private def to_nfi_type(type)
      idx = FFI.find_type(type)
      TO_NFI_TYPE.fetch(idx)
    end

    def attach_function(method_name, native_name, args_types, return_type = nil, options = {})
      unless return_type
        native_name, args_types, return_type = method_name, native_name, args_types
      end

      warn "options #{options} ignored for attach_function :#{method_name}" unless options.empty?

      nfi_args_types = args_types.map { |type| to_nfi_type(type) }
      nfi_return_type = to_nfi_type(return_type)

      function = @ffi_libs.each { |library|
        break library.find_symbol(native_name)
      }
      signature = "(#{nfi_args_types.join(',')}):#{nfi_return_type}"
      function = function.bind(Truffle::Interop.to_java_string(signature))

      define_singleton_method(method_name) { |*args|
        args = args.map { |arg| Struct === arg ? arg.to_ptr : arg }
        result = function.call(*args)
        if return_type == :pointer
          FFI::Pointer.new(Truffle::Interop.unbox(result))
        else
          result
        end
      }
    end

    def find_type(t)
      FFI.find_type(t)
    end
  end

  Pointer = Rubinius::FFI::Pointer
  MemoryPointer = Rubinius::FFI::MemoryPointer
  Struct = Rubinius::FFI::Struct

  class Struct
    def self.ptr
      warn "validation for #{self} parameter not yet implemented"
      :pointer
    end
  end
end
