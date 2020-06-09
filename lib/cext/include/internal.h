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
VALUE rb_hash_keys(VALUE hash);
VALUE rb_hash_delete_entry(VALUE hash, VALUE key);

VALUE rb_int_positive_pow(long x, unsigned long y);

VALUE rb_fstring(VALUE str);
VALUE rb_str_normalize_ospath(const char *ptr, long len);

#if defined(__STDC_VERSION__) && (__STDC_VERSION__ >= 201112L)
# define STATIC_ASSERT(name, expr) _Static_assert(expr, #name ": " #expr)
#elif GCC_VERSION_SINCE(4, 6, 0) || __has_extension(c_static_assert)
# define STATIC_ASSERT(name, expr) RB_GNUC_EXTENSION _Static_assert(expr, #name ": " #expr)
#else
# define STATIC_ASSERT(name, expr) typedef int static_assert_##name##_check[1 - 2*!(expr)]
#endif

typedef struct rb_imemo_tmpbuf_struct {
    VALUE flags;
    VALUE reserved;
    VALUE *ptr; /* malloc'ed buffer */
    struct rb_imemo_tmpbuf_struct *next; /* next imemo */
    size_t cnt; /* buffer size in VALUE */
} rb_imemo_tmpbuf_t;

#ifndef IMEMO_DEBUG
#define IMEMO_DEBUG 0
#endif

enum imemo_type {
    imemo_env            =  0,
    imemo_cref           =  1, /*!< class reference */
    imemo_svar           =  2, /*!< special variable */
    imemo_throw_data     =  3,
    imemo_ifunc          =  4, /*!< iterator function */
    imemo_memo           =  5,
    imemo_ment           =  6,
    imemo_iseq           =  7,
    imemo_tmpbuf         =  8,
    imemo_ast            =  9,
    imemo_parser_strterm = 10
};

/* FL_USER0 to FL_USER3 is for type */
#define IMEMO_FL_USHIFT (FL_USHIFT + 4)
#define IMEMO_FL_USER0 FL_USER4
#define IMEMO_FL_USER1 FL_USER5
#define IMEMO_FL_USER2 FL_USER6
#define IMEMO_FL_USER3 FL_USER7
#define IMEMO_FL_USER4 FL_USER8

#if IMEMO_DEBUG
VALUE rb_imemo_new_debug(enum imemo_type type, VALUE v1, VALUE v2, VALUE v3, VALUE v0, const char *file, int line);
#define rb_imemo_new(type, v1, v2, v3, v0) rb_imemo_new_debug(type, v1, v2, v3, v0, __FILE__, __LINE__)
#else
VALUE rb_imemo_new(enum imemo_type type, VALUE v1, VALUE v2, VALUE v3, VALUE v0);
#endif

#if defined(HAVE_MALLOC_USABLE_SIZE) || defined(HAVE_MALLOC_SIZE) || defined(_WIN32)
#define ruby_sized_xrealloc(ptr, new_size, old_size) ruby_xrealloc(ptr, new_size)
#define ruby_sized_xrealloc2(ptr, new_count, element_size, old_count) ruby_xrealloc(ptr, new_count, element_size)
#define ruby_sized_xfree(ptr, size) ruby_xfree(ptr)
#define SIZED_REALLOC_N(var,type,n,old_n) REALLOC_N(var, type, n)
#else
RUBY_SYMBOL_EXPORT_BEGIN
void *ruby_sized_xrealloc(void *ptr, size_t new_size, size_t old_size) RUBY_ATTR_ALLOC_SIZE((2));
void *ruby_sized_xrealloc2(void *ptr, size_t new_count, size_t element_size, size_t old_count) RUBY_ATTR_ALLOC_SIZE((2, 3));
void ruby_sized_xfree(void *x, size_t size);
RUBY_SYMBOL_EXPORT_END
#define SIZED_REALLOC_N(var,type,n,old_n) ((var)=(type*)ruby_sized_xrealloc((char*)(var), (n) * sizeof(type), (old_n) * sizeof(type)))
#endif


VALUE rb_ident_hash_new(void);

extern unsigned long ruby_scan_digits(const char *str, ssize_t len, int base, size_t *retlen, int *overflow);


#endif /* RUBY_INTERNAL_H */
