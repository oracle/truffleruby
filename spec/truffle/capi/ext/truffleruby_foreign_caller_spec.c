/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
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

static VALUE call_binding(VALUE self) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(self), "binding"));
}

static VALUE call_binding_rb_funcall(VALUE self) {
  return rb_funcall(self, rb_intern("binding"), 0);
}

void Init_truffleruby_foreign_caller_spec(void) {
  VALUE cls = rb_define_class("CApiTruffleRubyForeignCallerSpecs", rb_cObject);
  rb_define_method(cls, "call_binding", call_binding, 0);
  rb_define_method(cls, "call_binding_rb_funcall", call_binding_rb_funcall, 0);
}

#ifdef __cplusplus
}
#endif
