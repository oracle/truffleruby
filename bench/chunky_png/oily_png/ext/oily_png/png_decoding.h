#ifndef OILY_PNG_PNG_DECODING_H
#define OILY_PNG_PNG_DECODING_H

#define UNFILTER_BYTE(byte, adjustment)  byte = (BYTE) (((byte) + (adjustment)) & 0x000000ff)

#define ADD_PIXEL_FROM_PALLETE(pixels, decoding_palette, palette_entry) \
    if (RARRAY_LEN(decoding_palette) > (palette_entry)) { \
      rb_ary_push(pixels, rb_ary_entry(decoding_palette, (palette_entry))); \
    } else { \
      rb_raise(rb_eRuntimeError, "The decoding palette does not have %d entries!", (palette_entry)); \
    }
    
#define ADD_PIXEL_FROM_RGBA(pixels, r, g, b, a) rb_ary_push(pixels, UINT2NUM(BUILD_PIXEL(r,g,b,a)));


typedef void(*scanline_decoder_func)(VALUE, BYTE*, long, long, VALUE);

/*
  Decodes an image pass from the given byte stream at the given position.
  A normal PNG will only have one pass that consumes the entire stream, while an
  interlaced image requires 7 passes which are loaded from different starting positions.
  
  This function shouild replace ChunkyPNG::Canvas::PNGDecoding.decode_png_image_pass
*/
VALUE oily_png_decode_png_image_pass(VALUE self, VALUE stream, VALUE width, VALUE height, VALUE color_mode, VALUE depth, VALUE start_pos);

#endif
