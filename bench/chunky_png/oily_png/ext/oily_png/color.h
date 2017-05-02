#ifndef OILY_PNG_COLOR_H
#define OILY_PNG_COLOR_H

#define R_BYTE(pixel)  ((BYTE) (((pixel) & (PIXEL) 0xff000000) >> 24))
#define G_BYTE(pixel)  ((BYTE) (((pixel) & (PIXEL) 0x00ff0000) >> 16))
#define B_BYTE(pixel)  ((BYTE) (((pixel) & (PIXEL) 0x0000ff00) >> 8))
#define A_BYTE(pixel)  ((BYTE) (((pixel) & (PIXEL) 0x000000ff)))

#define BUILD_PIXEL(r, g, b, a)  (((PIXEL) (r) << 24) + ((PIXEL) (g) << 16) + ((PIXEL) (b) << 8) + (PIXEL) (a))
#define INT8_MULTIPLY(a, b)      (((((a) * (b) + 0x80) >> 8) + ((a) * (b) + 0x80)) >> 8)

/*
  Ruby replacement method for color composition using alpha transparency.

  This method should replace ChunkyPNG::Color.compose_quick
*/
VALUE oily_png_color_compose_quick(VALUE self, VALUE fg_color, VALUE bg_color);

/* Color composition using alpha transparency. */
PIXEL oily_png_compose_color(PIXEL fg, PIXEL bg);
PIXEL oily_png_color_interpolate_quick(PIXEL fg, PIXEL bg, int alpha);

/* Accessors */
VALUE oily_png_color_r(VALUE self, VALUE pixel);
VALUE oily_png_color_g(VALUE self, VALUE pixel);
VALUE oily_png_color_b(VALUE self, VALUE pixel);
VALUE oily_png_color_a(VALUE self, VALUE pixel);

#endif
