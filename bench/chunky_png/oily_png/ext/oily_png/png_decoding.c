#include "oily_png_ext.h"


/////////////////////////////////////////////////////////////////////
// UNFILTERING SCANLINES
/////////////////////////////////////////////////////////////////////


// Decodes a SUB filtered scanline at the given position in the byte array
void oily_png_decode_filter_sub(BYTE* bytes, long pos, long line_length, char pixel_size) {
  long i;
  for (i = 1 + pixel_size; i < line_length; i++) {
    UNFILTER_BYTE(bytes[pos + i], bytes[pos + i - pixel_size]);
  }
}

// Decodes an UP filtered scanline at the given position in the byte array
void oily_png_decode_filter_up(BYTE* bytes, long pos, long line_size, char pixel_size) {
  UNUSED_PARAMETER(pixel_size);
  long i;
  if (pos >= line_size) { // The first line is not filtered because there is no privous line
    for (i = 1; i < line_size; i++) {
      UNFILTER_BYTE(bytes[pos + i], bytes[pos + i - line_size]);
    }
  }
}

// Decodes an AVERAGE filtered scanline at the given position in the byte array
void oily_png_decode_filter_average(BYTE* bytes, long pos, long line_size, char pixel_size) {
  long i;
  BYTE a, b;
  for (i = 1; i < line_size; i++) {
    a = (i > pixel_size)     ? bytes[pos + i - pixel_size]  : 0;
    b = (pos >= line_size) ? bytes[pos + i - line_size] : 0;
    UNFILTER_BYTE(bytes[pos + i], (a + b) >> 1);
  }
}

// Decodes a PAETH filtered scanline at the given position in the byte array
void oily_png_decode_filter_paeth(BYTE* bytes, long pos, long line_size, char pixel_size) {
  BYTE a, b, c, pr;
  long i, p, pa, pb, pc;
  for (i = 1; i < line_size; i++) {
    a = (i > pixel_size) ? bytes[pos + i - pixel_size]  : 0;
    b = (pos >= line_size) ? bytes[pos + i - line_size] : 0;
    c = (pos >= line_size && i > pixel_size) ? bytes[pos + i - line_size - pixel_size] : 0;
    p = a + b - c;
    pa = (p > a) ? p - a : a - p;
    pb = (p > b) ? p - b : b - p;
    pc = (p > c) ? p - c : c - p;
    pr = (pa <= pb) ? (pa <= pc ? a : c) : (pb <= pc ? b : c);
    UNFILTER_BYTE(bytes[pos + i], pr);
  }
}

/////////////////////////////////////////////////////////////////////
// BIT HANDLING
/////////////////////////////////////////////////////////////////////


BYTE oily_png_extract_1bit_element(BYTE* bytes, long start, long x) {
  BYTE byte = bytes[start + 1 + (x >> 3)];
  char bitshift = 7 - (x & (BYTE) 0x07);
  return (byte & (0x01 << bitshift)) >> bitshift;
}


BYTE oily_png_extract_2bit_element(BYTE* bytes, long start, long x) {
  BYTE byte = bytes[start + 1 + (x >> 2)];
  char bitshift = (6 - ((x & (BYTE) 0x03) << 1));
  return (byte & (0x03 << bitshift)) >> bitshift;
}

BYTE oily_png_extract_4bit_element(BYTE* bytes, long start, long x) {
  return ((x & 0x01) == 0) ? ((bytes[(start) + 1 + ((x) >> 1)] & (BYTE) 0xf0) >> 4) : (bytes[(start) + 1 + ((x) >> 1)] & (BYTE) 0x0f);
}

BYTE oily_png_resample_1bit_element(BYTE* bytes, long start, long x) {
  BYTE value = oily_png_extract_1bit_element(bytes, start, x);
  return (value == 0) ? 0x00 : 0xff;
}

BYTE oily_png_resample_2bit_element(BYTE* bytes, long start, long x) {
  return oily_png_extract_2bit_element(bytes, start, x) * 85;
}

BYTE oily_png_resample_4bit_element(BYTE* bytes, long start, long x) {
  return oily_png_extract_4bit_element(bytes, start, x) * 17;
}

/////////////////////////////////////////////////////////////////////
// PIXEL DECODING SCANLINES
/////////////////////////////////////////////////////////////////////


void oily_png_decode_scanline_grayscale_1bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    oily_png_resample_1bit_element(bytes, start, x), 
                    oily_png_resample_1bit_element(bytes, start, x), 
                    oily_png_resample_1bit_element(bytes, start, x), 
                    0xff);
  }
}

