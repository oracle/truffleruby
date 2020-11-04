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
  int total = polyglot_get_arg_count();
  int n = 4;
  // Callers _should_ have terminated the var args with a VALUE sized 0, but
  // some code uses a zero instead, and this can break. So we read the
  // arguments using the polyglot api.
  for (;n < total; n++) {
    VALUE arg = polyglot_get_arg(n);
    if (arg == (VALUE)0) {
      break;
    }

    rb_ary_push(rescued, arg);
  }
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

void rb_tr_init_exception(void) {
  cext_rb_protect = polyglot_get_member(rb_tr_cext, "rb_protect");
  cext_rb_ensure = polyglot_get_member(rb_tr_cext, "rb_ensure");
  cext_rb_rescue = polyglot_get_member(rb_tr_cext, "rb_rescue");
  cext_rb_rescue2 = polyglot_get_member(rb_tr_cext, "rb_rescue2");
}
