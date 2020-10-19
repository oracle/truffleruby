#include <truffleruby-impl.h>

// Range, rb_range_*

VALUE rb_range_new(VALUE beg, VALUE end, int exclude_end) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_range_new", rb_tr_unwrap(beg), rb_tr_unwrap(end), exclude_end));
}

/* This function can not be inlined as the two rb_intern macros
   generate static variables, and would produce unwanted
   warnings. This does mean that the start and end VALUEs will be
   converted to native handles and back if Sulong doesn't choose to
   inline this function, but this is unlikely to cause a major
   performance issue.
 */
int rb_range_values(VALUE range, VALUE *begp, VALUE *endp, int *exclp) {
  if (!rb_obj_is_kind_of(range, rb_cRange)) {
    if (!rb_respond_to(range, rb_intern("begin"))) return Qfalse;
    if (!rb_respond_to(range, rb_intern("end"))) return Qfalse;
  }

  *begp = RUBY_INVOKE(range, "begin");
  *endp = RUBY_INVOKE(range, "end");
  *exclp = (int) RTEST(RUBY_INVOKE(range, "exclude_end?"));
  return Qtrue;
}

VALUE rb_range_beg_len(VALUE range, long *begp, long *lenp, long len, int err) {
  long beg, end, origbeg, origend;
  VALUE b, e;
  int excl;

  if (!rb_range_values(range, &b, &e, &excl)) {
    return Qfalse;
  }

  beg = NUM2LONG(b);
  end = NUM2LONG(e);
  origbeg = beg;
  origend = end;
  if (beg < 0) {
    beg += len;
    if (beg < 0) {
      goto out_of_range;
    }
  }
  if (end < 0) {
    end += len;
  }
  if (!excl) {
    end++;                        /* include end point */
  }
  if (err == 0 || err == 2) {
    if (beg > len) {
      goto out_of_range;
    }
    if (end > len) {
      end = len;
    }
  }
  len = end - beg;
  if (len < 0) {
    len = 0;
  }

  *begp = beg;
  *lenp = len;
  return Qtrue;

out_of_range:
  if (err) {
    rb_raise(rb_eRangeError, "%ld..%s%ld out of range",
             origbeg, excl ? "." : "", origend);
  }
  return Qnil;
}
