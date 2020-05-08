#include <truffleruby-impl.h>
#include <ruby/thread_native.h>

// Threads, rb_thread_*, rb_nativethread_*

int rb_thread_alone(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_thread_alone"));
}

VALUE rb_thread_current(void) {
  return RUBY_INVOKE(rb_cThread, "current");
}

VALUE rb_thread_local_aref(VALUE thread, ID id) {
  return RUBY_INVOKE(thread, "[]", ID2SYM(id));
}

VALUE rb_thread_local_aset(VALUE thread, ID id, VALUE val) {
  return RUBY_INVOKE(thread, "[]=", ID2SYM(id), val);
}

void rb_thread_wait_for(struct timeval time) {
  double seconds = (double)time.tv_sec + (double)time.tv_usec/1000000;
  polyglot_invoke(rb_tr_unwrap(rb_mKernel), "sleep", seconds);
}

VALUE rb_thread_wakeup(VALUE thread) {
  return RUBY_INVOKE(thread, "wakeup");
}

VALUE rb_thread_create(VALUE (*fn)(ANYARGS), void *arg) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_thread_create", fn, arg));
}

void rb_thread_schedule(void) {
  RUBY_INVOKE_NO_WRAP(rb_cThread, "pass");
}

rb_nativethread_id_t rb_nativethread_self() {
  return RUBY_CEXT_INVOKE("rb_nativethread_self");
}

void rb_nativethread_lock_initialize(rb_nativethread_lock_t *lock) {
  *lock = RUBY_CEXT_INVOKE("rb_nativethread_lock_initialize");
}

void rb_nativethread_lock_destroy(rb_nativethread_lock_t *lock) {
  *lock = RUBY_CEXT_INVOKE("rb_nativethread_lock_destroy", *lock);
}

void rb_nativethread_lock_lock(rb_nativethread_lock_t *lock) {
  RUBY_INVOKE_NO_WRAP(*lock, "lock");
}

void rb_nativethread_lock_unlock(rb_nativethread_lock_t *lock) {
  RUBY_INVOKE_NO_WRAP(*lock, "unlock");
}