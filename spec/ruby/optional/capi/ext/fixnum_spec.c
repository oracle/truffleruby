#include "ruby.h"
#include "rubyspec.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifdef HAVE_RB_FIX2UINT
static VALUE fixnum_spec_rb_fix2uint(VALUE self, VALUE value) {
  unsigned int i = rb_fix2uint(value);
  return UINT2NUM(i);
}
#endif

#ifdef HAVE_RB_FIX2INT
static VALUE fixnum_spec_rb_fix2int(VALUE self, VALUE value) {
  int i = rb_fix2int(value);
  return INT2NUM(i);
}
#endif


void Init_fixnum_spec(void) {
  VALUE cls;
  cls = rb_define_class("CApiFixnumSpecs", rb_cObject);

#ifdef HAVE_RB_FIX2UINT
  rb_define_method(cls, "rb_fix2uint", fixnum_spec_rb_fix2uint, 1);
#endif

#ifdef HAVE_RB_FIX2INT
  rb_define_method(cls, "rb_fix2int", fixnum_spec_rb_fix2int, 1);
#endif

  (void)cls;
}

#ifdef __cplusplus
}
#endif
