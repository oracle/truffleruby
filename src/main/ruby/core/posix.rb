# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle::POSIX
  LIBC = Truffle::Interop.eval('application/x-native', 'default')

  def self.resolve_type(type)
    case type
    when :int
      :sint32
    else
      type
    end
  end

  def self.attach_function(name, argument_types, return_type)
    func = LIBC[name]
    return_type = resolve_type(return_type)
    argument_types = argument_types.map { |type| resolve_type(type) }
    bound_func = func.bind("(#{argument_types.join(',')}):#{return_type}")

    define_singleton_method(name) { |*args|
      bound_func.call(*args)
    }
  end


  attach_function :access, [:string, :int], :int
end
