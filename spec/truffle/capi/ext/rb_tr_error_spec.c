#include "ruby.h"
#include "rubyspec.h"

#ifdef __cplusplus
extern "C" {
#endif

static VALUE rb_tr_error_spec_not_implemented(VALUE self, VALUE str) {
  // One of the not implemented ruby.c functions using rb_tr_error()
  rb_str_shared_replace(str, str);
  return Qnil;
}

void Init_rb_tr_error_spec(void) {
  VALUE cls;
  cls = rb_define_class("CApiRbTrErrorSpecs", rb_cObject);
  rb_define_method(cls, "not_implemented_function", rb_tr_error_spec_not_implemented, 1);
}

#ifdef __cplusplus
}
#endif
