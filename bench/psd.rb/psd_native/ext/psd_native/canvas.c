#include "psd_native_ext.h"

VALUE psd_canvas_to_pixel_array(VALUE canvas) {
  return rb_funcall(
    rb_funcall(canvas, rb_intern("canvas"), 0),
    rb_intern("pixels"),
    0
  );
}