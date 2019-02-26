#include "ruby.h"

#define NO_SANITIZE(x, y)     y
#define LIKELY(x)             RB_LIKELY(x)
#define UNLIKELY(x)           RB_UNLIKELY(x)

#define RHASH_ST_TABLE(hash)  (hash, false)
#define RHASH_TBL_RAW(hash)   (hash, false)

VALUE rb_hash_key_str(VALUE);
