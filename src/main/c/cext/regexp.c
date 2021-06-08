/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>

// Regexp, rb_reg_*

VALUE rb_backref_get(void) {
  return RUBY_CEXT_INVOKE("rb_backref_get");
}

void rb_backref_set(VALUE str) {
  RUBY_CEXT_INVOKE("rb_backref_set", str);
}

VALUE rb_reg_match_pre(VALUE match) {
  return RUBY_CEXT_INVOKE("rb_reg_match_pre", match);
}

VALUE rb_reg_new(const char *s, long len, int options) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_new", rb_tr_unwrap(rb_str_new(s, len)), options));
}

VALUE rb_reg_compile(VALUE str, int options, const char *sourcefile, int sourceline) {
  // TODO BJF May-29-2020 implement sourcefile, sourceline
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_compile", rb_tr_unwrap(str), options));
}

VALUE rb_reg_new_str(VALUE s, int options) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_new_str", rb_tr_unwrap(s), options));
}

VALUE rb_reg_nth_match(int nth, VALUE match) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_nth_match", nth, rb_tr_unwrap(match)));
}

int rb_reg_options(VALUE re) {
  return FIX2INT(RUBY_CEXT_INVOKE("rb_reg_options", re));
}

VALUE rb_reg_regcomp(VALUE str) {
  return RUBY_CEXT_INVOKE("rb_reg_regcomp", str);
}

VALUE rb_reg_match(VALUE re, VALUE str) {
  return RUBY_CEXT_INVOKE("rb_reg_match", re, str);
}
