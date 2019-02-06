# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::CExt
  # Methods defined in this file are not considered as Ruby code implementing MRI C parts,
  # see org.truffleruby.cext.CExtNodes.BlockProcNode

  # methods defined with rb_define_method are normal Ruby methods therefore they cannot be defined in the cext.rb file
  # file because blocks passed as arguments would be skipped by org.truffleruby.cext.CExtNodes.BlockProcNode
  def rb_define_method(mod, name, function, argc)
    if argc < -2
      raise "Unsupported rb_define_method argc: #{argc}"
    end

    method_body = Truffle::Graal.copy_captured_locals -> *args, &block do
      if argc == -1 # (int argc, VALUE *argv, VALUE obj)
        args = [args.size, Truffle::CExt.RARRAY_PTR(args), Truffle::CExt.rb_tr_wrap(self)]
      elsif argc == -2 # (VALUE obj, VALUE rubyArrayArgs)
        args = [Truffle::CExt.rb_tr_wrap(self), Truffle::CExt.rb_tr_wrap(args)]
      elsif argc >= 0 # (VALUE obj); (VALUE obj, VALUE arg1); (VALUE obj, VALUE arg1, VALUE arg2); ...
        if args.size != argc
          raise ArgumentError, "wrong number of arguments (given #{args.size}, expected #{argc})"
        end
        args = [Truffle::CExt.rb_tr_wrap(self), *args.map! { |arg| Truffle::CExt.rb_tr_wrap(arg) }]
      end

      # Using raw execute instead of #call here to avoid argument conversion
      Truffle::CExt.push_preserving_frame
      begin
        # We must call explicitly with the block argument if given
        # here so that the `rb_block_*` functions will be able to find
        # it by walking the stack.
        if block
          Truffle::CExt.rb_tr_unwrap(Truffle.invoke_primitive(:interop_call_c_with_mutex, function, args, &block))
        else
          Truffle::CExt.rb_tr_unwrap(Truffle.invoke_primitive(:interop_call_c_with_mutex, function, args))
        end
      ensure
        Truffle::CExt.pop_preserving_frame
      end
    end

    mod.send(:define_method, name, method_body)
  end

  private

  def rb_iterate_call_block(callback, block_arg, callback_arg, &block)
    rb_tr_unwrap(Truffle.invoke_primitive(:interop_call_c_with_mutex, callback, [rb_tr_wrap(block_arg), rb_tr_wrap(callback_arg)]))
  end

  def call_with_thread_locally_stored_block(function, *args, &block)
    # MRI puts the block to a thread local th->passed_block and later rb_funcall reads it,
    # we have to do the same
    # TODO (pitr-ch 14-Dec-2017): This is fixed just for rb_iterate with a rb_funcall in it combination
    previous_block = Thread.current[:__C_BLOCK__]
    begin
      Thread.current[:__C_BLOCK__] = block
      rb_tr_unwrap(Truffle.invoke_primitive(:interop_call_c_with_mutex, function, args.map! { |arg| Truffle::CExt.rb_tr_wrap(arg) }, &block))
    ensure
      Thread.current[:__C_BLOCK__] = previous_block
    end
  end

end
