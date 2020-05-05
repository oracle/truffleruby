#include <ruby.h>
#include <ruby/encoding.h>

#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>

// Private helper macros

#define rb_boolean(c) ((c) ? Qtrue : Qfalse)
