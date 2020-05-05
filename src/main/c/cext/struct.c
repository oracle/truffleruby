#include <truffleruby-impl.h>

// Structs, rb_struct_*

VALUE rb_struct_aref(VALUE s, VALUE idx) {
  return RUBY_CEXT_INVOKE("rb_struct_aref", s, idx);
}

VALUE rb_struct_aset(VALUE s, VALUE idx, VALUE val) {
  return RUBY_CEXT_INVOKE("rb_struct_aset", s, idx, val);
}

VALUE rb_struct_define(const char *name, ...) {
  VALUE rb_name = name == NULL ? Qnil : rb_str_new_cstr(name);
  VALUE ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  while ((arg = (char *)polyglot_get_arg(1+i)) != NULL) {
    rb_ary_push(ary, rb_str_new_cstr(arg));
    i++;
  }
  return RUBY_CEXT_INVOKE("rb_struct_define_no_splat", rb_name, ary);
}

VALUE rb_struct_define_under(VALUE outer, const char *name, ...) {
  VALUE rb_name = name == NULL ? Qnil : rb_str_new_cstr(name);
  VALUE ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  while ((arg = (char *)polyglot_get_arg(2+i)) != NULL) {
    rb_ary_push(ary, rb_str_new_cstr(arg));
    i++;
  }
  return RUBY_CEXT_INVOKE("rb_struct_define_under_no_splat", outer, rb_name, ary);
}

VALUE rb_struct_new(VALUE klass, ...) {
  int members = polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_struct_size", klass));
  VALUE ary = rb_ary_new();
  int i = 0;
  while (i < members) {
    VALUE arg = polyglot_get_arg(1+i);
    rb_ary_push(ary, arg);
    i++;
  }
  return RUBY_CEXT_INVOKE("rb_struct_new_no_splat", klass, ary);
}

VALUE rb_struct_size(VALUE s) {
  return RUBY_INVOKE(s, "size");
}
