#include <ruby.h>
#include <ruby/encoding.h>

#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>

// Private helper macros

#define rb_boolean(c) ((c) ? Qtrue : Qfalse)

extern bool (*rb_tr_is_native_object)(VALUE value);
