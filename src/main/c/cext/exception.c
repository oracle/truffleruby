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
#include <errno.h>

// Exceptions, rb_exc_* and function to raise and rescue Ruby Exceptions from C

VALUE rb_exc_new(VALUE etype, const char *ptr, long len) {
  return RUBY_INVOKE(etype, "new", rb_str_new(ptr, len));
}

VALUE rb_exc_new_cstr(VALUE exception_class, const char *message) {
  return RUBY_INVOKE(exception_class, "new", rb_str_new_cstr(message));
}

VALUE rb_exc_new_str(VALUE exception_class, VALUE message) {
  return RUBY_INVOKE(exception_class, "new", message);
}

void rb_exc_raise(VALUE exception) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_exc_raise", exception);
  rb_tr_error("rb_exc_raise should not return");
}

static void rb_protect_write_status(int *status, int value) {
  if (status != NULL) {
    *status = value;
  }
}

VALUE (*cext_rb_protect)(VALUE (*function)(VALUE), void *data, void (*write_status)(int *status, int value), int *status);

VALUE rb_protect(VALUE (*function)(VALUE), VALUE data, int *status) {
  return cext_rb_protect(function, data, rb_protect_write_status, status);
}

void rb_jump_tag(int status) {
  if (status) {
    polyglot_invoke(RUBY_CEXT, "rb_jump_tag", status);
  }
  rb_tr_error("rb_jump_tag should not return");
}

void rb_set_errinfo(VALUE error) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_set_errinfo", error);
}

VALUE rb_errinfo(void) {
  return RUBY_CEXT_INVOKE("rb_errinfo");
}

void rb_syserr_fail(int eno, const char *message) {
  polyglot_invoke(RUBY_CEXT, "rb_syserr_fail", eno, rb_tr_unwrap(rb_str_new_cstr(message == NULL ? "" : message)));
  rb_tr_error("rb_syserr_fail should not return");
}

void rb_sys_fail(const char *message) {
  int n = errno;
  errno = 0;

  if (n == 0) {
    rb_bug("rb_sys_fail(%s) - errno == 0", message ? message : "");
  }
  rb_syserr_fail(n, message);
}

VALUE rb_syserr_new_str(int n, VALUE mesg) {
  return RUBY_CEXT_INVOKE("rb_syserr_new", INT2FIX(n), mesg);
}

VALUE make_errno_exc_str(VALUE mesg) {
  int n = errno;

  errno = 0;
  if (!mesg) mesg = Qnil;
  if (n == 0) {
    const char *s = !NIL_P(mesg) ? RSTRING_PTR(mesg) : "";
    rb_bug("rb_sys_fail_str(%s) - errno == 0", s);
  }
  return rb_syserr_new_str(n, mesg);
}

void rb_sys_fail_str(VALUE mesg) {
  rb_exc_raise(make_errno_exc_str(mesg));
}

VALUE (*cext_rb_ensure)(VALUE (*b_proc)(VALUE), void* data1, VALUE (*e_proc)(VALUE), void* data2);

VALUE rb_ensure(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*e_proc)(ANYARGS), VALUE data2) {
  return cext_rb_ensure(b_proc, data1, e_proc, data2);
}

VALUE (*cext_rb_rescue)(VALUE (*b_proc)(VALUE data), void* data1, VALUE (*r_proc)(VALUE data, VALUE e), void* data2);

VALUE rb_rescue(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2) {
  return cext_rb_rescue(b_proc, data1, r_proc, data2);
}

VALUE (*cext_rb_rescue2)(VALUE (*b_proc)(VALUE data), void* data1, VALUE (*r_proc)(VALUE data, VALUE e), void* data2, void* rescued);

VALUE rb_rescue2(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2, ...) {
  VALUE rescued = rb_ary_new();
  va_list args;
  va_start(args, data2);
  int total = polyglot_get_array_size(args);
  int n = 0;
  for (;n < total; n++) {
    VALUE arg = polyglot_get_array_element(args, n);
    if (arg == (VALUE)0) { /* A 0 marks the end of the arguments so we break here rather than adding it to the array.*/
      break;
    }

    rb_ary_push(rescued, arg);
  }
  va_end(args);
  return cext_rb_rescue2(b_proc, data1, r_proc, data2, rb_tr_unwrap(rescued));
}

VALUE rb_make_backtrace(void) {
  return RUBY_CEXT_INVOKE("rb_make_backtrace");
}

void rb_throw(const char *tag, VALUE val) {
  rb_throw_obj(rb_intern(tag), val);
}

void rb_throw_obj(VALUE tag, VALUE value) {
  RUBY_INVOKE_NO_WRAP(rb_mKernel, "throw", tag, value ? value : Qnil);
  rb_tr_error("rb_throw_obj should not return");
}

VALUE rb_catch(const char *tag, VALUE (*func)(ANYARGS), VALUE data) {
  return rb_catch_obj(rb_intern(tag), func, data);
}

VALUE rb_catch_obj(VALUE t, VALUE (*func)(ANYARGS), VALUE data) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_catch_obj", rb_tr_unwrap(t), func, rb_tr_unwrap(data)));
}

void rb_memerror(void) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_memerror");
  rb_tr_error("rb_memerror should not return");
}

NORETURN(void rb_eof_error(void)) {
  rb_raise(rb_eEOFError, "end of file reached");
}

void rb_bug(const char *fmt, ...) {
  rb_tr_error("rb_bug not yet implemented");
}

VALUE rb_make_exception(int argc, const VALUE *argv) {
  return RUBY_CEXT_INVOKE("rb_make_exception", rb_ary_new4(argc, argv));
}

void rb_tr_init_exception(void) {
  cext_rb_protect = polyglot_get_member(rb_tr_cext, "rb_protect");
  cext_rb_ensure = polyglot_get_member(rb_tr_cext, "rb_ensure");
  cext_rb_rescue = polyglot_get_member(rb_tr_cext, "rb_rescue");
  cext_rb_rescue2 = polyglot_get_member(rb_tr_cext, "rb_rescue2");
}
