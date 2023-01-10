/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>

// Fiber, rb_fiber_*

VALUE rb_fiber_current() {
  return RUBY_CEXT_INVOKE("rb_fiber_current");
}

VALUE rb_fiber_alive_p(VALUE fiber) {
  return rb_funcallv(fiber, rb_intern("alive?"), 0, 0);
}

VALUE rb_fiber_resume(VALUE fib, int argc, const VALUE *argv) {
    return rb_fiber_resume_kw(fib, argc, argv, RB_NO_KEYWORDS);
}

VALUE rb_fiber_resume_kw(VALUE fib, int argc, const VALUE *argv, int kw_splat) {
  return rb_funcallv_kw(fib, rb_intern("resume"), argc, argv, kw_splat);
}

VALUE rb_fiber_yield(int argc, const VALUE *argv) {
  return rb_fiber_yield_kw(argc, argv, RB_NO_KEYWORDS);
}

VALUE rb_fiber_yield_kw(int argc, const VALUE *argv, int kw_splat) {
  VALUE rb_cFiber = RUBY_CEXT_INVOKE("rb_cFiber");
  return rb_funcallv_kw(rb_cFiber, rb_intern("yield"), argc, argv, kw_splat);
}

VALUE rb_fiber_new(VALUE (*function)(ANYARGS), VALUE value) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_fiber_new", function, rb_tr_unwrap(value)));
}
