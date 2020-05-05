#include <truffleruby-impl.h>

// Enumerable and Enumerator, rb_enum*

VALUE rb_enumeratorize(VALUE obj, VALUE meth, int argc, const VALUE *argv) {
  return RUBY_CEXT_INVOKE("rb_enumeratorize", obj, meth, rb_ary_new4(argc, argv));
}

#undef rb_enumeratorize_with_size
VALUE rb_enumeratorize_with_size(VALUE obj, VALUE meth, int argc, const VALUE *argv, rb_enumerator_size_func *size_fn) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enumeratorize_with_size", rb_tr_unwrap(obj), rb_tr_unwrap(meth), rb_tr_unwrap(rb_ary_new4(argc, argv)), size_fn));
}
