#include "oily_png_ext.h"

PIXEL oily_png_compose_color(PIXEL fg, PIXEL bg) {
  BYTE a_com, new_r, new_g, new_b, new_a;

  // Check for simple cases first
  if ((A_BYTE(fg) == 0xff) || (A_BYTE(bg) == 0x00)) return fg;
  if (A_BYTE(fg) == 0x00) return bg;

  // Calculate the new values using fast 8-bit multiplication
  a_com = INT8_MULTIPLY(0xff - A_BYTE(fg), A_BYTE(bg));
  new_r = INT8_MULTIPLY(A_BYTE(fg), R_BYTE(fg)) + INT8_MULTIPLY(a_com, R_BYTE(bg));
  new_g = INT8_MULTIPLY(A_BYTE(fg), G_BYTE(fg)) + INT8_MULTIPLY(a_com, G_BYTE(bg));
  new_b = INT8_MULTIPLY(A_BYTE(fg), B_BYTE(fg)) + INT8_MULTIPLY(a_com, B_BYTE(bg));
  new_a = A_BYTE(fg) + a_com;

  return BUILD_PIXEL(new_r, new_g, new_b, new_a);
}

PIXEL oily_png_color_interpolate_quick(PIXEL fg, PIXEL bg, int alpha) {
  BYTE a_com, new_r, new_g, new_b, new_a;

  if (alpha >= 255) return fg;
  if (alpha <= 0) return bg;

  a_com = 255 - alpha;
  new_r = INT8_MULTIPLY(alpha, R_BYTE(fg)) + INT8_MULTIPLY(a_com, R_BYTE(bg));
  new_g = INT8_MULTIPLY(alpha, G_BYTE(fg)) + INT8_MULTIPLY(a_com, G_BYTE(bg));
  new_b = INT8_MULTIPLY(alpha, B_BYTE(fg)) + INT8_MULTIPLY(a_com, B_BYTE(bg));
  new_a = INT8_MULTIPLY(alpha, A_BYTE(fg)) + INT8_MULTIPLY(a_com, A_BYTE(bg));

  return BUILD_PIXEL(new_r, new_g, new_b, new_a);
}

VALUE oily_png_color_compose_quick(VALUE self, VALUE fg_color, VALUE bg_color) {
  UNUSED_PARAMETER(self);
  return UINT2NUM(oily_png_compose_color(NUM2UINT(fg_color), NUM2UINT(bg_color)));
}

VALUE oily_png_color_r(VALUE self, VALUE value) {
  UNUSED_PARAMETER(self);
  return INT2FIX(R_BYTE(NUM2UINT(value)));
}

VALUE oily_png_color_g(VALUE self, VALUE value) {
  UNUSED_PARAMETER(self);
  return INT2FIX(G_BYTE(NUM2UINT(value)));
}

VALUE oily_png_color_b(VALUE self, VALUE value) {
  UNUSED_PARAMETER(self);
  return INT2FIX(B_BYTE(NUM2UINT(value)));
}

VALUE oily_png_color_a(VALUE self, VALUE value) {
  UNUSED_PARAMETER(self);
  return INT2FIX(A_BYTE(NUM2UINT(value)));
}
