#ifndef PSD_NATIVE_UTIL
#define PSD_NATIVE_UTIL

VALUE psd_native_util_pad2(VALUE self, VALUE i);
VALUE psd_native_util_pad4(VALUE self, VALUE i);
VALUE psd_native_util_clamp(VALUE self, VALUE num, VALUE min, VALUE max);

#endif