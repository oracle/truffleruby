# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle
  module ThreadOperations
    def self.get_thread_local(key)
      locals = Truffle.invoke_primitive :thread_get_locals, Thread.current
      Truffle.invoke_primitive :object_ivar_get, locals, key
    end
    Truffle::Graal.always_split(method(:get_thread_local))

    def self.set_thread_local(key, value)
      locals = Truffle.invoke_primitive :thread_get_locals, Thread.current
      Truffle.invoke_primitive :object_ivar_set, locals, key, value
    end
    Truffle::Graal.always_split(method(:set_thread_local))

  end
end
