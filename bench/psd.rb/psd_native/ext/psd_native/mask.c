#include "psd_native_ext.h"

VALUE psd_native_mask_apply_bang(VALUE self) {
  psd_logger("debug", "Applying mask with native code");

  int doc_width = FIX2INT(rb_iv_get(self, "@doc_width"));
  int doc_height = FIX2INT(rb_iv_get(self, "@doc_height"));

  VALUE layer = rb_iv_get(self, "@layer");
  VALUE canvas = rb_iv_get(self, "@canvas");
  VALUE mask = rb_funcall(layer, rb_intern("mask"), 0);
  VALUE *mask_data = RARRAY_PTR(rb_funcall(rb_funcall(layer, rb_intern("image"), 0), rb_intern("mask_data"), 0));

  int mask_height = FIX2INT(rb_funcall(mask, rb_intern("height"), 0));
  int mask_width = FIX2INT(rb_funcall(mask, rb_intern("width"), 0));
  int mask_left = FIX2INT(rb_funcall(mask, rb_intern("left"), 0));
  int mask_top = FIX2INT(rb_funcall(mask, rb_intern("top"), 0));

  int layer_height = FIX2INT(rb_funcall(layer, rb_intern("height"), 0));
  int layer_width = FIX2INT(rb_funcall(layer, rb_intern("width"), 0));
  int layer_left = FIX2INT(rb_funcall(layer, rb_intern("left"), 0));
  int layer_top = FIX2INT(rb_funcall(layer, rb_intern("top"), 0));

  PIXEL color;
  int x, y, doc_x, doc_y, layer_x, layer_y, alpha;
  int i = 0;
  for (y = 0; y < mask_height; y++) {
    for (x = 0; x < mask_width; x++) {
      doc_x = mask_left + x;
      doc_y = mask_top + y;
      layer_x = doc_x - layer_left;
      layer_y = doc_y - layer_top;

      if (layer_x < 0 || layer_x >= layer_width || layer_y < 0 || layer_y >= layer_height) continue;

      color = FIX2UINT(rb_funcall(canvas, rb_intern("[]"), 2, INT2FIX(layer_x), INT2FIX(layer_y)));

      if (doc_x < 0 || doc_x >= doc_width || doc_y < 0 || doc_y > doc_height) {
        alpha = 0;
      } else {
        alpha = FIX2INT(mask_data[i]);
      }

      color = (color & 0xffffff00) | (A(color) * alpha / 255);
      rb_funcall(canvas, rb_intern("[]="), 3, INT2FIX(layer_x), INT2FIX(layer_y), INT2FIX(color));

      i++;
    }
  }

  return Qnil;
}