#include "oily_png_ext.h"

///// Scanline filtering functions //////////////////////////////////////////

void oily_png_encode_filter_sub(BYTE* bytes, long pos, long line_size, char pixel_size) {
  long x;
  for (x = line_size - 1; x > pixel_size; x--) {
    FILTER_BYTE(bytes[pos + x], bytes[pos + x - pixel_size]);
  }
}

void oily_png_encode_filter_up(BYTE* bytes, long pos, long line_size, char pixel_size) {
  UNUSED_PARAMETER(pixel_size);
  
  long x;
  if (pos >= line_size) {
    for (x = line_size - 1; x > 0; x--) {
      FILTER_BYTE(bytes[pos + x], bytes[pos + x - line_size]);
    }
  }
}

void oily_png_encode_filter_average(BYTE* bytes, long pos, long line_size, char pixel_size) {
  long x; BYTE a, b;
  for (x = line_size - 1; x > 0; x--) {
    a = (x > pixel_size)   ? bytes[pos + x - pixel_size] : 0;
    b = (pos >= line_size) ? bytes[pos + x - line_size]  : 0;
    FILTER_BYTE(bytes[pos + x], (a + b) >> 1);
  }
}

void oily_png_encode_filter_paeth(BYTE* bytes, long pos, long line_size, char pixel_size) {
  long x; int p, pa, pb, pc; BYTE a, b, c, pr;
  for (x = line_size - 1; x > 0; x--) {
    a = (x > pixel_size) ? bytes[pos + x - pixel_size] : 0;
    b = (pos >= line_size) ? bytes[pos + x - line_size] : 0;
    c = (pos >= line_size && x > pixel_size) ? bytes[pos + x - line_size - pixel_size] : 0;
    p  = a + b - c;
    pa = abs(p - a);
    pb = abs(p - b);
    pc = abs(p - c);
    pr = (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
    FILTER_BYTE(bytes[pos + x], pr);
  }
}

///// Scanline encoding functions //////////////////////////////////////////

// Assume R == G == B. ChunkyPNG uses the B byte fot performance reasons. 
// We'll uses the same to remain compatible with ChunkyPNG.
void oily_png_encode_scanline_grayscale_1bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; BYTE p1, p2, p3, p4, p5, p6, p7, p8;
  for (x = 0; x < width; x += 8) {
    p1 = (x + 0 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 0))) >> 7);
    p2 = (x + 1 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 1))) >> 7);
    p3 = (x + 2 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 2))) >> 7);
    p4 = (x + 3 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 3))) >> 7);
    p5 = (x + 4 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 4))) >> 7);
    p6 = (x + 5 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 5))) >> 7);
    p7 = (x + 6 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 6))) >> 7);
    p8 = (x + 7 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 7))) >> 7);
    bytes[x >> 3] = (BYTE) ((p1 << 7) | (p2 << 6) | (p3 << 5) | (p4 << 4) | (p5 << 3) | (p6 << 2) | (p7 << 1) | (p8));
  }
}


// Assume R == G == B. ChunkyPNG uses the B byte fot performance reasons. 
// We'll uses the same to remain compatible with ChunkyPNG.
void oily_png_encode_scanline_grayscale_2bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; BYTE p1, p2, p3, p4;
  for (x = 0; x < width; x += 4) {
    p1 = (x + 0 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 0))) >> 6);
    p2 = (x + 1 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 1))) >> 6);
    p3 = (x + 2 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 2))) >> 6);
    p4 = (x + 3 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 3))) >> 6);
    bytes[x >> 2] = (BYTE) ((p1 << 6) | (p2 << 4) | (p3 << 2) | (p4));
  }
}

// Assume R == G == B. ChunkyPNG uses the B byte fot performance reasons. 
// We'll uses the same to remain compatible with ChunkyPNG.
void oily_png_encode_scanline_grayscale_4bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; BYTE p1, p2;
  for (x = 0; x < width; x += 2) {
    p1 = (x + 0 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 0))) >> 4);
    p2 = (x + 1 >= width) ? 0 : (B_BYTE(NUM2UINT(rb_ary_entry(pixels, y * width + x + 1))) >> 4);
    bytes[x >> 1] = (BYTE) ((p1 << 4) | (p2));
  }
}

