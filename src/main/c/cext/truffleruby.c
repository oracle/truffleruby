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

// Non-standard additional functions, rb_tr_*

void rb_tr_error(const char *message) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_tr_error", rb_str_new_cstr(message));
  abort();
}

void rb_tr_log_warning(const char *message) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_tr_log_warning", rb_str_new_cstr(message));
}

VALUE rb_java_class_of(VALUE obj) {
  return RUBY_CEXT_INVOKE("rb_java_class_of", obj);
}

VALUE rb_java_to_string(VALUE obj) {
  return RUBY_CEXT_INVOKE("rb_java_to_string", obj);
}

// BasicObject#equal?
int rb_tr_obj_equal(VALUE first, VALUE second) {
  return RTEST(rb_funcall(first, rb_intern("equal?"), 1, second));
}

int rb_tr_flags(VALUE value) {
  int flags = 0;
  if (OBJ_FROZEN(value)) {
    flags |= RUBY_FL_FREEZE;
  }
  if (RARRAY_LEN(rb_obj_instance_variables(value)) > 0) {
    flags |= RUBY_FL_EXIVAR;
  }
  // TODO BJF Nov-11-2017 Implement more flags
  return flags;
}

void rb_tr_add_flags(VALUE value, int flags) {
  if (flags & RUBY_FL_FREEZE) {
    rb_obj_freeze(value);
  }
}
