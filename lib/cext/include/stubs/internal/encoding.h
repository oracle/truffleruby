#ifndef INTERNAL_ENCODING_H                              /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_ENCODING_H

#include "ruby.h"

#define rb_is_usascii_enc(enc) ((enc) == rb_usascii_encoding())

#endif /* INTERNAL_ENCODING_H */
