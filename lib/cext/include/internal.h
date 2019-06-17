#include "ruby.h"

#define NO_SANITIZE(x, y)     y
#define LIKELY(x)             RB_LIKELY(x)
#define UNLIKELY(x)           RB_UNLIKELY(x)

#define STR_EMBED_P(str)      (str, false)
#define STR_SHARED_P(str)     (str, false)

VALUE rb_hash_key_str(VALUE);
VALUE rb_hash_delete_entry(VALUE hash, VALUE key);
