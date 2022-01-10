/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>

// Structs, rb_struct_*

VALUE rb_struct_aref(VALUE s, VALUE idx) {
  return RUBY_CEXT_INVOKE("rb_struct_aref", s, idx);
}

VALUE rb_struct_aset(VALUE s, VALUE idx, VALUE val) {
  return RUBY_CEXT_INVOKE("rb_struct_aset", s, idx, val);
}

VALUE rb_struct_define(const char *name, ...) {
  VALUE rb_name = name == NULL ? Qnil : rb_str_new_cstr(name);
  VALUE ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  va_list args;
  va_start(args, name);
  while ((arg = (char *)polyglot_get_array_element(&args, i)) != NULL) {
    rb_ary_push(ary, rb_str_new_cstr(arg));
    i++;
  }
  va_end(args);
  return RUBY_CEXT_INVOKE("rb_struct_define_no_splat", rb_name, ary);
}

VALUE rb_struct_define_under(VALUE outer, const char *name, ...) {
  VALUE rb_name = name == NULL ? Qnil : rb_str_new_cstr(name);
  VALUE ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  va_list args;
  va_start(args, name);
  while ((arg = (char *)polyglot_get_array_element(&args, i)) != NULL) {
    rb_ary_push(ary, rb_str_new_cstr(arg));
    i++;
  }
  va_end(args);
  return RUBY_CEXT_INVOKE("rb_struct_define_under_no_splat", outer, rb_name, ary);
}

VALUE rb_struct_new(VALUE klass, ...) {
  int members = polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_struct_size", klass));
  VALUE ary = rb_ary_new();
  int i = 0;
  va_list args;
  va_start(args, klass);
  while (i < members) {
    VALUE arg = polyglot_get_array_element(&args, i);
    rb_ary_push(ary, arg);
    i++;
  }
  va_end(args);
  return RUBY_CEXT_INVOKE("rb_struct_new_no_splat", klass, ary);
}

VALUE rb_struct_size(VALUE s) {
  return RUBY_INVOKE(s, "size");
}
