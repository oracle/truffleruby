#include <truffleruby-impl.h>

// GC, rb_gc_*

void rb_gc_register_address(VALUE *address) {
}

void rb_gc_unregister_address(VALUE *address) {
  // VALUE is only ever in managed memory. So, it is already garbage collected.
}

void rb_gc_mark(VALUE ptr) {
  polyglot_invoke(RUBY_CEXT, "rb_gc_mark", ptr);
}

VALUE rb_gc_enable() {
  return RUBY_CEXT_INVOKE("rb_gc_enable");
}

VALUE rb_gc_disable() {
  return RUBY_CEXT_INVOKE("rb_gc_disable");
}

void rb_gc(void) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_gc");
}

void rb_gc_force_recycle(VALUE obj) {
  // Comments in MRI imply rb_gc_force_recycle functions as a GC guard
  RB_GC_GUARD(obj);
}

VALUE rb_gc_latest_gc_info(VALUE key) {
  return RUBY_CEXT_INVOKE("rb_gc_latest_gc_info", key);
}

void rb_gc_register_mark_object(VALUE obj) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_gc_register_mark_object", obj);
}
