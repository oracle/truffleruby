/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include "ruby.h"
#include "rubyspec.h"

#ifdef __cplusplus
extern "C" {
#endif

static VALUE rb_tr_error_spec_not_implemented(VALUE self, VALUE str) {
  // One of the not implemented ruby.c functions using rb_tr_error()
  rb_str_shared_replace(str, str);
  return Qnil;
}

void Init_rb_tr_error_spec(void) {
  VALUE cls;
  cls = rb_define_class("CApiRbTrErrorSpecs", rb_cObject);
  rb_define_method(cls, "not_implemented_function", rb_tr_error_spec_not_implemented, 1);
}

#ifdef __cplusplus
}
#endif
