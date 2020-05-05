#include <truffleruby-impl.h>

// Complex, rb_complex_*

VALUE rb_Complex(VALUE real, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_Complex", real, imag);
}

VALUE rb_complex_new(VALUE real, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_complex_new", real, imag);
}

VALUE rb_complex_raw(VALUE real, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_complex_raw", real, imag);
}

VALUE rb_complex_polar(VALUE r, VALUE theta) {
  return RUBY_CEXT_INVOKE("rb_complex_polar", r, theta);
}

VALUE rb_complex_real(VALUE complex) {
  return RUBY_INVOKE(complex, "real");
}

VALUE rb_complex_imag(VALUE complex) {
  return RUBY_INVOKE(complex, "imag");
}

VALUE rb_complex_set_real(VALUE complex, VALUE real) {
  return RUBY_CEXT_INVOKE("rb_complex_set_real", complex, real);
}

VALUE rb_complex_set_imag(VALUE complex, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_complex_set_imag", complex, imag);
}
