#include "psd_native_ext.h"

VALUE psd_native_layer_raw_parse_raw_bang(VALUE self) {
  psd_logger("debug", "Attempting to parse RAW encoded channel with native code...");

  int chan_pos = FIX2INT(rb_iv_get(self, "@chan_pos"));
  int chan_length = FIX2INT(rb_hash_aref(rb_iv_get(self, "@ch_info"), ID2SYM(rb_intern("length"))));
  VALUE channel_data = rb_iv_get(self, "@channel_data");

  VALUE data = psd_file_read_bytes(self, chan_length - 2);

  int i, j = 0;
  for (i = chan_pos; i < (chan_pos + chan_length - 2); i++) {
    rb_ary_store(channel_data, i, rb_ary_entry(data, j++));
  }

  rb_iv_set(self, "@chan_pos", INT2FIX(chan_pos + chan_length - 2));

  return Qnil;
}