void oily_png_decode_scanline_grayscale_2bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    oily_png_resample_2bit_element(bytes, start, x), 
                    oily_png_resample_2bit_element(bytes, start, x), 
                    oily_png_resample_2bit_element(bytes, start, x), 
                    0xff);
  }
}

void oily_png_decode_scanline_grayscale_4bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    oily_png_resample_4bit_element(bytes, start, x), 
                    oily_png_resample_4bit_element(bytes, start, x), 
                    oily_png_resample_4bit_element(bytes, start, x), 
                    0xff);
  }
}

void oily_png_decode_scanline_grayscale_8bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + x], 
                    bytes[start + 1 + x], 
                    bytes[start + 1 + x], 
                    0xff);
  }
}

void oily_png_decode_scanline_grayscale_16bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + (x * 2)], 
                    bytes[start + 1 + (x * 2)], 
                    bytes[start + 1 + (x * 2)], 
                    0xff);
  }
}

void oily_png_decode_scanline_grayscale_alpha_8bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + (x * 2) + 0], 
                    bytes[start + 1 + (x * 2) + 0], 
                    bytes[start + 1 + (x * 2) + 0], 
                    bytes[start + 1 + (x * 2) + 1]);
  }
}

void oily_png_decode_scanline_grayscale_alpha_16bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + (x * 4) + 0], 
                    bytes[start + 1 + (x * 4) + 0], 
                    bytes[start + 1 + (x * 4) + 0], 
                    bytes[start + 1 + (x * 4) + 2]);
  }
}

void oily_png_decode_scanline_indexed_1bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_PALLETE(pixels, decoding_palette, oily_png_extract_1bit_element(bytes, start, x));
  }
}

void oily_png_decode_scanline_indexed_2bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_PALLETE(pixels, decoding_palette, oily_png_extract_2bit_element(bytes, start, x));
  }
}

void oily_png_decode_scanline_indexed_4bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_PALLETE(pixels, decoding_palette, oily_png_extract_4bit_element(bytes, start, x));
  }
}

void oily_png_decode_scanline_indexed_8bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_PALLETE(pixels, decoding_palette, bytes[start + 1 + x]);
  }
}

void oily_png_decode_scanline_truecolor_8bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + (x * 3) + 0], 
                    bytes[start + 1 + (x * 3) + 1], 
                    bytes[start + 1 + (x * 3) + 2], 
                    0xff);
  }
}

void oily_png_decode_scanline_truecolor_16bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + (x * 6) + 0], 
                    bytes[start + 1 + (x * 6) + 2], 
                    bytes[start + 1 + (x * 6) + 4], 
                    0xff);
  }
}

void oily_png_decode_scanline_truecolor_alpha_8bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,
                    bytes[start + 1 + (x * 4) + 0], 
                    bytes[start + 1 + (x * 4) + 1], 
                    bytes[start + 1 + (x * 4) + 2], 
                    bytes[start + 1 + (x * 4) + 3]);    
  }
}

void oily_png_decode_scanline_truecolor_alpha_16bit(VALUE pixels, BYTE* bytes, long start, long width, VALUE decoding_palette) {
  UNUSED_PARAMETER(decoding_palette);
  long x;
  for (x = 0; x < width; x++) {
    ADD_PIXEL_FROM_RGBA(pixels,    
                    bytes[start + 1 + (x * 8) + 0], 
                    bytes[start + 1 + (x * 8) + 2], 
                    bytes[start + 1 + (x * 8) + 4], 
                    bytes[start + 1 + (x * 8) + 6]);
  }
}

scanline_decoder_func oily_png_decode_scanline_func(int color_mode, int bit_depth) {
  switch (color_mode) {
    case OILY_PNG_COLOR_GRAYSCALE:
      switch (bit_depth) {
        case  1: return &oily_png_decode_scanline_grayscale_1bit;
        case  2: return &oily_png_decode_scanline_grayscale_2bit;
        case  4: return &oily_png_decode_scanline_grayscale_4bit;
        case  8: return &oily_png_decode_scanline_grayscale_8bit;
        case 16: return &oily_png_decode_scanline_grayscale_16bit;
        default: return NULL;
      }

    case OILY_PNG_COLOR_TRUECOLOR:
      switch (bit_depth) {
        case  8: return &oily_png_decode_scanline_truecolor_8bit;
        case 16: return &oily_png_decode_scanline_truecolor_16bit;
        default: return NULL;
      }
      
    case OILY_PNG_COLOR_INDEXED:
      switch (bit_depth) {
        case 1: return &oily_png_decode_scanline_indexed_1bit;
        case 2: return &oily_png_decode_scanline_indexed_2bit;
        case 4: return &oily_png_decode_scanline_indexed_4bit;
        case 8: return &oily_png_decode_scanline_indexed_8bit;
        default: return NULL;
      }
      
    case OILY_PNG_COLOR_GRAYSCALE_ALPHA:
      switch (bit_depth) {
        case  8: return &oily_png_decode_scanline_grayscale_alpha_8bit;
        case 16: return &oily_png_decode_scanline_grayscale_alpha_16bit;
        default: return NULL;
      }

    case OILY_PNG_COLOR_TRUECOLOR_ALPHA:
      switch (bit_depth) {
        case  8: return &oily_png_decode_scanline_truecolor_alpha_8bit;
        case 16: return &oily_png_decode_scanline_truecolor_alpha_16bit;
        default: return NULL;
      }
    
    default: return NULL;
  }
}

