module FFI
  module Library
    private def resolve_library(name)
      case name
      when 'SDL2' then '/usr/lib64/libSDL2.so'
      else
        unless File.exist?(name)
          raise "resolution of library #{name} not yet implemented"
        end
      end
    end

    def ffi_lib(name)
      name = resolve_library(name)
      @library = Truffle::Interop.eval('application/x-native', "load #{name}")
    end

    TO_NATIVE_TYPE = {
      int: "SINT32",
    }

    private def to_native_type(type)
      TO_NATIVE_TYPE.fetch(type, type)
    end

    def attach_function(method_name, native_name, args_types, return_type, options = {})
      warn "options #{options} ignored for attach_function :#{method_name}" unless options.empty?

      args_types = args_types.map { |type| to_native_type(type) }
      return_type = to_native_type(return_type)

      signature = "(#{args_types.join(',')}):#{return_type}"
      function = @library[native_name].bind(Truffle::Interop.to_java_string(signature))

      define_singleton_method(method_name) { |*args|
        args = args.map { |arg| Struct === arg ? arg.to_ptr : arg }
        function.call(*args)
      }
    end
  end

  MemoryPointer = Rubinius::FFI::MemoryPointer
  Struct = Rubinius::FFI::Struct

  class Struct
    def self.ptr
      warn "validation for #{self} parameter not yet implemented"
      :pointer
    end
  end
end