// Assume R == G == B. ChunkyPNG uses the B byte fot performance reasons. 
// We'll uses the same to remain compatible with ChunkyPNG.
void oily_png_encode_scanline_grayscale_8bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; PIXEL pixel;
  for (x = 0; x < width; x++) {
    pixel = NUM2UINT(rb_ary_entry(pixels, y * width + x));
    bytes[x] = B_BYTE(pixel);
  }
}

// Assume R == G == B. ChunkyPNG uses the B byte fot performance reasons. 
// We'll uses the same to remain compatible with ChunkyPNG.
void oily_png_encode_scanline_grayscale_alpha_8bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; PIXEL pixel;
  for (x = 0; x < width; x++) {
    pixel = NUM2UINT(rb_ary_entry(pixels, y * width + x));
    bytes[x * 2 + 0] = B_BYTE(pixel);
    bytes[x * 2 + 1] = A_BYTE(pixel);
  }
}

void oily_png_encode_scanline_indexed_8bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  long x;
  for (x = 0; x < width; x++) {
    bytes[x] = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x);
  }
}

void oily_png_encode_scanline_indexed_4bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  long x; BYTE p1, p2;
  for (x = 0; x < width; x += 2) {
    p1 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 0);
    p2 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 1);
    bytes[x >> 1] = (BYTE) ((p1 << 4) | (p2));
  }
}

void oily_png_encode_scanline_indexed_2bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  long x; BYTE p1, p2, p3, p4;
  for (x = 0; x < width; x += 4) {
    p1 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 0);
    p2 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 1);
    p3 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 2);
    p4 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 3);
    bytes[x >> 2] = (BYTE) ((p1 << 6) | (p2 << 4) | (p3 << 2) | (p4));
  }
}

void oily_png_encode_scanline_indexed_1bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  long x; BYTE p1, p2, p3, p4, p5, p6, p7, p8;
  for (x = 0; x < width; x += 8) {
    p1 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 0);
    p2 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 1);
    p3 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 2);
    p4 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 3);
    p5 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 4);
    p6 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 5);
    p7 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 6);
    p8 = ENCODING_PALETTE_INDEX(encoding_palette, pixels, width, y, x + 7);
    bytes[x >> 3] = (BYTE) ((p1 << 7) | (p2 << 6) | (p3 << 5) | (p4 << 4) | (p5 << 3) | (p6 << 2) | (p7 << 1) | (p8));
  }
}
 
void oily_png_encode_scanline_truecolor_8bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; PIXEL pixel;
  for (x = 0; x < width; x++) {
    pixel = NUM2UINT(rb_ary_entry(pixels, y * width + x));
    bytes[x * 3 + 0] = R_BYTE(pixel);
    bytes[x * 3 + 1] = G_BYTE(pixel);
    bytes[x * 3 + 2] = B_BYTE(pixel);
  }
}

void oily_png_encode_scanline_truecolor_alpha_8bit(BYTE* bytes, VALUE pixels, long y, long width, VALUE encoding_palette) {
  UNUSED_PARAMETER(encoding_palette);
  long x; PIXEL pixel;
  for (x = 0; x < width; x++) {
    pixel = NUM2UINT(rb_ary_entry(pixels, y * width + x));
    bytes[x * 4 + 0] = R_BYTE(pixel);
    bytes[x * 4 + 1] = G_BYTE(pixel);
    bytes[x * 4 + 2] = B_BYTE(pixel);
    bytes[x * 4 + 3] = A_BYTE(pixel);
  }
}


scanline_encoder_func oily_png_encode_scanline_func(char color_mode, char bit_depth) {
  switch (color_mode) {

    case OILY_PNG_COLOR_GRAYSCALE:
      switch (bit_depth) {
        case 8:  return &oily_png_encode_scanline_grayscale_8bit;
        case 4:  return &oily_png_encode_scanline_grayscale_4bit;
        case 2:  return &oily_png_encode_scanline_grayscale_2bit;
        case 1:  return &oily_png_encode_scanline_grayscale_1bit;
        default: return NULL;
      }
      
    case OILY_PNG_COLOR_GRAYSCALE_ALPHA:
      switch (bit_depth) {
        case 8:  return &oily_png_encode_scanline_grayscale_alpha_8bit;
        default: return NULL;
      }
    
    case OILY_PNG_COLOR_INDEXED:
      switch (bit_depth) {
        case 8:  return &oily_png_encode_scanline_indexed_8bit;
        case 4:  return &oily_png_encode_scanline_indexed_4bit;
        case 2:  return &oily_png_encode_scanline_indexed_2bit;
        case 1:  return &oily_png_encode_scanline_indexed_1bit;
        default: return NULL;
      }
      
    case OILY_PNG_COLOR_TRUECOLOR:
      switch (bit_depth) {
        case 8:  return &oily_png_encode_scanline_truecolor_8bit;
        default: return NULL;
      }
      
    case OILY_PNG_COLOR_TRUECOLOR_ALPHA:
      switch (bit_depth) {
        case 8:  return &oily_png_encode_scanline_truecolor_alpha_8bit;
        default: return NULL;
      }

    default: return NULL;
  }
}

