#include <truffleruby-impl.h>

// Float, rb_float_*

VALUE rb_float_new(double value) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_float_new", value));
}

VALUE rb_Float(VALUE value) {
  return RUBY_CEXT_INVOKE("rb_Float", value);
}

double rb_float_value(VALUE value) {
  return polyglot_as_double(RUBY_CEXT_INVOKE_NO_WRAP("RFLOAT_VALUE", value));
}
