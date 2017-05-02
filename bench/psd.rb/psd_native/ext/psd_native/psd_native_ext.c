#include "psd_native_ext.h"

static VALUE psd_class;
static VALUE logger;

void Init_psd_native() {
  // TODO(CS)
  //psd_class = rb_const_get(rb_cObject, rb_intern("PSD"));
  //logger = rb_funcall(psd_class, rb_intern("logger"), 0);

  VALUE PSDNative = rb_define_module("PSDNative");
  VALUE ImageMode = rb_define_module_under(PSDNative, "ImageMode");

  // RGB Processing
  VALUE ImageMode_RGB = rb_define_module_under(ImageMode, "RGB");
  rb_define_private_method(ImageMode_RGB, "combine_rgb_channel", psd_native_combine_rgb_channel, 0);

  // CMYK Processing
  VALUE ImageMode_CMYK = rb_define_module_under(ImageMode, "CMYK");
  rb_define_private_method(ImageMode_CMYK, "combine_cmyk_channel", psd_native_combine_cmyk_channel, 0);

  // Greyscale Processing
  VALUE ImageMode_Greyscale = rb_define_module_under(ImageMode, "Greyscale");
  rb_define_private_method(ImageMode_Greyscale, "combine_greyscale_channel", psd_native_combine_greyscale_channel, 0);

  // RLE decoding
  VALUE ImageFormat = rb_define_module_under(PSDNative, "ImageFormat");
  VALUE RLE = rb_define_module_under(ImageFormat, "RLE");
  rb_define_private_method(RLE, "decode_rle_channel", psd_native_decode_rle_channel, 0);

  // RAW decoding
  VALUE LayerRAW = rb_define_module_under(ImageFormat, "LayerRAW");
  rb_define_private_method(LayerRAW, "parse_raw!", psd_native_layer_raw_parse_raw_bang, 0);

  // Color functions
  VALUE Color = rb_define_module_under(PSDNative, "Color");
  rb_define_module_function(Color, "cmyk_to_rgb", psd_native_cmyk_to_rgb, 4);

  // Compose functions
  VALUE Compose = rb_define_module_under(PSDNative, "Compose");
  rb_define_module_function(Compose, "normal", psd_native_compose_normal, 3);
  rb_define_module_function(Compose, "darken", psd_native_compose_darken, 3);
  rb_define_module_function(Compose, "multiply", psd_native_compose_multiply, 3);
  rb_define_module_function(Compose, "color_burn", psd_native_compose_color_burn, 3);
  rb_define_module_function(Compose, "linear_burn", psd_native_compose_linear_burn, 3);
  rb_define_module_function(Compose, "lighten", psd_native_compose_lighten, 3);
  rb_define_module_function(Compose, "screen", psd_native_compose_screen, 3);
  rb_define_module_function(Compose, "color_dodge", psd_native_compose_color_dodge, 3);
  rb_define_module_function(Compose, "linear_dodge", psd_native_compose_linear_dodge, 3);
  rb_define_module_function(Compose, "overlay", psd_native_compose_overlay, 3);
  rb_define_module_function(Compose, "soft_light", psd_native_compose_soft_light, 3);
  rb_define_module_function(Compose, "hard_light", psd_native_compose_hard_light, 3);
  rb_define_module_function(Compose, "vivid_light", psd_native_compose_vivid_light, 3);
  rb_define_module_function(Compose, "linear_light", psd_native_compose_linear_light, 3);
  rb_define_module_function(Compose, "pin_light", psd_native_compose_pin_light, 3);
  rb_define_module_function(Compose, "hard_mix", psd_native_compose_hard_mix, 3);
  rb_define_module_function(Compose, "difference", psd_native_compose_difference, 3);
  rb_define_module_function(Compose, "exclusion", psd_native_compose_exclusion, 3);

  // Renderer
  VALUE Renderer = rb_define_module_under(PSDNative, "Renderer");

  // Clipping Mask
  VALUE ClippingMask = rb_define_module_under(Renderer, "ClippingMask");
  rb_define_method(ClippingMask, "apply!", psd_native_clipping_mask_apply_bang, 0);

  // Mask
  VALUE Mask = rb_define_module_under(Renderer, "Mask");
  rb_define_method(Mask, "apply!", psd_native_mask_apply_bang, 0);

  // Blender
  VALUE Blender = rb_define_module_under(Renderer, "Blender");
  rb_define_method(Blender, "compose!", psd_native_blender_compose_bang, 0);

  // Util
  VALUE Util = rb_define_module_under(PSDNative, "Util");
  rb_define_method(Util, "pad2", psd_native_util_pad2, 1);
  rb_define_method(Util, "pad4", psd_native_util_pad4, 1);
  rb_define_method(Util, "clamp", psd_native_util_clamp, 3);

  psd_logger("info", "PSD native mixins enabled!");
}

void psd_logger(char* level, char* message) {
  // TODO(CS)
  //rb_funcall(logger, rb_intern(level), 1, rb_str_new2(message));
}