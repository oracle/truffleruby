#include "oily_png_ext.h"

void oily_png_check_size_constraints(long self_width, long self_height, long other_width, long other_height, long offset_x, long offset_y){
  // For now, these raise a standard runtime error. They should however raise custom exception classes (OutOfBounds)
  if(self_width  < other_width  + offset_x){
    rb_raise(rb_eRuntimeError, "Background image width is too small!");
  }
  if(self_height < other_height + offset_y){
    rb_raise(rb_eRuntimeError, "Background image height is too small!");
  }
}

VALUE oily_png_compose_bang(int argc, VALUE *argv, VALUE self) {
  // Corresponds to the other image(foreground) that we want to compose onto this one(background).
  VALUE other;
  
  // The offsets are optional arguments, so these may or may not be null pointers.
  // We'll prefix them with 'opt' to identify this.
  VALUE opt_offset_x;
  VALUE opt_offset_y;
  
  // Scan the passed in arguments, and populate the above-declared variables. Notice that '12'
  // specifies that oily_png_compose_bang takes in 1 required parameter, and 2 optional ones (the offsets)
  rb_scan_args(argc, argv, "12", &other,&opt_offset_x,&opt_offset_y);
  
  // Regardless of whether offsets were provided, we must specify a default value for them since they will
  // be used in calculating the position of the composed element.
  long offset_x = 0;
  long offset_y = 0;

  // If offsets were provided, then the opt_offset_* variables will not be null pointers. FIXNUM_P checks
  // whether they point to a fixnum object. If they do, then we can safely assign our offset_* variables to the values.
  if(FIXNUM_P(opt_offset_x)){
    offset_x = FIX2LONG(opt_offset_x);
  }
  if(FIXNUM_P(opt_offset_y)){
    offset_y = FIX2LONG(opt_offset_y);
  }
  
  // Get the dimension data for both foreground and background images.
  long self_width        = FIX2LONG(rb_funcall(self, rb_intern("width"), 0));
  long self_height       = FIX2LONG(rb_funcall(self, rb_intern("height"), 0));
  long other_width       = FIX2LONG(rb_funcall(other, rb_intern("width"), 0));
  long other_height      = FIX2LONG(rb_funcall(other, rb_intern("height"), 0));
  
  // Make sure that the 'other' image fits within the current image. If it doesn't, an exception is raised
  // and the operation should be aborted.
  oily_png_check_size_constraints( self_width, self_height, other_width, other_height, offset_x, offset_y );
  
  // Get the pixel data for both the foreground(other) and background(self) pixels. 
  VALUE* bg_pixels = RARRAY_PTR(rb_funcall(self, rb_intern("pixels"), 0));
  VALUE* fg_pixels = RARRAY_PTR(rb_funcall(other, rb_intern("pixels"), 0));

  long x = 0;
  long y = 0;
  long bg_index = 0; // corresponds to the current index in the bg_pixels array.
  for( y = 0; y < other_height; y++ ){
    for( x = 0; x < other_width; x++ ){
      // We need to find the value of bg_index twice, so we only calculate and store it once.
      bg_index = ( x + offset_x ) + ( y + offset_y ) * self_width;
      // Replace the background pixel with the composition of background + foreground
      bg_pixels[bg_index] = UINT2NUM( oily_png_compose_color( NUM2UINT( fg_pixels[x+ y * other_width] ), NUM2UINT( bg_pixels[bg_index] ) ) );
    }
  }
  return self;
}


VALUE oily_png_replace_bang(int argc, VALUE *argv, VALUE self) {
  // Corresponds to the other image(foreground) that we want to compose onto this one(background).
  VALUE other;
  
  // The offsets are optional arguments, so these may or may not be null pointers.
  // We'll prefix them with 'opt' to identify this.
  VALUE opt_offset_x;
  VALUE opt_offset_y;
  
  // Scan the passed in arguments, and populate the above-declared variables. Notice that '12'
  // specifies that oily_png_compose_bang takes in 1 required parameter, and 2 optional ones (the offsets)
  rb_scan_args(argc, argv, "12", &other,&opt_offset_x,&opt_offset_y);
  
  // Regardless of whether offsets were provided, we must specify a default value for them since they will
  // be used in calculating the position of the composed element.
  long offset_x = 0;
  long offset_y = 0;

  // If offsets were provided, then the opt_offset_* variables will not be null pointers. FIXNUM_P checks
  // whether they point to a fixnum object. If they do, then we can safely assign our offset_* variables to the values.
  if(FIXNUM_P(opt_offset_x)){
    offset_x = FIX2LONG(opt_offset_x);
  }
  if(FIXNUM_P(opt_offset_y)){
    offset_y = FIX2LONG(opt_offset_y);
  }
  
  // Get the dimension data for both foreground and background images.
  long self_width        = FIX2LONG(rb_funcall(self, rb_intern("width"), 0));
  long self_height       = FIX2LONG(rb_funcall(self, rb_intern("height"), 0));
  long other_width       = FIX2LONG(rb_funcall(other, rb_intern("width"), 0));
  long other_height      = FIX2LONG(rb_funcall(other, rb_intern("height"), 0));
  
  // Make sure that the 'other' image fits within the current image. If it doesn't, an exception is raised
  // and the operation should be aborted.
  oily_png_check_size_constraints( self_width, self_height, other_width, other_height, offset_x, offset_y );
  
  // Get the pixel data for both the foreground(other) and background(self) pixels. 
  VALUE* bg_pixels = RARRAY_PTR(rb_funcall(self, rb_intern("pixels"), 0));
  VALUE* fg_pixels = RARRAY_PTR(rb_funcall(other, rb_intern("pixels"), 0));

  long x = 0;
  long y = 0;
  long bg_index = 0; // corresponds to the current index in the bg_pixels array.
  for( y = 0; y < other_height; y++ ){
    for( x = 0; x < other_width; x++ ){
      // We need to find the value of bg_index twice, so we only calculate and store it once.
      bg_index = ( x + offset_x ) + ( y + offset_y ) * self_width;
      // Replace the background pixel with the composition of background + foreground
      bg_pixels[bg_index] = fg_pixels[x+ y * other_width];
    }
  }
  return self;
}
