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
#include "rubyspec.h"
#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

static VALUE value_comparison_with_nil(VALUE self, VALUE val) {
  return val == Qnil ? Qtrue : Qfalse;
}

static VALUE value_array_ptr_access(VALUE self, VALUE ary) {
  VALUE *ptr = RARRAY_PTR(ary);
  return ptr[0];
}

static VALUE value_array_ptr_memcpy(VALUE self, VALUE ary) {
  VALUE *ptr = RARRAY_PTR(ary);
  VALUE *cpy = malloc(RARRAY_LEN(ary) * sizeof(VALUE));
  VALUE res;
  memcpy(cpy, ptr, RARRAY_LEN(ary) * sizeof(VALUE));
  res = cpy[1];
  free(cpy);
  return res;
}

static VALUE our_static_value;

static VALUE value_store_in_static(VALUE self, VALUE val) {
  our_static_value = val;
  return val;
}

void Init_handle_conversion_spec(void) {
  VALUE cls = rb_define_class("CAPIHandleConversionTest", rb_cObject);
  rb_define_method(cls, "value_comparison_with_nil", value_comparison_with_nil, 1);
  rb_define_method(cls, "value_array_ptr_access", value_array_ptr_access, 1);
  rb_define_method(cls, "value_array_ptr_memcpy", value_array_ptr_memcpy, 1);
  rb_define_method(cls, "value_store_in_static", value_store_in_static, 1);
}

#ifdef __cplusplus
}
#endif
