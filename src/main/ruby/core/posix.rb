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
    TYPES.fetch(type, type)
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

  def self.resolve_type(type)
    Rubinius::Config["rbx.platform.typedef.#{type}"].to_sym
  end

  mode_t = resolve_type('mode_t')

  attach_function :access, [:string, :int], :int
  attach_function :chmod, [:string, mode_t], :int
end
