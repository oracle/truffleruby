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
  }

  def self.to_nfi_type(type)
    if found = TYPES[type]
      found
    elsif typedef = Rubinius::Config["rbx.platform.typedef.#{type}"]
      TYPES[type] = to_nfi_type(typedef.to_sym)
    else
      TYPES[type] = type
    end
  end

  def self.attach_function(name, argument_types, return_type)
    func = LIBC[name]
    return_type = to_nfi_type(return_type)
    argument_types = argument_types.map { |type| to_nfi_type(type) }
    bound_func = func.bind("(#{argument_types.join(',')}):#{return_type}")

    define_singleton_method(name) { |*args|
      bound_func.call(*args)
    }
  end

  attach_function :access, [:string, :int], :int
  attach_function :chmod, [:string, :mode_t], :int
  attach_function :chown, [:string, :uid_t, :gid_t], :int
  attach_function :dup, [:int], :int
  attach_function :dup2, [:int, :int], :int
  attach_function :fchmod, [:int, :mode_t], :int
  attach_function :fchown, [:int, :uid_t, :gid_t], :int
  attach_function :fsync, [:int], :int

  attach_function :getegid, [], :gid_t
  attach_function :geteuid, [], :uid_t
  attach_function :getgid, [], :gid_t
  attach_function :getuid, [], :uid_t
  attach_function :seteuid, [:uid_t], :int
  attach_function :setgid, [:gid_t], :int
  attach_function :setuid, [:uid_t], :int

  if Rubinius.linux?
    attach_function :__errno_location, [], :pointer
    def self.errno_address; __errno_location; end
  elsif Rubinius.darwin?
    attach_function :__error, [], :pointer
    def self.errno_address; __error; end
  elsif Rubinius.solaris?
    attach_function :___errno, [], :pointer
    def self.errno_address; ___errno; end
  else
    raise 'Unsupported platform'
  end
end