/////////////////////////////////////////////////////////////////////
// DECODING AN IMAGE PASS
/////////////////////////////////////////////////////////////////////

VALUE oily_png_decode_palette(VALUE self) {
  VALUE palette_instance = rb_funcall(self, rb_intern("decoding_palette"), 0);
  if (palette_instance != Qnil) {
    VALUE decoding_map = rb_iv_get(palette_instance, "@decoding_map");
    if (rb_funcall(decoding_map, rb_intern("kind_of?"), 1, rb_cArray) == Qtrue) {  
      return decoding_map;
    }
  }
  rb_raise(rb_eRuntimeError, "Could not retrieve a decoding palette for this image!");  
}


VALUE oily_png_decode_png_image_pass(VALUE self, VALUE stream, VALUE width, VALUE height, VALUE color_mode, VALUE depth, VALUE start_pos) {
  
  VALUE pixels = rb_ary_new();
  
  if ((FIX2LONG(height) > 0) && (FIX2LONG(width) > 0)) {

    char pixel_size = oily_png_pixel_bytesize(FIX2INT(color_mode), FIX2INT(depth));
    long line_size  = oily_png_scanline_bytesize(FIX2INT(color_mode), FIX2INT(depth), FIX2LONG(width));
    long pass_size  = oily_png_pass_bytesize(FIX2INT(color_mode), FIX2INT(depth), FIX2LONG(width), FIX2LONG(height));
      
    // Make sure that the stream is large enough to contain our pass.
    if (RSTRING_LEN(stream) < pass_size + FIX2LONG(start_pos)) {
      rb_raise(rb_eRuntimeError, "The length of the stream is too short to contain the image!");
    }

    // Copy the bytes for this pass from the stream to a separate location
    // so we can work on this byte array directly.
    BYTE* bytes = ALLOC_N(BYTE, pass_size);
    memcpy(bytes, RSTRING_PTR(stream) + FIX2LONG(start_pos), pass_size);

    // Get the decoding palette for indexed images.
    VALUE decoding_palette = Qnil;
    if (FIX2INT(color_mode) == OILY_PNG_COLOR_INDEXED) {
      decoding_palette = oily_png_decode_palette(self);
    }

    // Select the scanline decoder function for this color mode and bit depth.
    scanline_decoder_func scanline_decoder = oily_png_decode_scanline_func(FIX2INT(color_mode), FIX2INT(depth));
    if (scanline_decoder == NULL) {
      rb_raise(rb_eRuntimeError, "No decoder for color mode %d and bit depth %d", FIX2INT(color_mode), FIX2INT(depth));
    }
  
    long y, line_start;
    for (y = 0; y < FIX2LONG(height); y++) {
      line_start = y * line_size;
    
      // Apply filering to the line
      switch (bytes[line_start]) {
        case OILY_PNG_FILTER_NONE:    break;
        case OILY_PNG_FILTER_SUB:     oily_png_decode_filter_sub(     bytes, line_start, line_size, pixel_size); break;
        case OILY_PNG_FILTER_UP:      oily_png_decode_filter_up(      bytes, line_start, line_size, pixel_size); break;
        case OILY_PNG_FILTER_AVERAGE: oily_png_decode_filter_average( bytes, line_start, line_size, pixel_size); break;
        case OILY_PNG_FILTER_PAETH:   oily_png_decode_filter_paeth(   bytes, line_start, line_size, pixel_size); break;
        default: rb_raise(rb_eRuntimeError, "Filter type not supported: %d", bytes[line_start]);
      }
    
      // Set the filter byte to 0 because the bytearray is now unfiltered.
      bytes[line_start] = OILY_PNG_FILTER_NONE;
      scanline_decoder(pixels, bytes, line_start, FIX2LONG(width), decoding_palette);
    }
    
    xfree(bytes);
  }

  // Now, return a new ChunkyPNG::Canvas instance with the decoded pixels.
  return rb_funcall(self, rb_intern("new"), 3, width, height, pixels);
}
