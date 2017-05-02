#include "psd_native_ext.h"

VALUE psd_native_combine_rgb_channel(VALUE self) {
  psd_logger("debug", "Beginning RGB processing");
  
  uint32_t num_pixels = FIX2UINT(rb_iv_get(self, "@num_pixels"));
  uint32_t pixel_step = FIX2UINT(rb_funcall(self, rb_intern("pixel_step"), 0));

  VALUE* channels_info = RARRAY_PTR(rb_iv_get(self, "@channels_info"));
  VALUE* channel_data = RARRAY_PTR(rb_iv_get(self, "@channel_data"));
  uint32_t channel_length = FIX2UINT(rb_iv_get(self, "@channel_length"));
  int channel_count = RARRAY_LENINT(rb_iv_get(self, "@channels_info"));

  int i, j;
  uint32_t val, color;

  // int channel_ids[channel_count];
  int * channel_ids = malloc(sizeof(int) * channel_count); // TODO(mg): trufflec cannot dynamically allocate stack space
  for (i = 0; i < channel_count; i++) {
    channel_ids[i] = FIX2INT(rb_hash_aref(channels_info[i], ID2SYM(rb_intern("id"))));
  }

  VALUE pixel_data = rb_iv_get(self, "@pixel_data");

  // Loop through every pixel in the image
  for (i = 0; i < num_pixels; i += pixel_step) {
    color = 0x000000ff;

    // And every channel for every pixel
    for (j = 0; j < channel_count; j++) {
      if (channel_ids[j] == -2) continue;

      val = FIX2UINT(channel_data[i + (channel_length * j)]);

      // Get the hash containing channel info
      switch (channel_ids[j]) {
        case -1: color = (color & 0xffffff00) | val; break;         // A
        case 0:  color = (color & 0x00ffffff) | (val << 24); break; // R
        case 1:  color = (color & 0xff00ffff) | (val << 16); break; // G
        case 2:  color = (color & 0xffff00ff) | (val << 8); break;  // B
      }
    }

    rb_ary_push(pixel_data, INT2FIX(color));
  }
  
  free(channel_ids); // TODO(mg)
  return Qnil;
}