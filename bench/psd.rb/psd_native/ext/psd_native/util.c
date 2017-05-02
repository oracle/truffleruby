#include "psd_native_ext.h"

VALUE psd_native_util_pad2(VALUE self, VALUE i) {
  return INT2FIX((FIX2INT(i) + 1) & ~0x01);
}

VALUE psd_native_util_pad4(VALUE self, VALUE i) {
  return INT2FIX(((FIX2INT(i) + 4) & ~0x03) - 1);
}

VALUE psd_native_util_clamp(VALUE self, VALUE r_num, VALUE r_min, VALUE r_max) {
  int num = FIX2INT(r_num);
  int min = FIX2INT(r_min);
  int max = FIX2INT(r_max);

  return num > max ? r_max : (num < min ? r_min : r_num);
}