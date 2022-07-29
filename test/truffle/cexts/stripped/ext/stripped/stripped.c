#include <stdio.h>
#include <ruby.h>

static VALUE stripped_stripped(VALUE self) {
  return rb_str_new_cstr("Stripped!");
}

void Init_stripped() {
  VALUE mod = rb_define_module("Stripped");
  rb_define_singleton_method(mod, "stripped", stripped_stripped, 0);
}
