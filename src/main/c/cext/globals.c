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
#include <internal/variable.h>

// Global variables, rb_gvar_*, rb_gv_*

VALUE rb_gvar_var_getter(ID id, VALUE *data) {
  return *data;
}

void rb_gvar_var_setter(VALUE val, ID id, VALUE *data) {
  *data = val;
}

void rb_define_hooked_variable(const char *name, VALUE *var, VALUE (*getter)(ANYARGS), void (*setter)(ANYARGS)) {
  if (!getter) {
    getter = rb_gvar_var_getter;
  }

  if (!setter) {
    setter = rb_gvar_var_setter;
  }

  polyglot_invoke(RUBY_CEXT, "rb_define_hooked_variable", rb_tr_unwrap(rb_str_new_cstr(name)), var, getter, setter);
}

void rb_gvar_readonly_setter(VALUE val, ID id, VALUE *data) {
  rb_raise(rb_eNameError, "read-only variable");
}

void rb_define_readonly_variable(const char *name, const VALUE *var) {
  rb_define_hooked_variable(name, (VALUE *)var, NULL, rb_gvar_readonly_setter);
}

void rb_define_variable(const char *name, VALUE *var) {
  rb_define_hooked_variable(name, var, 0, 0);
}

VALUE rb_f_global_variables(void) {
  return RUBY_CEXT_INVOKE("rb_f_global_variables");
}

VALUE rb_gv_set(const char *name, VALUE value) {
  return RUBY_CEXT_INVOKE("rb_gv_set", rb_str_new_cstr(name), value);
}

VALUE rb_gv_get(const char *name) {
  return RUBY_CEXT_INVOKE("rb_gv_get", rb_str_new_cstr(name));
}

// $SAFE

void rb_check_trusted(VALUE obj) {
  rb_warning("rb_check_trusted is deprecated and will be removed in Ruby 3.2.");
}

// $VERBOSE

VALUE rb_tr_ruby_verbose_ptr;

VALUE *rb_ruby_verbose_ptr(void) {
  rb_tr_ruby_verbose_ptr = RUBY_CEXT_INVOKE("rb_ruby_verbose_ptr");
  return &rb_tr_ruby_verbose_ptr;
}

// $DEBUG

VALUE rb_tr_ruby_debug_ptr;

VALUE *rb_ruby_debug_ptr(void) {
  rb_tr_ruby_debug_ptr = RUBY_CEXT_INVOKE("rb_ruby_debug_ptr");
  return &rb_tr_ruby_debug_ptr;
}
