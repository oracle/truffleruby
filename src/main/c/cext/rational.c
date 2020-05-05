#include <truffleruby-impl.h>

// Rational, rb_rational_*

VALUE rb_Rational(VALUE num, VALUE den) {
  return RUBY_CEXT_INVOKE("rb_Rational", num, den);
}

VALUE rb_rational_raw(VALUE num, VALUE den) {
  return RUBY_CEXT_INVOKE("rb_rational_raw", num, den);
}

VALUE rb_rational_new(VALUE num, VALUE den) {
  return RUBY_CEXT_INVOKE("rb_rational_new", num, den);
}

VALUE rb_rational_num(VALUE rat) {
  return RUBY_INVOKE(rat, "numerator");
}

VALUE rb_rational_den(VALUE rat) {
  return RUBY_INVOKE(rat, "denominator");
}

VALUE rb_flt_rationalize_with_prec(VALUE value, VALUE precision) {
  return RUBY_INVOKE(value, "rationalize", precision);
}

VALUE rb_flt_rationalize(VALUE value) {
    return RUBY_INVOKE(value, "rationalize");
}
