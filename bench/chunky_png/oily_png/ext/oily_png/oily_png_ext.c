#include "oily_png_ext.h"

void Init_oily_png() {
  VALUE OilyPNG = rb_define_module("OilyPNG");

  VALUE OilyPNG_Canvas = rb_define_module_under(OilyPNG, "Resampling");
  rb_define_private_method(OilyPNG_Canvas, "steps_residues", oily_png_canvas_steps_residues, 2);
  rb_define_private_method(OilyPNG_Canvas, "steps", oily_png_canvas_steps, 2);
  rb_define_method(OilyPNG_Canvas, "resample_nearest_neighbor!", oily_png_canvas_resample_nearest_neighbor_bang, 2);
  rb_define_method(OilyPNG_Canvas, "resample_bilinear!", oily_png_canvas_resample_bilinear_bang, 2);

  // Setup decoding module
  VALUE OilyPNG_PNGDecoding = rb_define_module_under(OilyPNG, "PNGDecoding");
  rb_define_method(OilyPNG_PNGDecoding, "decode_png_image_pass", oily_png_decode_png_image_pass, 6);

  // Setup encoding module
  VALUE OilyPNG_PNGEncoding = rb_define_module_under(OilyPNG, "PNGEncoding");
  rb_define_method(OilyPNG_PNGEncoding, "encode_png_image_pass_to_stream", oily_png_encode_png_image_pass_to_stream, 4);

  // Setup Color module
  VALUE OilyPNG_Color = rb_define_module_under(OilyPNG, "Color");
  rb_define_method(OilyPNG_Color, "compose_quick", oily_png_color_compose_quick, 2);
  rb_define_method(OilyPNG_Color, "r", oily_png_color_r, 1);
  rb_define_method(OilyPNG_Color, "g", oily_png_color_g, 1);
  rb_define_method(OilyPNG_Color, "b", oily_png_color_b, 1);
  rb_define_method(OilyPNG_Color, "a", oily_png_color_a, 1);

  // Setup Operations module
  VALUE OilyPNG_Operations = rb_define_module_under(OilyPNG, "Operations");
  rb_define_method(OilyPNG_Operations, "compose!", oily_png_compose_bang, -1);
  rb_define_method(OilyPNG_Operations, "replace!", oily_png_replace_bang, -1);
}

char oily_png_samples_per_pixel(char color_mode) {
  switch (color_mode) {
    case OILY_PNG_COLOR_GRAYSCALE:       return 1;
    case OILY_PNG_COLOR_TRUECOLOR:       return 3;
    case OILY_PNG_COLOR_INDEXED:         return 1;
    case OILY_PNG_COLOR_GRAYSCALE_ALPHA: return 2;
    case OILY_PNG_COLOR_TRUECOLOR_ALPHA: return 4;
    default: rb_raise(rb_eRuntimeError, "Unsupported color mode: %d", color_mode);
  }
}

char oily_png_pixel_bitsize(char color_mode, char bit_depth) {
  return oily_png_samples_per_pixel(color_mode) * bit_depth;
}

char oily_png_pixel_bytesize(char color_mode, char bit_depth) {
  return (bit_depth < 8) ? 1 : (oily_png_pixel_bitsize(color_mode, bit_depth) + 7) >> 3;
}

long oily_png_scanline_bytesize(char color_mode, char bit_depth, long width) {
  return (8 + ((oily_png_pixel_bitsize(color_mode, bit_depth) * width) + 7)) >> 3;
}

long oily_png_pass_bytesize(char color_mode, char bit_depth, long width, long height) {
  return (width == 0 || height == 0) ? 0 : (oily_png_scanline_bytesize(color_mode, bit_depth, width)) * height;
}
