/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include "ruby.h"
#include "ruby/thread.h"
#include "rubyspec.h"

#ifdef __cplusplus
extern "C" {
#endif

static VALUE truffleCExt;

static VALUE has_lock(VALUE self) {
  return rb_tr_cext_lock_owned_p();
}

static void* called_without_gvl(void* data) {
  return rb_tr_cext_lock_owned_p();
}

static VALUE has_lock_in_call_without_gvl(VALUE self) {
  return rb_thread_call_without_gvl(called_without_gvl, 0, RUBY_UBF_IO, 0);
}

static VALUE has_lock_in_rb_funcall(VALUE self) {
  return rb_funcall(truffleCExt, rb_intern("cext_lock_owned?"), 0);
}

void Init_truffleruby_lock_spec(void) {
  truffleCExt = rb_const_get(rb_const_get(rb_cObject, rb_intern("Truffle")), rb_intern("CExt"));
  VALUE cls = rb_define_class("CApiTruffleRubyLockSpecs", rb_cObject);
  rb_define_method(cls, "has_lock?", has_lock, 0);
  rb_define_method(cls, "has_lock_in_call_without_gvl?", has_lock_in_call_without_gvl, 0);
  rb_define_method(cls, "has_lock_in_rb_funcall?", has_lock_in_rb_funcall, 0);
}

#ifdef __cplusplus
}
#endif
