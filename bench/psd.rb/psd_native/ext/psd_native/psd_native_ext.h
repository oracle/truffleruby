#ifndef PSD_NATIVE_EXT
#define PSD_NATIVE_EXT

#include "ruby.h"

#define RSTRING_NOT_MODIFIED

// Pixels use 32 bits unsigned integers
// We borrow this from OilyPNG
typedef uint32_t PIXEL;

// Our native mixins
#include "util.h"
#include "file.h"
#include "color.h"
#include "clipping_mask.h"
#include "mask.h"
#include "compose.h"
#include "image_mode_cmyk.h"
#include "image_mode_greyscale.h"
#include "image_mode_rgb.h"
#include "rle_decoding.h"
#include "canvas.h"
#include "blender.h"
#include "layer_raw.h"

void Init_psd_native();
// VALUE psd_class();
void psd_logger(char* level, char* message);

#endif