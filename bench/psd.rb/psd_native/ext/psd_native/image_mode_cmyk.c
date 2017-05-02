#include "psd_native_ext.h"

VALUE psd_native_combine_cmyk_channel(VALUE self) {
  psd_logger("debug", "Beginning CMYK processing");
  
  uint32_t num_pixels = FIX2UINT(rb_iv_get(self, "@num_pixels"));
  uint32_t pixel_step = FIX2UINT(rb_funcall(self, rb_intern("pixel_step"), 0));

  VALUE* channels_info = RARRAY_PTR(rb_iv_get(self, "@channels_info"));
  VALUE* channel_data = RARRAY_PTR(rb_iv_get(self, "@channel_data"));
  uint32_t channel_length = FIX2UINT(rb_iv_get(self, "@channel_length"));
  int channel_count = RARRAY_LENINT(rb_iv_get(self, "@channels_info"));

  int i, j;
  uint32_t val, c, m, y, k;
  uint32_t r, g, b;
  uint32_t a = 255;
  VALUE rgb;
  
  // TODO(mg): truffle c has problems with dynamic stack allocations
  int * channel_ids = malloc(channel_count * sizeof(int));
  // int channel_ids[channel_count];
  for (i = 0; i < channel_count; i++) {
    channel_ids[i] = FIX2INT(rb_hash_aref(channels_info[i], ID2SYM(rb_intern("id"))));
  }

  // Loop through every pixel in the image
  for (i = 0; i < num_pixels; i += pixel_step) {
    c = m = y = k = 0;
    a = 255;

    for (j = 0; j < channel_count; j++) {
      if (channel_ids[j] == -2) continue;

      val = FIX2UINT(channel_data[i + (channel_length * j)]);

      switch (channel_ids[j]) {
        case -1: a = val; break;
        case 0: c = val; break;
        case 1: m = val; break;
        case 2: y = val; break;
        case 3: k = val; break;
      }
    }

    rgb = psd_native_cmyk_to_rgb(
      self, 
      INT2FIX(255 - c), 
      INT2FIX(255 - m), 
      INT2FIX(255 - y),
      INT2FIX(255 - k)
    );


    r = FIX2UINT(rb_hash_aref(rgb, ID2SYM(rb_intern("r"))));
    g = FIX2UINT(rb_hash_aref(rgb, ID2SYM(rb_intern("g"))));
    b = FIX2UINT(rb_hash_aref(rgb, ID2SYM(rb_intern("b"))));

    rb_ary_push(rb_iv_get(self, "@pixel_data"), INT2FIX(BUILD_PIXEL(r, g, b, a)));
  }

  free(channel_ids); //TODO(mg)
  return Qnil;
}