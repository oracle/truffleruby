#include "psd_native_ext.h"

VALUE psd_native_combine_greyscale_channel(VALUE self) {
  psd_logger("debug", "Beginning greyscale processing");

  uint32_t channel_count = FIX2UINT(rb_funcall(self, rb_intern("channels"), 0));
  uint32_t num_pixels = FIX2UINT(rb_iv_get(self, "@num_pixels"));
  uint32_t pixel_step = FIX2UINT(rb_funcall(self, rb_intern("pixel_step"), 0));

  VALUE* channel_data = RARRAY_PTR(rb_iv_get(self, "@channel_data"));
  uint32_t channel_length = FIX2UINT(rb_iv_get(self, "@channel_length"));

  uint32_t i, alpha, grey;
  for (i = 0; i < num_pixels; i += pixel_step) {
    if (channel_count == 2) {
      grey = FIX2UINT(channel_data[i]);
      alpha = FIX2UINT(channel_data[channel_length + i]);

      rb_ary_push(rb_iv_get(self, "@pixel_data"), INT2FIX(BUILD_PIXEL(grey, grey, grey, alpha)));
    } else {
      grey = FIX2UINT(channel_data[i]);
      rb_ary_push(rb_iv_get(self, "@pixel_data"), INT2FIX(BUILD_PIXEL(grey, grey, grey, 255)));
    }
  }

  return Qnil;
}