# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle::POSIX
  LIBC = Truffle::Interop.eval('application/x-native', 'default')

  TYPES = {
    :short => :int16,
    :ushort => :uint16,
    :int => :sint32,
    :uint => :uint32,
    :long => :sint64,
    :ulong => :uint64,
  }

  FS_ENCODING = Encoding.find('filesystem')

  def self.to_nfi_type(type)
    if Array === type
      return "[#{to_nfi_type(type[0])}]"
    end

    if found = TYPES[type]
      found
    elsif typedef = Rubinius::Config["rbx.platform.typedef.#{type}"]
      TYPES[type] = to_nfi_type(typedef.to_sym)
    else
      TYPES[type] = type
    end
  end

  def self.attach_function(method_name, native_name = method_name, argument_types, return_type)
    begin
      func = LIBC[native_name]
    rescue RubyTruffleError => e
      raise e unless e.message.include?('Unknown identifier')
    end

    if func
      return_type = to_nfi_type(return_type)
      argument_types = argument_types.map { |type| to_nfi_type(type) }
      bound_func = func.bind("(#{argument_types.join(',')}):#{return_type}")

      string_args = []
      argument_types.each_with_index { |arg_type, i|
        string_args << i if arg_type == :string
      }
      string_args.freeze

      define_singleton_method(method_name) { |*args|
        string_args.each do |i|
          str = args.fetch(i)
          if str.encoding == Encoding::BINARY
            str = str.dup.force_encoding(FS_ENCODING)
          else
            str = str.encode(FS_ENCODING)
          end
          args[i] = str
        end

        bound_func.call(*args)
      }
    else
      define_singleton_method(method_name) { |*args|
        raise NotImplementedError, "#{native_name} is not available"
      }
      Truffle.invoke_primitive :method_unimplement, method(method_name)
    end
  end

  # Filesystem-related
  attach_function :access, [:string, :int], :int
  attach_function :chdir, [:string], :int
  attach_function :chmod, [:string, :mode_t], :int
  attach_function :chown, [:string, :uid_t, :gid_t], :int
  attach_function :dup, [:int], :int
  attach_function :dup2, [:int, :int], :int
  attach_function :fchmod, [:int, :mode_t], :int
  attach_function :fchown, [:int, :uid_t, :gid_t], :int
  attach_function :fsync, [:int], :int
  attach_function :lchmod, [:string, :mode_t], :int
  attach_function :link, [:string, :string], :int
  attach_function :mkdir, [:string, :mode_t], :int
  attach_function :mkfifo, [:string, :mode_t], :int
  attach_function :readlink, [:string, :pointer, :size_t], :ssize_t
  attach_function :rmdir, [:string], :int
  attach_function :umask, [:mode_t], :mode_t
  attach_function :unlink, [:string], :int
  attach_function :utimes, [:string, :pointer], :int

  # Process-related
  attach_function :getegid, [], :gid_t
  attach_function :geteuid, [], :uid_t
  attach_function :getgid, [], :gid_t
  attach_function :getuid, [], :uid_t
  attach_function :seteuid, [:uid_t], :int
  attach_function :setgid, [:gid_t], :int
  attach_function :setuid, [:uid_t], :int
  attach_function :getgroups, [:int, [:gid_t]], :int

  attach_function :getrlimit, [:int, :pointer], :int
  attach_function :setrlimit, [:int, :pointer], :int

  attach_function :getpriority, [:int, :id_t], :int
  attach_function :setpriority, [:int, :id_t, :int], :int

  # ENV-related
  attach_function :getenv_native, :getenv, [:string], :string
  def self.getenv(name)
    value = getenv_native(name)
    if value.nil?
      nil
    else
      ptr = Rubinius::FFI::Pointer.new(Truffle::Interop.as_pointer(value))
      ptr.read_string_to_null
    end
  end

  attach_function :setenv_native, :setenv, [:string, :string, :int], :int
  def self.setenv(name, value, overwrite)
    Truffle.invoke_primitive :posix_invalidate_env, name
    setenv_native(name, value, overwrite)
  end

  attach_function :unsetenv_native, :unsetenv, [:string], :int
  def self.unsetenv(name)
    Truffle.invoke_primitive :posix_invalidate_env, name
    unsetenv_native(name)
  end

  if Rubinius.linux?
    attach_function :errno_address, :__errno_location, [], :pointer
  elsif Rubinius.darwin?
    attach_function :errno_address, :__error, [], :pointer
  elsif Rubinius.solaris?
    attach_function :errno_address, :___errno, [], :pointer
  else
    raise 'Unsupported platform'
  end
end
