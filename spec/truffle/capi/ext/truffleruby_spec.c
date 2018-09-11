#include "ruby.h"
#include "rubyspec.h"

#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

static VALUE truffleruby_spec_truffleruby(VALUE self) {
  #ifdef TRUFFLERUBY
    return Qtrue;
  #else
    return Qfalse;
  #endif
}

void Init_truffleruby_spec(void) {
  VALUE cls;
  cls = rb_define_class("CApiTruffleRubySpecs", rb_cObject);
  rb_define_method(cls, "truffleruby", truffleruby_spec_truffleruby, 0);
}

#ifdef __cplusplus
}
#endif
