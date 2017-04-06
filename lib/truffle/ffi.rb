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
      if libname
        Truffle::Interop.eval('application/x-native', "load #{libname}")
      else
        Truffle::Interop.eval('application/x-native', 'default')
      end
    end
  end
end

require_relative 'ffi/library'

module FFI
  module Library
    TO_NATIVE_TYPE = {
      int: 'SINT32',
    }

    private def to_native_type(type)
      TO_NATIVE_TYPE.fetch(type, type)
    end

    def attach_function(method_name, native_name, args_types, return_type, options = {})
      warn "options #{options} ignored for attach_function :#{method_name}" unless options.empty?

      args_types = args_types.map { |type| to_native_type(type) }
      return_type = to_native_type(return_type)

      function = @ffi_libs.each { |library|
        break library[native_name]
      }
      signature = "(#{args_types.join(',')}):#{return_type}"
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
