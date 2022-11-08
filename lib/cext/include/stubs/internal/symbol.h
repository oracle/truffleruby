#ifndef INTERNAL_SYMBOL_H                                /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_SYMBOL_H

#include "ruby.h"

VALUE rb_sym_intern_ascii_cstr(const char *ptr);
ID rb_make_temporary_id(size_t n);

#endif /* INTERNAL_SYMBOL_H */
