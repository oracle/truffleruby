#ifndef OILY_PNG_RESAMPLING_H
#define OILY_PNG_RESAMPLING_H

/*
 * Generates the interpolation steps (nearest neighbour) through two values.
 */
void oily_png_generate_steps_residues(long width, long new_width, long *steps, long *residues);

/*
 * Generates the interpolation steps through two values.
 *
 * Returns a Ruby Array
 */
VALUE oily_png_canvas_steps_residues(VALUE self, VALUE width, VALUE new_width);
VALUE oily_png_canvas_steps(VALUE self, VALUE width, VALUE new_width);


/*
 * Performs nearest neighbor interpolation on the Canvas
 */
VALUE oily_png_canvas_resample_nearest_neighbor_bang(VALUE self, VALUE new_width, VALUE new_height);

VALUE oily_png_canvas_resample_bilinear_bang(VALUE self, VALUE new_width, VALUE new_height);

#endif
