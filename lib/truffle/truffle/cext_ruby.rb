# truffleruby_primitives: true

# Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::CExt
  # Methods defined in this file are not considered as Ruby code implementing MRI C parts,
  # see org.truffleruby.cext.CExtNodes.BlockProcNode

  # methods defined with rb_define_method are normal Ruby methods therefore they cannot be defined in the cext.rb file
  # file because blocks passed as arguments would be skipped by org.truffleruby.cext.CExtNodes.BlockProcNode
  def rb_define_method(mod, name, function, argc)
    if argc < -2 or 15 < argc
      raise ArgumentError, "arity out of range: #{argc} for -2..15"
    end

    method_body = Truffle::Graal.copy_captured_locals -> *args, &block do
      if argc == -1 # (int argc, VALUE *argv, VALUE obj)
        args = [args.size, Truffle::CExt.RARRAY_PTR(args), Primitive.cext_wrap(self)]
      elsif argc == -2 # (VALUE obj, VALUE rubyArrayArgs)
        args = [Primitive.cext_wrap(self), Primitive.cext_wrap(args)]
      elsif argc >= 0 # (VALUE obj); (VALUE obj, VALUE arg1); (VALUE obj, VALUE arg1, VALUE arg2); ...
        if args.size != argc
          raise ArgumentError, "wrong number of arguments (given #{args.size}, expected #{argc})"
        end
        args = [Primitive.cext_wrap(self), *args.map! { |arg| Primitive.cext_wrap(arg) }]
      end

      exc = $!
      Primitive.thread_set_exception(nil)
      # Using raw execute instead of #call here to avoid argument conversion

      # We must set block argument if given here so that the
      # `rb_block_*` functions will be able to find it by walking the
      # stack.
      res = Primitive.cext_unwrap(Primitive.call_with_c_mutex_and_frame(function, args, Primitive.caller_special_variables_if_fast, block))
      Primitive.thread_set_exception(exc)
      res
    end

    # Even if the argc is -2, the arity number
    # is still any number of arguments, -1
    arity = argc == -2 ? -1 : argc

    method_body_with_arity = Primitive.proc_specify_arity(method_body, arity)
    mod.define_method(name, method_body_with_arity)
  end
end
