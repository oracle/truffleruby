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

static inline
unsigned int nlz_long(unsigned long x) {
#if defined(HAVE_BUILTIN___BUILTIN_CLZL)
    if (x == 0) return SIZEOF_LONG * CHAR_BIT;
    return (unsigned int)__builtin_clzl(x);
#else
    #error no __builtin_clzl
#endif
}

static inline
unsigned int nlz_intptr(uintptr_t x) {
#if SIZEOF_UINTPTR_T == SIZEOF_LONG
    return nlz_long(x);
#else
    #error no known integer type corresponds uintptr_t
    return /* sane compiler */ ~0;
#endif
}

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

VALUE rb_imemo_tmpbuf_auto_free_pointer(void);
void* rb_imemo_tmpbuf_set_ptr(VALUE v, void *ptr);

rb_imemo_tmpbuf_t *rb_imemo_tmpbuf_parser_heap(void *buf, rb_imemo_tmpbuf_t *old_heap, size_t cnt);

#if defined(HAVE_MALLOC_USABLE_SIZE) || defined(HAVE_MALLOC_SIZE) || defined(_WIN32)
#define ruby_sized_xrealloc(ptr, new_size, old_size) ruby_xrealloc(ptr, new_size)
#define ruby_sized_xrealloc2(ptr, new_count, element_size, old_count) ruby_xrealloc2(ptr, new_count, element_size)
#define ruby_sized_xfree(ptr, size) ruby_xfree(ptr)
#define SIZED_REALLOC_N(var,type,n,old_n) REALLOC_N(var, type, n)
#else
RUBY_SYMBOL_EXPORT_BEGIN
void *ruby_sized_xrealloc(void *ptr, size_t new_size, size_t old_size) RUBY_ATTR_RETURNS_NONNULL RUBY_ATTR_ALLOC_SIZE((2));
void *ruby_sized_xrealloc2(void *ptr, size_t new_count, size_t element_size, size_t old_count) RUBY_ATTR_RETURNS_NONNULL RUBY_ATTR_ALLOC_SIZE((2, 3));
void ruby_sized_xfree(void *x, size_t size);
RUBY_SYMBOL_EXPORT_END
#define SIZED_REALLOC_N(var,type,n,old_n) ((var)=(type*)ruby_sized_xrealloc2((void*)(var), (n), sizeof(type), (old_n)))
#endif

void *rb_xmalloc_mul_add(size_t, size_t, size_t) RUBY_ATTR_MALLOC;

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

VALUE rb_ident_hash_new(void);

extern unsigned long ruby_scan_digits(const char *str, ssize_t len, int base, size_t *retlen, int *overflow);

struct rb_global_entry {
    struct rb_global_variable *var;
    ID id;
};

struct rb_global_entry *rb_global_entry(ID);

/* A macro for defining a flexible array, like: VALUE ary[FLEX_ARY_LEN]; */
#if defined(__STDC_VERSION__) && (__STDC_VERSION__ >= 199901L)
# define FLEX_ARY_LEN   /* VALUE ary[]; */
#elif defined(__GNUC__) && !defined(__STRICT_ANSI__)
# define FLEX_ARY_LEN 0 /* VALUE ary[0]; */
#else
# define FLEX_ARY_LEN 1 /* VALUE ary[1]; */
#endif

#define BIGNUM_SIGN_BIT ((VALUE)FL_USER1)
#define BIGNUM_NEGATE(b) (RBASIC(b)->flags ^= BIGNUM_SIGN_BIT)

struct RRational {
    struct RBasic basic;
    const VALUE num;
    const VALUE den;
};

#define RRATIONAL(obj) (R_CAST(RRational)(obj))
#define RRATIONAL_SET_NUM(rat, n) RB_OBJ_WRITE((rat), &((struct RRational *)(rat))->num,(n))

struct RFloat {
    struct RBasic basic;
    double float_value;
};

#define RFLOAT(obj)  (R_CAST(RFloat)(obj))

struct RComplex {
    struct RBasic basic;
    const VALUE real;
    const VALUE imag;
};

#define RCOMPLEX(obj) (R_CAST(RComplex)(obj))

#define RCOMPLEX_SET_REAL(cmp, r) RB_OBJ_WRITE((cmp), &((struct RComplex *)(cmp))->real,(r))
#define RCOMPLEX_SET_IMAG(cmp, i) RB_OBJ_WRITE((cmp), &((struct RComplex *)(cmp))->imag,(i))

/* re.c */
VALUE rb_reg_compile(VALUE str, int options, const char *sourcefile, int sourceline);
VALUE rb_reg_check_preprocess(VALUE);

/* compile.c */
struct rb_block;
int rb_dvar_defined(ID, const struct rb_block *);
int rb_local_defined(ID, const struct rb_block *);

/* io.c */
int rb_stderr_tty_p(void);

/* error.c */
extern VALUE rb_eEAGAIN;
extern VALUE rb_eEWOULDBLOCK;
extern VALUE rb_eEINPROGRESS;
void rb_report_bug_valist(VALUE file, int line, const char *fmt, va_list args);
VALUE rb_check_backtrace(VALUE);
NORETURN(void rb_async_bug_errno(const char *,int));
const char *rb_builtin_type_name(int t);
const char *rb_builtin_class_name(VALUE x);
PRINTF_ARGS(void rb_sys_warn(const char *fmt, ...), 1, 2);
PRINTF_ARGS(void rb_syserr_warn(int err, const char *fmt, ...), 2, 3);
PRINTF_ARGS(void rb_sys_warning(const char *fmt, ...), 1, 2);
PRINTF_ARGS(void rb_syserr_warning(int err, const char *fmt, ...), 2, 3);
#ifdef RUBY_ENCODING_H
VALUE rb_syntax_error_append(VALUE, VALUE, int, int, rb_encoding*, const char*, va_list);
PRINTF_ARGS(void rb_enc_warn(rb_encoding *enc, const char *fmt, ...), 2, 3);
PRINTF_ARGS(void rb_sys_enc_warn(rb_encoding *enc, const char *fmt, ...), 2, 3);
PRINTF_ARGS(void rb_syserr_enc_warn(int err, rb_encoding *enc, const char *fmt, ...), 3, 4);
PRINTF_ARGS(void rb_enc_warning(rb_encoding *enc, const char *fmt, ...), 2, 3);
PRINTF_ARGS(void rb_sys_enc_warning(rb_encoding *enc, const char *fmt, ...), 2, 3);
PRINTF_ARGS(void rb_syserr_enc_warning(int err, rb_encoding *enc, const char *fmt, ...), 3, 4);
#endif

rb_warning_category_t rb_warning_category_from_name(VALUE category);
bool rb_warning_category_enabled_p(rb_warning_category_t category);

/* symbol.c */
VALUE rb_sym_intern_ascii_cstr(const char *ptr);

/* thread.c */
VALUE rb_suppress_tracing(VALUE (*func)(VALUE), VALUE arg);

/* vm.c */
VALUE rb_source_location(int *pline);

/* io.c (export) */
void rb_write_error_str(VALUE mesg);

#endif /* RUBY_INTERNAL_H */
