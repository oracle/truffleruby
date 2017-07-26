#include <ruby.h>

VALUE baz(VALUE bob) {
  return rb_funcall(bob, rb_intern("call"), 0);
}

VALUE foo(VALUE self, VALUE bar) {
  return rb_funcall(bar, rb_intern("bar"), 1, rb_proc_new(baz, Qnil));
}

void Init_backtraces() {
  VALUE module = rb_define_module("Backtraces");
  rb_define_module_function(module, "foo", &foo, 1);
}
