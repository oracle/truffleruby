#include "oily_png_ext.h"
#include <math.h>

void oily_png_generate_steps_residues(long width, long new_width, long *steps, long *residues) {
  long base_step = width / new_width;
  long err_step = (width % new_width) << 1;
  long denominator = new_width << 1;

  long index;
  long err;
  /* We require an arithmetic modolus and rounding to the left of zero
   * This is standard Ruby behaviour (I hope!) but differs with how C/Java
   * typically handle integer division and modulo. But since we are workig
   * in mixed numbers, Ruby's convention is especially convienent */
  if (width >= new_width) {
    index = (width - new_width) / denominator;
    err = (width - new_width) % denominator;
  } else {
    index = (width - new_width) / denominator - 1;
    err = denominator - ((new_width - width) % denominator);
  }

  long i;
  for (i=0; i < new_width; i++){
    if (residues != NULL) {
      steps[i] = index;
      residues[i] = (long) round(255.0 * (float) err / (float) denominator);
    } else {
      /* If residues aren't requested, we round to the nearest pixel */
      if (err < new_width) {
        steps[i] = index;
      } else {
        steps[i] = index + 1;
      }
    }

    index += base_step;
    err += err_step;
    if (err >= denominator) {
      index += 1;
      err -= denominator;
    }
  }
}

VALUE oily_png_canvas_steps(VALUE self, VALUE v_width, VALUE v_new_width) {
  UNUSED_PARAMETER(self);
  long width = NUM2LONG(v_width);
  long new_width = NUM2LONG(v_new_width);

  long *steps = ALLOC_N(long, new_width);

  VALUE ret = rb_ary_new2(new_width);

  oily_png_generate_steps_residues(width, new_width, steps, NULL);

  long i;
  for (i=0; i < new_width; i++) {
    rb_ary_store(ret, i, LONG2FIX(steps[i]));
  }

  /* This is an unprotected allocation; it will leak on exception.
   * However, rb_ary_store should not generate one as we have
   * pre-allocated the array.
   */
  xfree(steps);
  steps = NULL;

  return ret;
}


VALUE oily_png_canvas_steps_residues(VALUE self, VALUE v_width, VALUE v_new_width) {
  UNUSED_PARAMETER(self);
  long width = NUM2LONG(v_width);
  long new_width = NUM2LONG(v_new_width);


  VALUE ret_steps = rb_ary_new2(new_width);
  VALUE ret_residues = rb_ary_new2(new_width);


  long *steps = ALLOC_N(long, new_width);
  long *residues = ALLOC_N(long, new_width);

  oily_png_generate_steps_residues(width, new_width, steps, residues);


  long i;
  for (i=0; i < new_width; i++) {
    rb_ary_store(ret_steps, i, LONG2FIX(steps[i]));
    rb_ary_store(ret_residues, i, LONG2FIX(residues[i]));
  }

  /* This is an unprotected allocation; it will leak on exception.
   * However, rb_ary_store should not generate one as we have
   * pre-allocated the array.
   */
  xfree(steps);
  steps = NULL;

  xfree(residues);
  residues = NULL;


  /* We return multiple values */
  VALUE ret = rb_ary_new2(2);
  rb_ary_store(ret, 0, ret_steps);
  rb_ary_store(ret, 1, ret_residues);

  return ret;
}

VALUE oily_png_canvas_resample_nearest_neighbor_bang(VALUE self, VALUE v_new_width, VALUE v_new_height) {
  long new_width = NUM2LONG(v_new_width);
  long new_height = NUM2LONG(v_new_height);

  long self_width = NUM2LONG(rb_funcall(self, rb_intern("width"), 0));
  long self_height = NUM2LONG(rb_funcall(self, rb_intern("height"), 0));

  VALUE pixels = rb_ary_new2(new_width*new_height);
  VALUE source = rb_iv_get(self, "@pixels");

  long *steps_x = ALLOC_N(long, new_width);
  long *steps_y = ALLOC_N(long, new_height);

  oily_png_generate_steps_residues(self_width, new_width, steps_x, NULL);
  oily_png_generate_steps_residues(self_height, new_height, steps_y, NULL);

  long index = 0;
  long x, y;
  long src_index;
  for (y=0; y < new_height; y++) {
    for (x = 0; x < new_width; x++) {
      src_index = steps_y[y] * self_width + steps_x[x];
      VALUE pixel = rb_ary_entry(source, src_index);
      rb_ary_store(pixels, index, pixel);
      index++;
    }
  }

  xfree(steps_x);
  steps_x = NULL;

  xfree(steps_y);
  steps_y = NULL;

  rb_iv_set(self, "@pixels", pixels);
  rb_iv_set(self, "@width", LONG2NUM(new_width));
  rb_iv_set(self, "@height", LONG2NUM(new_height));

  return self;
}

VALUE oily_png_canvas_resample_bilinear_bang(VALUE self, VALUE v_new_width, VALUE v_new_height) {
  long new_width = NUM2LONG(v_new_width);
  long new_height = NUM2LONG(v_new_height);

  long self_width = NUM2LONG(rb_funcall(self, rb_intern("width"), 0));
  long self_height = NUM2LONG(rb_funcall(self, rb_intern("height"), 0));

  VALUE pixels = rb_ary_new2(new_width*new_height);
  VALUE source = rb_iv_get(self, "@pixels");

  long *index_x = ALLOC_N(long, new_width);
  long *index_y = ALLOC_N(long, new_height);
  long *interp_x = ALLOC_N(long, new_width);
  long *interp_y = ALLOC_N(long, new_height);

  oily_png_generate_steps_residues(self_width, new_width, index_x, interp_x);
  oily_png_generate_steps_residues(self_height, new_height, index_y, interp_y);

  long index = 0;
  long x, y;
  long y1, y2, x1, x2;
  PIXEL y_residue, x_residue;
  PIXEL pixel_11, pixel_21, pixel_12, pixel_22;
  PIXEL pixel_top, pixel_bot;
  for (y = 0; y < new_height; y++) {
    y1 = index_y[y] < 0 ? 0 : index_y[y];
    y2 = y1+1 >= self_height ? self_height-1 : y1+1;
    y_residue = interp_y[y];

    for (x = 0; x < new_width; x++) {
      x1 = index_x[x] < 0 ? 0 : index_x[x];
      x2 = x1+1 >= self_width ? self_height-1 : x1+1;
      x_residue = interp_x[x];

      pixel_11 = NUM2UINT(rb_ary_entry(source, y1*self_width + x1));
      pixel_21 = NUM2UINT(rb_ary_entry(source, y1*self_width + x2));
      pixel_12 = NUM2UINT(rb_ary_entry(source, y2*self_width + x1));
      pixel_22 = NUM2UINT(rb_ary_entry(source, y2*self_width + x2));

      pixel_top = oily_png_color_interpolate_quick(pixel_21, pixel_11, x_residue);
      pixel_bot = oily_png_color_interpolate_quick(pixel_22, pixel_12, x_residue);

      rb_ary_store(pixels, index++, UINT2NUM(oily_png_color_interpolate_quick(pixel_bot, pixel_top, y_residue)));
    }
  }

  xfree(index_x);
  xfree(index_y);
  xfree(interp_x);
  xfree(interp_y);
  interp_x = NULL;
  interp_y = NULL;

  rb_iv_set(self, "@pixels", pixels);
  rb_iv_set(self, "@width", LONG2NUM(new_width));
  rb_iv_set(self, "@height", LONG2NUM(new_height));

  return self;
}
