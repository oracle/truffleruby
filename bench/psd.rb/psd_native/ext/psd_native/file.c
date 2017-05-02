#include "psd_native_ext.h"

VALUE psd_file_read_byte(VALUE self) {
  // @file.read(1).bytes[0]
  VALUE data = rb_funcall(psd_file(self), rb_intern("read"), 1, INT2FIX(1));
  return RARRAY_PTR(rb_funcall(data, rb_intern("bytes"), 0))[0];
}

VALUE psd_file_read_bytes(VALUE self, int bytes) {
  VALUE data = rb_funcall(psd_file(self), rb_intern("read"), 1, INT2FIX(bytes));
  return rb_funcall(rb_funcall(data, rb_intern("bytes"), 0), rb_intern("to_a"), 0);
}

VALUE psd_file(VALUE self) {
  return rb_iv_get(self, "@file");
}

int psd_file_tell(VALUE self) {
  return FIX2INT(rb_funcall(psd_file(self), rb_intern("tell"), 0));
}