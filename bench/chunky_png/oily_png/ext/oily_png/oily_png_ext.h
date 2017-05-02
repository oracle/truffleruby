#ifndef OILY_PNG_OILY_PNG_EXT
#define OILY_PNG_OILY_PNG_EXT

#include "ruby.h"

#define RSTRING_NOT_MODIFIED

// PNG color mode constants
#define OILY_PNG_COLOR_GRAYSCALE        0
#define OILY_PNG_COLOR_TRUECOLOR        2
#define OILY_PNG_COLOR_INDEXED          3
#define OILY_PNG_COLOR_GRAYSCALE_ALPHA  4
#define OILY_PNG_COLOR_TRUECOLOR_ALPHA  6

// PNG filter constants
#define OILY_PNG_FILTER_NONE    0
#define OILY_PNG_FILTER_SUB     1
#define OILY_PNG_FILTER_UP      2
#define OILY_PNG_FILTER_AVERAGE 3
#define OILY_PNG_FILTER_PAETH   4

// Macro to surpress warnings about unused parameters.
#define UNUSED_PARAMETER(param) (void) param

// Type definitions
typedef uint32_t PIXEL; // Pixels use 32 bits unsigned integers
typedef unsigned char BYTE; // Bytes use 8 bits unsigned integers


#include "png_decoding.h"
#include "png_encoding.h"
#include "color.h"
#include "operations.h"
#include "resampling.h"

/*
  Initialize the extension by creating the OilyPNG modules, and registering
  the encoding and decoding replacement functions.
  
  Note, this does not actually replace functionality in ChunkyPNG; you will need
  to extend the ChunkyPNG::Canvas class with the OilyPNG::PNGDecoding module to 
  speed up decoding, and include OilyPNG::PNGEncoding into the same class to speed
  up encoding. This is done in lib/oily_png.rb
*/
void Init_oily_png();

/*
  Returns the number of samples per pixel for a given color mode
*/
char oily_png_samples_per_pixel(char color_mode);

/*
  Returns the number of bits per pixel for a given color mode and bit depth.
*/
char oily_png_pixel_bitsize(char color_mode, char bit_depth);

/*
  Returns the number of bytes per pixel for a given color mode and bit depth.
*/
char oily_png_pixel_bytesize(char color_mode, char bit_depth);

/*
  Returns the number of bytes per scanline for a given width, color mode and bit depth.
*/
long oily_png_scanline_bytesize(char color_mode, char bit_depth, long width);

/*
  Returns the number of bytes in an image pass with the given properties.
*/
long oily_png_pass_bytesize(char color_mode, char bit_depth, long width, long height);

#endif
