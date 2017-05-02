#ifndef OILY_PNG_OPERATIONS_H
#define OILY_PNG_OPERATIONS_H

/* 
  Checks whether an image 'other' can fits into 'self'. Takes offset into account.
  An exception is raised if the check fails.
  
  Instead of taking in an object 'self' and an object 'other' and then calculating their parameters,
  we ask for the respective height and width directly. This is because these variables will need to be calculated 
  by 'rb_intern()' within the method calling oily_png_check_size_constraints (ex: oily_png_compose), so there's no 
  use in calculating them twice.
  
*/
void oily_png_check_size_constraints(long self_width, long self_height, long other_width, long other_height, long offset_x, long offset_y);

/* 
  C replacement method for composing another image onto this image using alpha blending.
  
  TODO: Implement functionality with ChunkyPNG and OilyPNG so that an image can be composited onto another
  regardless of its size: however, only the intersecting elements of both images should be mixed.
  
  This method should replace ChunkyPNG::Canvas.compose!
*/
VALUE oily_png_compose_bang(int argc, VALUE *argv, VALUE c);

/* 
  C replacement method for composing another image onto this image by simply replacing pixels.
  
  TODO: Implement functionality with ChunkyPNG and OilyPNG so that an image can be composited onto another
  regardless of its size: however, only the intersecting elements of both images should be mixed.
  
  This method should replace ChunkyPNG::Canvas.replace!
*/
VALUE oily_png_replace_bang(int argc, VALUE *argv, VALUE c);

#endif
