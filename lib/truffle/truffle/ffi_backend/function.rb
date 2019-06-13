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
  class Function < Pointer
    # @param [Type, Symbol] return_type return type for the function
    # @param [Array<Type, Symbol>] param_types array of parameters types
    # @param [Proc || FFI::Pointer || FFI::DynamicLibrary::Symbol] the function Proc or native address
    # @param [Hash] options see {FFI::FunctionType} for available options
    def initialize(return_type, param_types, function = nil, options = {}, &block)
      function ||= block

      if FunctionType === return_type and nil == param_types
        @function_info = return_type
      else
        @function_info = FunctionType.new(return_type, param_types, options)
      end

      if FFI::DynamicLibrary::Symbol === function
        @function = function.handle.bind(@function_info.nfi_type)
        super(@function)
      elsif FFI::Pointer === function
        @function = create_nfi_pointer(function, @function_info.nfi_type)
        super(@function)
      elsif Proc === function || Method === function
        @function = function
        @native_wrapper = create_native_wrapper(@function, @function_info)
        super(@native_wrapper)
      else
        raise ArgumentError, "Unknown how to convert #{function} to a function"
      end

      @autorelease = true
    end

    def call(*args, &block)
      param_types = @function_info.param_types
      return_type = @function_info.return_type
      enums = @function_info.enums

      converted_args = args.dup
      if block
        function_index = param_types.index { |type| FFI::FunctionType === type }
        converted_args.insert(function_index, block)
      end

      unless converted_args.size == param_types.size
        raise ArgumentError, "wrong number of arguments (given #{converted_args.size}, expected #{param_types.size})"
      end

      converted_args.each_with_index do |arg, i|
        converted_args[i] = convert_ruby_to_native(param_types[i], arg, enums)
      end

      result = @function.call(*converted_args)

      convert_native_to_ruby(return_type, result)
    end

    private def callback(function, function_info)
      param_types = function_info.param_types
      return_type = function_info.return_type
      enums = function_info.enums

      -> *args do
        converted_args = args.dup
        converted_args.each_with_index do |arg, i|
          converted_args[i] = convert_native_to_ruby(param_types[i], arg)
        end

        result = function.call(*converted_args)

        convert_ruby_to_native(return_type, result, enums)
      end
    end

    def attach(mod, name)
      this = self
      body = -> *args, &block do
        this.call(*args, &block)
      end
      mod.define_method(name, body)
      mod.define_singleton_method(name, body)
      self
    end

    def autorelease?
      @autorelease
    end

    # Actually, we always have autorelease for FFI::Function
    def autorelease=(value)
      @autorelease = value
    end

    def free
      unless @native_wrapper
        raise RuntimeError, 'cannot free function which was not allocated'
      end

      # TODO (eregon): Actually free() the @native_wrapper eagerly
      @native_wrapper = nil
    end

    private def convert_ruby_to_native(type, value, enums)
      if FFI::Type::Mapped === type
        type.to_native(value, nil)
      elsif enums and Symbol === value
        enums.__map_symbol(value)
      elsif FFI::Type::UINT64 == type or FFI::Type::ULONG == type
        Truffle::Type.rb_num2ulong(value)
      elsif FFI::Type::FLOAT32 == type
        Truffle::Type.double_to_float(Truffle::Type.rb_num2dbl(value))
      elsif FFI::Type::POINTER == type
        get_pointer_value(value)
      elsif FFI::Type::STRING == type
        value ? Truffle::Type.check_null_safe(value) : nil
      elsif FFI::FunctionType === type and Proc === value
        callback(value, type)
      else
        value
      end
    end

    private def convert_native_to_ruby(type, value)
      if Type::Mapped === type
        # #to_native expects a Ruby object, not e.g., a NFI NativePointer
        ruby_value = convert_native_to_ruby(type.native_type, value)
        type.from_native(ruby_value, nil)
      elsif FFI::Type::POINTER == type
        FFI::Pointer.new(Truffle::Interop.as_pointer(value))
      elsif FFI::Type::BOOL == type
        value != 0
      elsif FFI::Type::STRING == type
        if value.nil?
          nil
        elsif String === value
          value
        else
          FFI::Pointer.new(Truffle::Interop.as_pointer(value)).read_string_to_null
        end
      elsif FFI::Type::Builtin === type and type.unsigned?
        # TODO: NFI workaround
        type.signed2unsigned(value)
      elsif FFI::FunctionType === type
        ptr = FFI::Pointer.new(Truffle::Interop.as_pointer(value))
        FFI::Function.new(type, nil, ptr)
      else
        value
      end
    end

    # Returns a Truffle::FFI::Pointer, to correctly keep the argument alive during the native call
    private def get_pointer_value(value)
      if Truffle::FFI::Pointer === value
        value
      elsif nil.equal?(value)
        Truffle::FFI::Pointer::NULL
      elsif String === value
        Truffle::CExt.string_to_pointer(value)
      elsif value.respond_to?(:to_ptr)
        Truffle::Type.coerce_to value, Truffle::FFI::Pointer, :to_ptr
      else
        raise ArgumentError, "#{value.inspect} (#{value.class}) is not a pointer"
      end
    end

    private def create_nfi_pointer(pointer, nfi_signature)
      lib = Truffle::POSIX::LIBTRUFFLEPOSIX.resolve
      lib['identity_pointer'].bind("(pointer):#{nfi_signature}").call(pointer)
    end

    private def create_native_wrapper(function, function_info)
      lib = Truffle::POSIX::LIBTRUFFLEPOSIX.resolve
      create = lib['create_native_wrapper'].bind("(env,#{function_info.nfi_type}):object")
      create.call(callback(function, function_info))
    end
  end

  class VariadicInvoker
    def initialize(function, args_types, return_type, options)
      @function = function
      @return_type = return_type
      @options = options
      @fixed = args_types.map { |type| FFI.find_type(type) }.reject { |type| type == Type::VARARGS }
      @type_map = options[:type_map]

      @options[:varargs] = @fixed.size
    end

    def invoke(param_types, param_values, &block)
      Function.new(@return_type, param_types, @function, @options).call(*param_values, &block)
    end
  end
end
