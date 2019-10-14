/**********************************************************************

  internal.h -

  $Author$
  created at: Tue May 17 11:42:20 JST 2011

  Copyright (C) 2011 Yukihiro Matsumoto

**********************************************************************/

#ifndef RUBY_INTERNAL_H
#define RUBY_INTERNAL_H 1

#include "ruby.h"

#define LIKELY(x)             RB_LIKELY(x)
#define UNLIKELY(x)           RB_UNLIKELY(x)

#define STR_EMBED_P(str)      (str, false)
#define STR_SHARED_P(str)     (str, false)

VALUE rb_hash_key_str(VALUE);
VALUE rb_hash_delete_entry(VALUE hash, VALUE key);

VALUE rb_int_positive_pow(long x, unsigned long y);

VALUE rb_fstring(VALUE str);
VALUE rb_str_normalize_ospath(const char *ptr, long len);

#endif /* RUBY_INTERNAL_H */
