#include "ruby.h"
#include "ruby/thread.h"
#include "rubyspec.h"

#ifdef __cplusplus
extern "C" {
#endif

static VALUE thread_spec_rb_thread_call_without_gvl_native_function(VALUE self) {
  // Must be a real native function, see https://github.com/oracle/truffleruby/issues/2090
  pid_t ret = (pid_t) (long) rb_thread_call_without_gvl((void *(*)(void *)) getpid, 0, RUBY_UBF_IO, 0);
  return LONG2FIX(ret);
}

void Init_truffleruby_thread_spec(void) {
  VALUE cls = rb_define_class("CApiTruffleRubyThreadSpecs", rb_cObject);
  rb_define_method(cls, "rb_thread_call_without_gvl_native_function", thread_spec_rb_thread_call_without_gvl_native_function, 0);
}

#ifdef __cplusplus
}
#endif