/////////////////////////////////////////////////////////////////////
// ENCODING AN IMAGE PASS
/////////////////////////////////////////////////////////////////////

VALUE oily_png_encode_palette(VALUE self) {
  VALUE palette_instance = rb_funcall(self, rb_intern("encoding_palette"), 0);
  if (palette_instance != Qnil) {
    VALUE encoding_map = rb_iv_get(palette_instance, "@encoding_map");
    if (rb_funcall(encoding_map, rb_intern("kind_of?"), 1, rb_cHash) == Qtrue) {
      return encoding_map;
    }
  }
  rb_raise(rb_eRuntimeError, "Could not retrieve a decoding palette for this image!");
}

VALUE oily_png_encode_png_image_pass_to_stream(VALUE self, VALUE stream, VALUE color_mode, VALUE bit_depth, VALUE filtering) {
  
  UNUSED_PARAMETER(bit_depth);
  
  // Get the data
  char depth      = (char) FIX2INT(bit_depth);
  long width      = FIX2LONG(rb_funcall(self, rb_intern("width"), 0));
  long height     = FIX2LONG(rb_funcall(self, rb_intern("height"), 0));
  VALUE pixels    = rb_funcall(self, rb_intern("pixels"), 0);
  
  if (RARRAY_LEN(pixels) != width * height) {
    rb_raise(rb_eRuntimeError, "The number of pixels does not match the canvas dimensions.");
  }

  // Get the encoding palette if we're encoding to an indexed bytestream.
  VALUE encoding_palette = Qnil;
  if (FIX2INT(color_mode) == OILY_PNG_COLOR_INDEXED) {
    encoding_palette = oily_png_encode_palette(self);
  }
  
  char pixel_size = oily_png_pixel_bytesize(FIX2INT(color_mode), depth);
  long line_size  = oily_png_scanline_bytesize(FIX2INT(color_mode), depth, width);
  long pass_size  = oily_png_pass_bytesize(FIX2INT(color_mode), depth, width, height);

  // Allocate memory for the byte array.
  BYTE* bytes = ALLOC_N(BYTE, pass_size);
  
  // Get the scanline encoder function.
  scanline_encoder_func scanline_encoder = oily_png_encode_scanline_func(FIX2INT(color_mode), depth);
  if (scanline_encoder == NULL) {
    rb_raise(rb_eRuntimeError, "No encoder for color mode %d and bit depth %d", FIX2INT(color_mode), depth);
  }

  long y, pos;
  for (y = height - 1; y >= 0; y--) {
    pos = line_size * y;
    bytes[pos] = (BYTE) FIX2INT(filtering);
    scanline_encoder(bytes + pos + 1, pixels, y, width, encoding_palette);
  }
  
  if (FIX2INT(filtering) != OILY_PNG_FILTER_NONE) {

    // Get the scanline filter function
    void (*scanline_filter)(BYTE*, long, long, char) = NULL;
    switch (FIX2INT(filtering)) {
      case OILY_PNG_FILTER_SUB:     scanline_filter = &oily_png_encode_filter_sub; break;
      case OILY_PNG_FILTER_UP:      scanline_filter = &oily_png_encode_filter_up; break;
      case OILY_PNG_FILTER_AVERAGE: scanline_filter = &oily_png_encode_filter_average; break;
      case OILY_PNG_FILTER_PAETH:   scanline_filter = &oily_png_encode_filter_paeth; break;
      default: rb_raise(rb_eRuntimeError, "Unsupported filter type: %d", FIX2INT(filtering));
    }

    for (y = height - 1; y >= 0; y--) {
      scanline_filter(bytes, line_size * y, line_size, pixel_size);
    }
  }
  
  // Append to encoded image pass to the output stream.
  rb_str_cat(stream, (char*) bytes, pass_size);
  xfree(bytes);
  return Qnil;
}
