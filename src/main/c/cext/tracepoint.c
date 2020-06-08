#include <truffleruby-impl.h>
#include <ruby/debug.h>

// TracePoint, rb_tracepoint_*

VALUE rb_tracepoint_new(VALUE target_thval, rb_event_flag_t events, void (*func)(VALUE, void *), void *data) {
  // target_thval parameter is unused as of MRI 2.6
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_tracepoint_new", events, func, data));
}

VALUE rb_tracepoint_enable(VALUE tpval) {
  return RUBY_INVOKE(tpval, "enable");
}

VALUE rb_tracepoint_disable(VALUE tpval) {
  return RUBY_INVOKE(tpval, "disable");
}

VALUE rb_tracepoint_enabled_p(VALUE tpval) {
  return RUBY_INVOKE(tpval, "enabled?");
}
