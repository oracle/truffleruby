#ifndef INTERNAL_FILE_H                                  /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_FILE_H

#include "ruby.h"

#ifdef __APPLE__
VALUE rb_str_normalize_ospath(const char *ptr, long len);
#endif

#endif /* INTERNAL_FILE_H */
