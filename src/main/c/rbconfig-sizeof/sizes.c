#include "ruby/ruby.h"

#if defined(HAVE_TYPE_SIG_ATOMIC_T)
# include <signal.h>
#endif

#if defined(HAVE_TYPE_WINT_T) || defined(HAVE_TYPE_WCTRANS_T) || defined(HAVE_TYPE_WCTYPE_T)
# include <wctype.h>
#endif

extern void Init_limits(void);
void
Init_sizeof(void)
{
    VALUE s = rb_hash_new();
    rb_define_const(rb_define_module("RbConfig"), "SIZEOF", s);

#define DEFINE(type, size) rb_hash_aset(s, rb_str_new_cstr(#type), INT2FIX(SIZEOF_##size))
#define DEFINE_SIZE(type) rb_hash_aset(s, rb_str_new_cstr(#type), INT2FIX(sizeof(type)))

#if SIZEOF_INT != 0
    DEFINE(int, INT);
#endif
#if SIZEOF_SHORT != 0
    DEFINE(short, SHORT);
#endif
#if SIZEOF_LONG != 0
    DEFINE(long, LONG);
#endif
#if SIZEOF_LONG_LONG != 0 && defined(HAVE_TRUE_LONG_LONG)
    DEFINE(long long, LONG_LONG);
#endif
#if SIZEOF___INT64 != 0
    DEFINE(__int64, __INT64);
#endif
#ifdef HAVE_TYPE___INT128
    DEFINE_SIZE(__int128);
#endif
#if SIZEOF_OFF_T != 0
    DEFINE(off_t, OFF_T);
#endif
#if SIZEOF_VOIDP != 0
    DEFINE(void*, VOIDP);
#endif
#if SIZEOF_FLOAT != 0
    DEFINE(float, FLOAT);
#endif
#if SIZEOF_DOUBLE != 0
    DEFINE(double, DOUBLE);
#endif
#if SIZEOF_TIME_T != 0
    DEFINE(time_t, TIME_T);
#endif
#if SIZEOF_CLOCK_T != 0
    DEFINE(clock_t, CLOCK_T);
#endif
#if SIZEOF_SIZE_T != 0
    DEFINE(size_t, SIZE_T);
#endif
#if SIZEOF_PTRDIFF_T != 0
    DEFINE(ptrdiff_t, PTRDIFF_T);
#endif
#if SIZEOF_INT8_T != 0
    DEFINE(int8_t, INT8_T);
#endif
#if SIZEOF_UINT8_T != 0
    DEFINE(uint8_t, UINT8_T);
#endif
#if SIZEOF_INT16_T != 0
    DEFINE(int16_t, INT16_T);
#endif
#if SIZEOF_UINT16_T != 0
    DEFINE(uint16_t, UINT16_T);
#endif
#if SIZEOF_INT32_T != 0
    DEFINE(int32_t, INT32_T);
#endif
#if SIZEOF_UINT32_T != 0
    DEFINE(uint32_t, UINT32_T);
#endif
#if SIZEOF_INT64_T != 0
    DEFINE(int64_t, INT64_T);
#endif
#if SIZEOF_UINT64_T != 0
    DEFINE(uint64_t, UINT64_T);
#endif
#if SIZEOF_INT128_T != 0
    DEFINE(int128_t, INT128_T);
#endif
#if SIZEOF_UINT128_T != 0
    DEFINE(uint128_t, UINT128_T);
#endif
#if SIZEOF_INTPTR_T != 0
    DEFINE(intptr_t, INTPTR_T);
#endif
#if SIZEOF_UINTPTR_T != 0
    DEFINE(uintptr_t, UINTPTR_T);
#endif
#if SIZEOF_SSIZE_T != 0
    DEFINE(ssize_t, SSIZE_T);
#endif
#ifdef HAVE_TYPE_INT_LEAST8_T
    DEFINE_SIZE(int_least8_t);
#endif
#ifdef HAVE_TYPE_INT_LEAST16_T
    DEFINE_SIZE(int_least16_t);
#endif
#ifdef HAVE_TYPE_INT_LEAST32_T
    DEFINE_SIZE(int_least32_t);
#endif
#ifdef HAVE_TYPE_INT_LEAST64_T
    DEFINE_SIZE(int_least64_t);
#endif
#ifdef HAVE_TYPE_INT_FAST8_T
    DEFINE_SIZE(int_fast8_t);
#endif
#ifdef HAVE_TYPE_INT_FAST16_T
    DEFINE_SIZE(int_fast16_t);
#endif
#ifdef HAVE_TYPE_INT_FAST32_T
    DEFINE_SIZE(int_fast32_t);
#endif
#ifdef HAVE_TYPE_INT_FAST64_T
    DEFINE_SIZE(int_fast64_t);
#endif
#ifdef HAVE_TYPE_INTMAX_T
    DEFINE_SIZE(intmax_t);
#endif
#ifdef HAVE_TYPE_SIG_ATOMIC_T
    DEFINE_SIZE(sig_atomic_t);
#endif
#ifdef HAVE_TYPE_WCHAR_T
    DEFINE_SIZE(wchar_t);
#endif
#ifdef HAVE_TYPE_WINT_T
    DEFINE_SIZE(wint_t);
#endif
#ifdef HAVE_TYPE_WCTRANS_T
    DEFINE_SIZE(wctrans_t);
#endif
#ifdef HAVE_TYPE_WCTYPE_T
    DEFINE_SIZE(wctype_t);
#endif
#ifdef HAVE_TYPE__BOOL
    DEFINE_SIZE(_Bool);
#endif
#ifdef HAVE_TYPE_LONG_DOUBLE
    DEFINE_SIZE(long double);
#endif
#ifdef HAVE_TYPE_FLOAT__COMPLEX
    DEFINE_SIZE(float _Complex);
#endif
#ifdef HAVE_TYPE_DOUBLE__COMPLEX
    DEFINE_SIZE(double _Complex);
#endif
#ifdef HAVE_TYPE_LONG_DOUBLE__COMPLEX
    DEFINE_SIZE(long double _Complex);
#endif
#ifdef HAVE_TYPE_FLOAT__IMAGINARY
    DEFINE_SIZE(float _Imaginary);
#endif
#ifdef HAVE_TYPE_DOUBLE__IMAGINARY
    DEFINE_SIZE(double _Imaginary);
#endif
#ifdef HAVE_TYPE_LONG_DOUBLE__IMAGINARY
    DEFINE_SIZE(long double _Imaginary);
#endif
#ifdef HAVE_TYPE___INT128
    DEFINE_SIZE(__int128);
#endif
#ifdef HAVE_TYPE___FLOAT128
    DEFINE_SIZE(__float128);
#endif
#ifdef HAVE_TYPE__DECIMAL32
    DEFINE_SIZE(_Decimal32);
#endif
#ifdef HAVE_TYPE__DECIMAL64
    DEFINE_SIZE(_Decimal64);
#endif
#ifdef HAVE_TYPE__DECIMAL128
    DEFINE_SIZE(_Decimal128);
#endif
#ifdef HAVE_TYPE___M64
    DEFINE_SIZE(__m64);
#endif
#ifdef HAVE_TYPE___M128
    DEFINE_SIZE(__m128);
#endif
#ifdef HAVE_TYPE___FLOAT80
    DEFINE_SIZE(__float80);
#endif
    OBJ_FREEZE(s);

#undef DEFINE
    Init_limits();
}
