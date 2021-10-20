#ifndef INTERNAL_HASH_H                                  /*-*-C-*-vi:se ft=c:*/
#define INTERNAL_HASH_H

#include "ruby.h"

VALUE rb_hash_key_str(VALUE);
VALUE rb_hash_keys(VALUE hash);
VALUE rb_hash_delete_entry(VALUE hash, VALUE key);
VALUE rb_ident_hash_new(void);

#endif /* INTERNAL_HASH_H */
