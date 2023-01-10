/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>

// Enumerable and Enumerator, rb_enum*

VALUE rb_enumeratorize(VALUE obj, VALUE meth, int argc, const VALUE *argv) {
  return RUBY_CEXT_INVOKE("rb_enumeratorize", obj, meth, rb_ary_new4(argc, argv));
}

#undef rb_enumeratorize_with_size
VALUE rb_enumeratorize_with_size(VALUE obj, VALUE meth, int argc, const VALUE *argv, rb_enumerator_size_func *size_fn) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enumeratorize_with_size", rb_tr_unwrap(obj), rb_tr_unwrap(meth), rb_tr_unwrap(rb_ary_new4(argc, argv)), size_fn));
}
