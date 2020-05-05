#include <truffleruby-impl.h>

// Marshal, rb_marshal_*

VALUE rb_marshal_dump(VALUE obj, VALUE port) {
  return RUBY_CEXT_INVOKE("rb_marshal_dump", obj, port);
}

VALUE rb_marshal_load(VALUE port) {
  return RUBY_CEXT_INVOKE("rb_marshal_load", port);
}
