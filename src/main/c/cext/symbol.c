#include <truffleruby-impl.h>
#include <ruby/encoding.h>

// Symbol and ID, rb_sym*, rb_id*

ID rb_to_id(VALUE name) {
  return SYM2ID(RUBY_INVOKE(name, "to_sym"));
}

#undef rb_intern
ID rb_intern(const char *string) {
  return SYM2ID(RUBY_CEXT_INVOKE("rb_intern", rb_str_new_cstr(string)));
}

ID rb_intern2(const char *string, long length) {
  return SYM2ID(RUBY_CEXT_INVOKE("rb_intern", rb_str_new(string, length)));
}

ID rb_intern3(const char *name, long len, rb_encoding *enc) {
  return SYM2ID(RUBY_CEXT_INVOKE("rb_intern3", rb_str_new(name, len), rb_enc_from_encoding(enc)));
}

VALUE rb_sym2str(VALUE string) {
  return RUBY_INVOKE(string, "to_s");
}

const char *rb_id2name(ID id) {
    return RSTRING_PTR(rb_id2str(id));
}

VALUE rb_id2str(ID id) {
  return RUBY_CEXT_INVOKE("rb_id2str", ID2SYM(id));
}

int rb_is_class_id(ID id) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_is_class_id", ID2SYM(id)));
}

int rb_is_const_id(ID id) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_is_const_id", ID2SYM(id)));
}

int rb_is_instance_id(ID id) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_is_instance_id", ID2SYM(id)));
}

VALUE rb_check_symbol_cstr(const char *ptr, long len, rb_encoding *enc) {
  VALUE str = rb_enc_str_new(ptr, len, enc);
  return RUBY_CEXT_INVOKE("rb_check_symbol_cstr", str);
}

VALUE rb_sym_to_s(VALUE sym) {
  return RUBY_INVOKE(sym, "to_s");
}

#undef rb_sym2id
ID rb_sym2id(VALUE sym) {
  return rb_tr_sym2id(sym);;
}

#undef rb_id2sym
VALUE rb_id2sym(ID x) {
  return rb_tr_wrap(rb_tr_id2sym(x));
}

VALUE rb_to_symbol(VALUE name) {
  return RUBY_CEXT_INVOKE("rb_to_symbol", name);
}
