module Truffle
  module ThreadOperations
    def self.get_thread_local(key)
      locals = Truffle.invoke_primitive :thread_get_locals, Thread.current
      Truffle.invoke_primitive :object_ivar_get, locals, key
    end

    def self.set_thread_local(key, value)
      locals = Truffle.invoke_primitive :thread_get_locals, Thread.current
      Truffle.invoke_primitive :object_ivar_set, locals, key, value
    end
  end
end
