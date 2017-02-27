/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * This file contains code that is based on the Ruby API headers,
 * copyright (C) Yukihiro Matsumoto, licensed under the 2-clause BSD licence.
 */

#ifndef TRUFFLERUBY_H
#define TRUFFLERUBY_H

#if defined(__cplusplus)
extern "C" {
#endif

#include <ctype.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdbool.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <sys/time.h>

#include <truffle.h>

// Support

#define RUBY_CEXT (void *)truffle_import_cached("ruby_cext")
#define MUST_INLINE __attribute__((always_inline))

// Configuration

#define TRUFFLERUBY 1

#define SIZEOF_INT 32
#define SIZEOF_LONG 64
#define LONG_LONG long long

#define HAVE_SYS_TIME_H

#define HAVE_RB_IO_T

// Overrides

#ifdef memcpy
#undef memcpy
#endif

#define memcpy truffle_managed_memcpy

// Macros

#define NORETURN(X) __attribute__((__noreturn__)) X
#define UNREACHABLE ((void)0)
#define _(x) x

#ifdef __cplusplus
#define ANYARGS ...
#else
#define ANYARGS
#endif

// Basic types

typedef void *VALUE;
typedef VALUE SIGNED_VALUE;

typedef VALUE ID;

typedef unsigned long st_data_t;
typedef st_data_t st_index_t;

// Helpers

NORETURN(VALUE rb_f_notimplement(int args_count, const VALUE *args, VALUE object));

// Non-standard

NORETURN(void rb_jt_error(const char *message));

void *rb_jt_to_native_handle(VALUE managed);
VALUE rb_jt_from_native_handle(void *native);

// Memory

#define xmalloc       malloc
#define xfree         free
#define ruby_xfree    free
#define ruby_xcalloc  calloc

#define ALLOC_N(type, n)            ((type *)malloc(sizeof(type) * (n)))
#define ALLOCA_N(type, n)           ((type *)alloca(sizeof(type) * (n)))

#define RB_ZALLOC_N(type, n)        ((type *)ruby_xcalloc((n), sizeof(type)))
#define RB_ZALLOC(type)             (RB_ZALLOC_N(type, 1))
#define ZALLOC_N(type, n)           RB_ZALLOC_N(type, n)
#define ZALLOC(type)                RB_ZALLOC(type)

void *rb_alloc_tmp_buffer(VALUE *buffer_pointer, long length);
void *rb_alloc_tmp_buffer2(VALUE *buffer_pointer, long count, size_t size);
void rb_free_tmp_buffer(VALUE *buffer_pointer);

#define RB_ALLOCV(v, n)             rb_alloc_tmp_buffer(&(v), (n))
#define RB_ALLOCV_N(type, v, n)     rb_alloc_tmp_buffer2(&(v), (n), sizeof(type))
#define RB_ALLOCV_END(v)            rb_free_tmp_buffer(&(v))

#define ALLOCV(v, n)                RB_ALLOCV(v, n)
#define ALLOCV_N(type, v, n)        RB_ALLOCV_N(type, v, n)
#define ALLOCV_END(v)               RB_ALLOCV_END(v)

// Types

enum ruby_value_type {
  RUBY_T_NONE     = 0x00,

  RUBY_T_OBJECT   = 0x01,
  RUBY_T_CLASS    = 0x02,
  RUBY_T_MODULE   = 0x03,
  RUBY_T_FLOAT    = 0x04,
  RUBY_T_STRING   = 0x05,
  RUBY_T_REGEXP   = 0x06,
  RUBY_T_ARRAY    = 0x07,
  RUBY_T_HASH     = 0x08,
  RUBY_T_STRUCT   = 0x09,
  RUBY_T_BIGNUM   = 0x0a,
  RUBY_T_FILE     = 0x0b,
  RUBY_T_DATA     = 0x0c,
  RUBY_T_MATCH    = 0x0d,
  RUBY_T_COMPLEX  = 0x0e,
  RUBY_T_RATIONAL = 0x0f,

  RUBY_T_NIL      = 0x11,
  RUBY_T_TRUE     = 0x12,
  RUBY_T_FALSE    = 0x13,
  RUBY_T_SYMBOL   = 0x14,
  RUBY_T_FIXNUM   = 0x15,
  RUBY_T_UNDEF    = 0x16,

  RUBY_T_IMEMO    = 0x1a,
  RUBY_T_NODE     = 0x1b,
  RUBY_T_ICLASS   = 0x1c,
  RUBY_T_ZOMBIE   = 0x1d,

  RUBY_T_MASK     = 0x1f
};

#define T_NONE    RUBY_T_NONE
#define T_NIL     RUBY_T_NIL
#define T_OBJECT  RUBY_T_OBJECT
#define T_CLASS   RUBY_T_CLASS
#define T_ICLASS  RUBY_T_ICLASS
#define T_MODULE  RUBY_T_MODULE
#define T_FLOAT   RUBY_T_FLOAT
#define T_STRING  RUBY_T_STRING
#define T_REGEXP  RUBY_T_REGEXP
#define T_ARRAY   RUBY_T_ARRAY
#define T_HASH    RUBY_T_HASH
#define T_STRUCT  RUBY_T_STRUCT
#define T_BIGNUM  RUBY_T_BIGNUM
#define T_FILE    RUBY_T_FILE
#define T_FIXNUM  RUBY_T_FIXNUM
#define T_TRUE    RUBY_T_TRUE
#define T_FALSE   RUBY_T_FALSE
#define T_DATA    RUBY_T_DATA
#define T_MATCH   RUBY_T_MATCH
#define T_SYMBOL  RUBY_T_SYMBOL
#define T_RATIONAL  RUBY_T_RATIONAL
#define T_COMPLEX   RUBY_T_COMPLEX
#define T_IMEMO   RUBY_T_IMEMO
#define T_UNDEF   RUBY_T_UNDEF
#define T_NODE    RUBY_T_NODE
#define T_ZOMBIE  RUBY_T_ZOMBIE
#define T_MASK    RUBY_T_MASK

int rb_type(VALUE value);
#define TYPE(value) rb_type((VALUE) (value))
bool RB_TYPE_P(VALUE value, int type);

void rb_check_type(VALUE value, int type);
#define Check_Type(v,t) rb_check_type((VALUE)(v), (t))

VALUE rb_obj_is_instance_of(VALUE object, VALUE ruby_class);
VALUE rb_obj_is_kind_of(VALUE object, VALUE ruby_class);

void rb_check_frozen(VALUE object);
void rb_check_safe_obj(VALUE object);

bool SYMBOL_P(VALUE value);

// Constants

VALUE rb_jt_get_undef(void);
#define Qundef rb_jt_get_undef()

VALUE rb_jt_get_true(void);
VALUE rb_jt_get_false(void);
VALUE rb_jt_get_nil(void);
VALUE rb_jt_get_Array(void);
VALUE rb_jt_get_Bignum(void);
VALUE rb_jt_get_Class(void);
VALUE rb_jt_get_Comparable(void);
VALUE rb_jt_get_Data(void);
VALUE rb_jt_get_Enumerable(void);
VALUE rb_jt_get_FalseClass(void);
VALUE rb_jt_get_File(void);
VALUE rb_jt_get_Fixnum(void);
VALUE rb_jt_get_Float(void);
VALUE rb_jt_get_Hash(void);
VALUE rb_jt_get_Integer(void);
VALUE rb_jt_get_IO(void);
VALUE rb_jt_get_Kernel(void);
VALUE rb_jt_get_Match(void);
VALUE rb_jt_get_Module(void);
VALUE rb_jt_get_NilClass(void);
VALUE rb_jt_get_Numeric(void);
VALUE rb_jt_get_Object(void);
VALUE rb_jt_get_Range(void);
VALUE rb_jt_get_Regexp(void);
VALUE rb_jt_get_String(void);
VALUE rb_jt_get_Struct(void);
VALUE rb_jt_get_Symbol(void);
VALUE rb_jt_get_Time(void);
VALUE rb_jt_get_Thread(void);
VALUE rb_jt_get_TrueClass(void);
VALUE rb_jt_get_Proc(void);
VALUE rb_jt_get_Method(void);
VALUE rb_jt_get_Dir(void);
VALUE rb_jt_get_ArgError(void);
VALUE rb_jt_get_EOFError(void);
VALUE rb_jt_get_Errno(void);
VALUE rb_jt_get_Exception(void);
VALUE rb_jt_get_FloatDomainError(void);
VALUE rb_jt_get_IndexError(void);
VALUE rb_jt_get_Interrupt(void);
VALUE rb_jt_get_IOError(void);
VALUE rb_jt_get_LoadError(void);
VALUE rb_jt_get_LocalJumpError(void);
VALUE rb_jt_get_MathDomainError(void);
VALUE rb_jt_get_EncCompatError(void);
VALUE rb_jt_get_NameError(void);
VALUE rb_jt_get_NoMemError(void);
VALUE rb_jt_get_NoMethodError(void);
VALUE rb_jt_get_NotImpError(void);
VALUE rb_jt_get_RangeError(void);
VALUE rb_jt_get_RegexpError(void);
VALUE rb_jt_get_RuntimeError(void);
VALUE rb_jt_get_ScriptError(void);
VALUE rb_jt_get_SecurityError(void);
VALUE rb_jt_get_Signal(void);
VALUE rb_jt_get_StandardError(void);
VALUE rb_jt_get_SyntaxError(void);
VALUE rb_jt_get_SystemCallError(void);
VALUE rb_jt_get_SystemExit(void);
VALUE rb_jt_get_SysStackError(void);
VALUE rb_jt_get_TypeError(void);
VALUE rb_jt_get_ThreadError(void);
VALUE rb_jt_get_WaitReadable(void);
VALUE rb_jt_get_WaitWritable(void);
VALUE rb_jt_get_ZeroDivError(void);
VALUE rb_jt_get_stdin(void);
VALUE rb_jt_get_stdout(void);
VALUE rb_jt_get_stderr(void);
VALUE rb_jt_get_output_fs(void);
VALUE rb_jt_get_rs(void);
VALUE rb_jt_get_output_rs(void);
VALUE rb_jt_get_default_rs(void);

#define Qtrue rb_jt_get_true()
#define Qfalse rb_jt_get_false()
#define Qnil rb_jt_get_nil()
#define rb_cArray rb_jt_get_Array()
#define rb_cBignum rb_jt_get_Bignum()
#define rb_cClass rb_jt_get_Class()
#define rb_mComparable rb_jt_get_Comparable()
#define rb_cData rb_jt_get_Data()
#define rb_mEnumerable rb_jt_get_Enumerable()
#define rb_cFalseClass rb_jt_get_FalseClass()
#define rb_cFile rb_jt_get_File()
#define rb_cFixnum rb_jt_get_Fixnum()
#define rb_cFloat rb_jt_get_Float()
#define rb_cHash rb_jt_get_Hash()
#define rb_cInteger rb_jt_get_Integer()
#define rb_cIO rb_jt_get_IO()
#define rb_mKernel rb_jt_get_Kernel()
#define rb_cMatch rb_jt_get_Match()
#define rb_cModule rb_jt_get_Module()
#define rb_cNilClass rb_jt_get_NilClass()
#define rb_cNumeric rb_jt_get_Numeric()
#define rb_cObject rb_jt_get_Object()
#define rb_cRange rb_jt_get_Range()
#define rb_cRegexp rb_jt_get_Regexp()
#define rb_cString rb_jt_get_String()
#define rb_cStruct rb_jt_get_Struct()
#define rb_cSymbol rb_jt_get_Symbol()
#define rb_cTime rb_jt_get_Time()
#define rb_cThread rb_jt_get_Thread()
#define rb_cTrueClass rb_jt_get_TrueClass()
#define rb_cProc rb_jt_get_Proc()
#define rb_cMethod rb_jt_get_Method()
#define rb_cDir rb_jt_get_Dir()
#define rb_eArgError rb_jt_get_ArgError()
#define rb_eEOFError rb_jt_get_EOFError()
#define rb_mErrno rb_jt_get_Errno()
#define rb_eException rb_jt_get_Exception()
#define rb_eFloatDomainError rb_jt_get_FloatDomainError()
#define rb_eIndexError rb_jt_get_IndexError()
#define rb_eInterrupt rb_jt_get_Interrupt()
#define rb_eIOError rb_jt_get_IOError()
#define rb_eLoadError rb_jt_get_LoadError()
#define rb_eLocalJumpError rb_jt_get_LocalJumpError()
#define rb_eMathDomainError rb_jt_get_MathDomainError()
#define rb_eEncCompatError rb_jt_get_EncCompatError()
#define rb_eNameError rb_jt_get_NameError()
#define rb_eNoMemError rb_jt_get_NoMemError()
#define rb_eNoMethodError rb_jt_get_NoMethodError()
#define rb_eNotImpError rb_jt_get_NotImpError()
#define rb_eRangeError rb_jt_get_RangeError()
#define rb_eRegexpError rb_jt_get_RegexpError()
#define rb_eRuntimeError rb_jt_get_RuntimeError()
#define rb_eScriptError rb_jt_get_ScriptError()
#define rb_eSecurityError rb_jt_get_SecurityError()
#define rb_eSignal rb_jt_get_Signal()
#define rb_eStandardError rb_jt_get_StandardError()
#define rb_eSyntaxError rb_jt_get_SyntaxError()
#define rb_eSystemCallError rb_jt_get_SystemCallError()
#define rb_eSystemExit rb_jt_get_SystemExit()
#define rb_eSysStackError rb_jt_get_SysStackError()
#define rb_eTypeError rb_jt_get_TypeError()
#define rb_eThreadError rb_jt_get_ThreadError()
#define rb_mWaitReadable rb_jt_get_WaitReadable()
#define rb_mWaitWritable rb_jt_get_WaitWritable()
#define rb_eZeroDivError rb_jt_get_ZeroDivError()
#define rb_stdin rb_jt_get_stdin()
#define rb_stdout rb_jt_get_stdout()
#define rb_stderr rb_jt_get_stderr()
#define rb_output_fs rb_jt_get_output_fs()
#define rb_rs rb_jt_get_rs()
#define rb_output_rs rb_jt_get_output_rs()
#define rb_default_rs rb_jt_get_default_rs()

#define rb_defout rb_stdout

// Conversions

VALUE CHR2FIX(char ch);
int NUM2INT(VALUE value);
unsigned int NUM2UINT(VALUE value);
long NUM2LONG(VALUE value);
unsigned long NUM2ULONG(VALUE value);
double NUM2DBL(VALUE value);
int FIX2INT(VALUE value);
unsigned int FIX2UINT(VALUE value);
long FIX2LONG(VALUE value);
VALUE INT2NUM(long value);
VALUE INT2FIX(long value);
VALUE UINT2NUM(unsigned int value);
VALUE LONG2NUM(long value);
VALUE ULONG2NUM(long value);
VALUE LONG2FIX(long value);
int rb_fix2int(VALUE value);
unsigned long rb_fix2uint(VALUE value);
int rb_long2int(long value);
ID SYM2ID(VALUE value);
VALUE ID2SYM(ID value);
#define NUM2TIMET(value) NUM2LONG(value)
#define TIMET2NUM(value) LONG2NUM(value)
char RB_NUM2CHR(VALUE x);
#define NUM2CHR(x) RB_NUM2CHR(x)
int rb_cmpint(VALUE val, VALUE a, VALUE b);
VALUE rb_int2inum(SIGNED_VALUE n);
VALUE rb_ll2inum(LONG_LONG n);
double rb_num2dbl(VALUE val);
long rb_num2int(VALUE val);
unsigned long rb_num2uint(VALUE val);
long rb_num2long(VALUE val);
unsigned long rb_num2ulong(VALUE val);
VALUE rb_num_coerce_bin(VALUE x, VALUE y, ID func);
VALUE rb_num_coerce_cmp(VALUE x, VALUE y, ID func);
VALUE rb_num_coerce_relop(VALUE x, VALUE y, ID func);
void rb_num_zerodiv(void);
VALUE LL2NUM(LONG_LONG n);

// Type checks

int RB_NIL_P(VALUE value);
int RB_FIXNUM_P(VALUE value);

#define NIL_P RB_NIL_P
#define FIXNUM_P RB_FIXNUM_P

int RTEST(VALUE value);

// Kernel

VALUE rb_require(const char *feature);
VALUE rb_eval_string(const char *str);
VALUE rb_exec_recursive(VALUE (*func) (VALUE, VALUE, int), VALUE obj, VALUE arg);
VALUE rb_f_sprintf(int argc, const VALUE *argv);
void rb_need_block(void);
void rb_set_end_proc(void (*func)(VALUE), VALUE data);
void rb_iter_break(void);
const char *rb_sourcefile(void);
int rb_sourceline(void);
int rb_method_boundp(VALUE klass, ID id, int ex);

// Object

VALUE rb_obj_dup(VALUE object);
VALUE rb_any_to_s(VALUE object);
VALUE rb_obj_instance_variables(VALUE object);
VALUE rb_check_convert_type(VALUE object, int type, const char *type_name, const char *method);
VALUE rb_check_to_integer(VALUE object, const char *method);
VALUE rb_check_string_type(VALUE object);
VALUE rb_convert_type(VALUE object, int type, const char *type_name, const char *method);
void rb_extend_object(VALUE object, VALUE module);
VALUE rb_inspect(VALUE object);
void rb_obj_call_init(VALUE object, int argc, const VALUE *argv);
const char *rb_obj_classname(VALUE object);
VALUE rb_obj_id(VALUE object);
int rb_obj_method_arity(VALUE object, ID id);
int rb_obj_respond_to(VALUE object, ID id, int priv);
VALUE rb_special_const_p(VALUE object);
int RB_BUILTIN_TYPE(VALUE object);
VALUE rb_to_int(VALUE object);
VALUE rb_obj_instance_eval(int argc, const VALUE *argv, VALUE self);
VALUE rb_ivar_defined(VALUE object, ID id);
VALUE rb_equal_opt(VALUE a, VALUE b);
VALUE rb_class_inherited_p(VALUE module, VALUE object);
VALUE rb_equal(VALUE a, VALUE b);

#define rb_type_p(object, type)         (rb_type(object) == (type))
#define BUILTIN_TYPE(object)            RB_BUILTIN_TYPE(object)

VALUE rb_obj_taint(VALUE object);
bool rb_jt_obj_taintable_p(VALUE object);
bool rb_jt_obj_tainted_p(VALUE object);
void rb_jt_obj_infect(VALUE a, VALUE b);
#define RB_OBJ_TAINTABLE(object)        rb_jt_obj_taintable_p(object)
#define RB_OBJ_TAINTED_RAW(object)      rb_jt_obj_tainted_p(object)
#define RB_OBJ_TAINTED(object)          rb_jt_obj_tainted_p(object)
#define RB_OBJ_TAINT_RAW(object)        rb_obj_taint(object)
#define RB_OBJ_TAINT(object)            rb_obj_taint(object)
#define RB_OBJ_UNTRUSTED(object)        rb_jt_obj_tainted_p(object)
#define RB_OBJ_UNTRUST(object)          rb_obj_taint(object)
#define OBJ_TAINTABLE(object)           rb_jt_obj_taintable_p(object)
#define OBJ_TAINTED_RAW(object)         rb_jt_obj_tainted_p(object)
#define OBJ_TAINTED(object)             rb_jt_obj_tainted_p(object)
#define OBJ_TAINT_RAW(object)           rb_obj_taint(object)
#define OBJ_TAINT(object)               rb_obj_taint(object)
#define OBJ_UNTRUSTED(object)           rb_jt_obj_tainted_p(object)
#define OBJ_UNTRUST(object)             rb_jt_obj_tainted_p(object)
#define RB_OBJ_INFECT_RAW(a, b)         rb_jt_obj_infect(a, b)
#define RB_OBJ_INFECT(a, b)             rb_jt_obj_infect(a, b)
#define OBJ_INFECT(a, b)                rb_jt_obj_infect(a, b)

VALUE rb_obj_freeze(VALUE object);
VALUE rb_obj_frozen_p(VALUE object);
#define rb_obj_freeze_inline(object)    rb_obj_freeze(object)
#define RB_OBJ_FROZEN_RAW(x)            rb_obj_frozen_p(object)
#define RB_OBJ_FROZEN(x)                rb_obj_frozen_p(object)
#define RB_OBJ_FREEZE_RAW(object)       rb_obj_freeze(object)
#define RB_OBJ_FREEZE(x)                rb_obj_freeze((VALUE)x)
#define OBJ_FROZEN_RAW(object)          rb_obj_frozen_p(object)
#define OBJ_FROZEN(object)              rb_obj_frozen_p(object)
#define OBJ_FREEZE_RAW(object)          rb_obj_freeze(object)
#define OBJ_FREEZE(object)              rb_obj_freeze(object)

// Integer

VALUE rb_Integer(VALUE value);

#define INTEGER_PACK_MSWORD_FIRST                   0x01
#define INTEGER_PACK_LSWORD_FIRST                   0x02
#define INTEGER_PACK_MSBYTE_FIRST                   0x10
#define INTEGER_PACK_LSBYTE_FIRST                   0x20
#define INTEGER_PACK_NATIVE_BYTE_ORDER              0x40
#define INTEGER_PACK_2COMP                          0x80
#define INTEGER_PACK_FORCE_GENERIC_IMPLEMENTATION   0x400
#define INTEGER_PACK_FORCE_BIGNUM                   0x100
#define INTEGER_PACK_NEGATIVE                       0x200
#define INTEGER_PACK_LITTLE_ENDIAN                  (INTEGER_PACK_LSWORD_FIRST | INTEGER_PACK_LSBYTE_FIRST)
#define INTEGER_PACK_BIG_ENDIAN                     (INTEGER_PACK_MSWORD_FIRST | INTEGER_PACK_MSBYTE_FIRST)

int rb_integer_pack(VALUE value, void *words, size_t numwords, size_t wordsize, size_t nails, int flags);
VALUE rb_integer_unpack(const void *words, size_t numwords, size_t wordsize, size_t nails, int flags);
size_t rb_absint_size(VALUE value, int *nlz_bits_ret);
VALUE rb_cstr_to_inum(const char* string, int base, int raise);

double rb_big2dbl(VALUE x);
VALUE rb_dbl2big(double d);
LONG_LONG rb_big2ll(VALUE x);
long rb_big2long(VALUE x);
VALUE rb_big2str(VALUE x, int base);
unsigned long rb_big2ulong(VALUE x);
VALUE rb_big_cmp(VALUE x, VALUE y);
void rb_big_pack(VALUE val, unsigned long *buf, long num_longs);

// Float

VALUE rb_float_new(double value);
VALUE rb_Float(VALUE value);
double RFLOAT_VALUE(VALUE value);

// String

typedef struct {
  char *name;
} rb_encoding;

enum ruby_coderange_type {
    RUBY_ENC_CODERANGE_UNKNOWN	= 0,
    RUBY_ENC_CODERANGE_7BIT	    = 1,
    RUBY_ENC_CODERANGE_VALID    = 2,
    RUBY_ENC_CODERANGE_BROKEN	  = 4
};

#define ENC_CODERANGE_UNKNOWN   RUBY_ENC_CODERANGE_UNKNOWN
#define ENC_CODERANGE_7BIT      RUBY_ENC_CODERANGE_7BIT
#define ENC_CODERANGE_VALID     RUBY_ENC_CODERANGE_VALID
#define ENC_CODERANGE_BROKEN    RUBY_ENC_CODERANGE_BROKEN

#define PRI_VALUE_PREFIX        "l"
#define PRI_LONG_PREFIX         "l"
#define PRI_64_PREFIX           PRI_LONG_PREFIX
#define RUBY_PRI_VALUE_MARK     "\v"
#define PRIdVALUE               PRI_VALUE_PREFIX"d"
#define PRIoVALUE               PRI_VALUE_PREFIX"o"
#define PRIuVALUE               PRI_VALUE_PREFIX"u"
#define PRIxVALUE               PRI_VALUE_PREFIX"x"
#define PRIXVALUE               PRI_VALUE_PREFIX"X"
#define PRIsVALUE               PRI_VALUE_PREFIX"i" RUBY_PRI_VALUE_MARK

char *RSTRING_PTR(VALUE string);
int rb_str_len(VALUE string);
#define RSTRING_LEN(str) rb_str_len(str)
#define RSTRING_LENINT(str) rb_str_len(str)
VALUE rb_intern_str(VALUE string);
VALUE rb_str_new(const char *string, long length);
VALUE rb_str_new_cstr(const char *string);
VALUE rb_str_new_shared(VALUE string);
VALUE rb_str_new_frozen(VALUE string);
VALUE rb_str_new_with_class(VALUE klass, const char *string, long len);
#define rb_str_new2 rb_str_new_cstr
#define rb_str_new3 rb_str_new_shared
#define rb_str_new4 rb_str_new_frozen
#define rb_str_new5 rb_str_new_with_class
VALUE rb_str_cat(VALUE string, const char *to_concat, long length);
VALUE rb_str_cat2(VALUE string, const char *to_concat);
VALUE rb_str_to_str(VALUE string);
VALUE rb_cstr2inum(const char *string, int base);
VALUE rb_str2inum(VALUE string, int base);
#define rb_str_buf_new2 rb_str_buf_new_cstr
VALUE rb_str_buf_new_cstr(const char *string);
int rb_str_cmp(VALUE a, VALUE b);
VALUE rb_str_buf_cat(VALUE string, const char *to_concat, long length);
rb_encoding *rb_to_encoding(VALUE encoding);
VALUE rb_str_conv_enc(VALUE string, rb_encoding *from, rb_encoding *to);
VALUE rb_str_conv_enc_opts(VALUE string, rb_encoding *from, rb_encoding *to, int ecflags, VALUE ecopts);
VALUE rb_external_str_new_with_enc(const char *string, long len, rb_encoding *eenc);
VALUE rb_external_str_with_enc(VALUE string, rb_encoding *eenc);
VALUE rb_external_str_new(const char *string, long len);
VALUE rb_external_str_new_cstr(const char *string);
VALUE rb_locale_str_new(const char *string, long len);
VALUE rb_locale_str_new_cstr(const char *string);
VALUE rb_filesystem_str_new(const char *string, long len);
VALUE rb_filesystem_str_new_cstr(const char *string);
VALUE rb_str_export(VALUE string);
VALUE rb_str_export_locale(VALUE string);
VALUE rb_str_export_to_enc(VALUE string, rb_encoding *enc);
rb_encoding *rb_default_external_encoding(void);
rb_encoding *rb_default_internal_encoding(void);
rb_encoding *rb_locale_encoding(void);
int rb_locale_encindex(void);
rb_encoding *rb_filesystem_encoding(void);
int rb_filesystem_encindex(void);
rb_encoding *get_encoding(VALUE string);
#define STR_ENC_GET(string) get_encoding(string)
#define rb_str_dup(string) rb_obj_dup(string)
#define rb_str_freeze(string) rb_obj_freeze(string)
#define rb_str_inspect(string) rb_inspect(string)
VALUE rb_str_intern(VALUE string);
VALUE rb_str_length(VALUE string);
VALUE rb_str_plus(VALUE a, VALUE b);
VALUE rb_str_subseq(VALUE string, long beg, long len);
VALUE rb_str_substr(VALUE string, long beg, long len);
st_index_t rb_str_hash(VALUE string);
void rb_str_update(VALUE string, long beg, long len, VALUE value);
VALUE rb_str_equal(VALUE a, VALUE b);
void rb_str_free(VALUE string);
unsigned int rb_enc_codepoint_len(const char *p, const char *e, int *len_p, rb_encoding *encoding);
rb_encoding *rb_enc_get(VALUE object);
#define RB_ENCODING_GET(obj) rb_enc_get(obj)
#define ENCODING_GET(obj) RB_ENCODING_GET(obj)
void rb_enc_set_index(VALUE obj, int idx);
#define RB_ENCODING_SET(obj,i) rb_enc_set_index((obj), (i))
#define ENCODING_SET(obj,i) RB_ENCODING_SET(obj,i)
rb_encoding *rb_ascii8bit_encoding(void);
int rb_ascii8bit_encindex(void);
rb_encoding *rb_usascii_encoding(void);
int rb_usascii_encindex(void);
rb_encoding *rb_utf8_encoding(void);
int rb_utf8_encindex(void);
#define StringValue(value) rb_string_value(&(value))
#define SafeStringValue StringValue
#define StringValuePtr(string) rb_string_value_ptr(&(string))
#define StringValueCStr(string) rb_string_value_cstr(&(string))
VALUE rb_str_buf_new(long capacity);
VALUE rb_sprintf(const char *format, ...);
VALUE rb_vsprintf(const char *format, va_list args);
VALUE rb_str_append(VALUE string, VALUE to_append);
void rb_str_set_len(VALUE string, long length);
VALUE rb_String(VALUE value);
VALUE rb_str_resize(VALUE string, long length);
#define RSTRING_GETMEM(string, data_pointer, length_pointer) ((data_pointer) = RSTRING_PTR(string), (length_pointer) = rb_str_len(string))
VALUE rb_str_split(VALUE string, const char *split);
void rb_str_modify(VALUE string);
#define ENC_CODERANGE_7BIT	RUBY_ENC_CODERANGE_7BIT
enum ruby_coderange_type RB_ENC_CODERANGE(VALUE obj);
#define RB_ENC_CODERANGE_ASCIIONLY(obj) (RB_ENC_CODERANGE(obj) == RUBY_ENC_CODERANGE_7BIT)
#define ENC_CODERANGE_ASCIIONLY(obj) RB_ENC_CODERANGE_ASCIIONLY(obj)
int rb_encdb_alias(const char *alias, const char *orig);
VALUE rb_enc_associate(VALUE obj, rb_encoding *enc);
VALUE rb_enc_associate_index(VALUE obj, int idx);
rb_encoding* rb_enc_compatible(VALUE str1, VALUE str2);
void rb_enc_copy(VALUE obj1, VALUE obj2);
int rb_enc_find_index(const char *name);
rb_encoding *rb_enc_find(const char *name);
VALUE rb_enc_from_encoding(rb_encoding *encoding);
rb_encoding *rb_enc_from_index(int index);
int rb_enc_str_coderange(VALUE str);
VALUE rb_enc_str_new(const char *ptr, long len, rb_encoding *enc);
int rb_enc_to_index(rb_encoding *enc);
VALUE rb_obj_encoding(VALUE obj);
VALUE rb_str_encode(VALUE str, VALUE to, int ecflags, VALUE ecopts);
VALUE rb_usascii_str_new(const char *ptr, long len);
VALUE rb_usascii_str_new_cstr(const char *ptr);
int rb_to_encoding_index(VALUE enc);
char* rb_enc_nth(const char *p, const char *e, long nth, rb_encoding *enc);
#define rb_enc_name(enc) ((enc)->name)
int rb_enc_get_index(VALUE obj);
MUST_INLINE VALUE rb_string_value(VALUE *value_pointer);
MUST_INLINE char *rb_string_value_ptr(VALUE *value_pointer);
MUST_INLINE char *rb_string_value_cstr(VALUE *value_pointer);

// Symbol

ID rb_to_id(VALUE name);
ID rb_intern(const char *string);
ID rb_intern2(const char *string, long length);
ID rb_intern3(const char *name, long len, rb_encoding *enc);
#define rb_intern_const(str) rb_intern2((str), strlen(str))
VALUE rb_sym2str(VALUE string);
const char *rb_id2name(ID id);
VALUE rb_id2str(ID id);
int rb_is_class_id(ID id);
int rb_is_const_id(ID id);
int rb_is_instance_id(ID id);

// Array

int RARRAY_LEN(VALUE array);
int RARRAY_LENINT(VALUE array);
VALUE *RARRAY_PTR(VALUE array);
VALUE RARRAY_AREF(VALUE array, long index);
VALUE rb_Array(VALUE value);
VALUE rb_ary_new(void);
VALUE rb_ary_new_capa(long capacity);
#define rb_ary_new2 rb_ary_new_capa
VALUE rb_ary_new_from_args(long n, ...);
#define rb_ary_new3 rb_ary_new_from_args
VALUE rb_ary_new4(long n, const VALUE *values);
VALUE rb_ary_push(VALUE array, VALUE value);
VALUE rb_ary_pop(VALUE array);
void rb_ary_store(VALUE array, long index, VALUE value);
VALUE rb_ary_entry(VALUE array, long index);
#define rb_ary_dup(array) rb_obj_dup(array)
VALUE rb_ary_each(VALUE array);
VALUE rb_ary_unshift(VALUE array, VALUE value);
VALUE rb_ary_aref(int n, const VALUE* values, VALUE array);
VALUE rb_ary_clear(VALUE array);
VALUE rb_ary_delete(VALUE array, VALUE value);
VALUE rb_ary_delete_at(VALUE array, long n);
VALUE rb_ary_includes(VALUE array, VALUE value);
VALUE rb_ary_join(VALUE array, VALUE sep);
VALUE rb_ary_to_s(VALUE array);
VALUE rb_ary_reverse(VALUE array);
VALUE rb_ary_shift(VALUE array);
VALUE rb_ary_concat(VALUE a, VALUE b);
VALUE rb_ary_plus(VALUE a, VALUE b);
VALUE rb_iterate(VALUE (*method)(), VALUE arg1, VALUE (*block)(), VALUE arg2);
VALUE rb_each(VALUE array);
void rb_mem_clear(VALUE *mem, long n);
#define rb_ary_freeze(array) rb_obj_freeze(array)
VALUE rb_ary_to_ary(VALUE array);
VALUE rb_ary_subseq(VALUE array, long start, long length);
#define rb_assoc_new(a, b) rb_ary_new3(2, a, b)
VALUE rb_check_array_type(VALUE array);

// Hash

VALUE rb_hash(VALUE obj);
VALUE rb_hash_new(void);
VALUE rb_hash_aref(VALUE hash, VALUE key);
VALUE rb_hash_aset(VALUE hash, VALUE key, VALUE value);
VALUE rb_hash_lookup(VALUE hash, VALUE key);
VALUE rb_hash_lookup2(VALUE hash, VALUE key, VALUE default_value);
VALUE rb_hash_set_ifnone(VALUE hash, VALUE if_none);
#define RHASH_SET_IFNONE(hash, if_none) rb_hash_set_ifnone((VALUE) hash, if_none)
st_index_t rb_memhash(const void *data, long length);
#define rb_hash_freeze(array) rb_obj_freeze(array)
VALUE rb_hash_clear(VALUE hash);
VALUE rb_hash_delete(VALUE hash, VALUE key);
VALUE rb_hash_delete_if(VALUE hash);
void rb_hash_foreach(VALUE hash, int (*func)(ANYARGS), VALUE farg);
VALUE rb_hash_size(VALUE hash);
#define rb_hash_dup(array) rb_obj_dup(array)

// Class

const char* rb_class2name(VALUE module);
VALUE rb_class_real(VALUE ruby_class);
VALUE rb_class_superclass(VALUE ruby_class);
VALUE rb_class_of(VALUE object);
VALUE rb_obj_class(VALUE object);
VALUE CLASS_OF(VALUE object);
VALUE rb_obj_alloc(VALUE ruby_class);
VALUE rb_class_path(VALUE ruby_class);
VALUE rb_path2class(const char *string);
VALUE rb_path_to_class(VALUE pathname);
VALUE rb_class_name(VALUE klass);
VALUE rb_class_new(VALUE super);
VALUE rb_class_new_instance(int argc, const VALUE *argv, VALUE klass);
VALUE rb_cvar_defined(VALUE klass, ID id);
VALUE rb_cvar_get(VALUE klass, ID id);
void rb_cvar_set(VALUE klass, ID id, VALUE val);
VALUE rb_cv_get(VALUE klass, const char *name);
void rb_cv_set(VALUE klass, const char *name, VALUE val);
void rb_define_attr(VALUE klass, const char *name, int read, int write);
void rb_define_class_variable(VALUE klass, const char *name, VALUE val);

// Proc

VALUE rb_proc_new(void *function, VALUE value);

// Utilities

void rb_warn(const char *fmt, ...);
void rb_warning(const char *fmt, ...);

MUST_INLINE int rb_jt_scan_args_0_hash(int argc, VALUE *argv, const char *format, VALUE *v1);
MUST_INLINE int rb_jt_scan_args_02(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2);
MUST_INLINE int rb_jt_scan_args_11(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2);
MUST_INLINE int rb_jt_scan_args_12(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2, VALUE *v3);
MUST_INLINE int rb_jt_scan_args_1_star(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2);
MUST_INLINE int rb_jt_scan_args(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2, VALUE *v3, VALUE *v4, VALUE *v5, VALUE *v6, VALUE *v7, VALUE *v8, VALUE *v9, VALUE *v10);

#define rb_jt_scan_args_1(ARGC, ARGV, FORMAT, V1) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_jt_scan_args_2(ARGC, ARGV, FORMAT, V1, V2) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_jt_scan_args_3(ARGC, ARGV, FORMAT, V1, V2, V3) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_jt_scan_args_4(ARGC, ARGV, FORMAT, V1, V2, V3, V4) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_jt_scan_args_5(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, NULL, NULL, NULL, NULL, NULL)
#define rb_jt_scan_args_6(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, NULL, NULL, NULL, NULL)
#define rb_jt_scan_args_7(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, NULL, NULL, NULL)
#define rb_jt_scan_args_8(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, NULL, NULL)
#define rb_jt_scan_args_9(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9, NULL)
#define rb_jt_scan_args_10(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10) rb_jt_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10)

#define SCAN_ARGS_IMPL(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, NAME, ...) NAME
#define rb_scan_args(ARGC, ARGV, FORMAT, ...) SCAN_ARGS_IMPL(__VA_ARGS__, rb_jt_scan_args_10, rb_jt_scan_args_9, rb_jt_scan_args_8, rb_jt_scan_args_7, rb_jt_scan_args_6, rb_jt_scan_args_5, rb_jt_scan_args_4, rb_jt_scan_args_3, rb_jt_scan_args_2, rb_jt_scan_args_1)(ARGC, ARGV, FORMAT, __VA_ARGS__)

VALUE rb_enumeratorize(VALUE obj, VALUE meth, int argc, const VALUE *argv);

// Calls

int rb_respond_to(VALUE object, ID name);
#define rb_funcall(object, name, ...) truffle_invoke(RUBY_CEXT, "rb_funcall", (void *)object, name, __VA_ARGS__)
VALUE rb_funcallv(VALUE object, ID name, int args_count, const VALUE *args);
VALUE rb_funcallv_public(VALUE object, ID name, int args_count, const VALUE *args);
#define rb_funcall2 rb_funcallv
#define rb_funcall3 rb_funcallv_public
VALUE rb_apply(VALUE object, ID name, VALUE args);
#define RUBY_BLOCK_CALL_FUNC_TAKES_BLOCKARG 1
#define RB_BLOCK_CALL_FUNC_ARGLIST(yielded_arg, callback_arg) VALUE yielded_arg, VALUE callback_arg, int __args_count, const VALUE *__args, VALUE __block_arg
typedef VALUE rb_block_call_func(RB_BLOCK_CALL_FUNC_ARGLIST(yielded_arg, callback_arg));
typedef rb_block_call_func *rb_block_call_func_t;
VALUE rb_block_call(VALUE object, ID name, int args_count, const VALUE *args, rb_block_call_func_t block_call_func, VALUE data);
VALUE rb_call_super(int args_count, const VALUE *args);
int rb_block_given_p();
VALUE rb_block_proc(void);
VALUE rb_yield(VALUE value);
VALUE rb_funcall_with_block(VALUE recv, ID mid, int argc, const VALUE *argv, VALUE pass_procval);
VALUE rb_yield_splat(VALUE values);
VALUE rb_yield_values(int n, ...);

// Instance variables

VALUE rb_iv_get(VALUE object, const char *name);
VALUE rb_iv_set(VALUE object, const char *name, VALUE value);
VALUE rb_ivar_get(VALUE object, ID name);
VALUE rb_ivar_set(VALUE object, ID name, VALUE value);
VALUE rb_ivar_lookup(VALUE object, const char *name, VALUE default_value);
VALUE rb_attr_get(VALUE object, const char *name);

// Accessing constants

int rb_const_defined(VALUE module, ID name);
int rb_const_defined_at(VALUE module, ID name);
VALUE rb_const_get(VALUE module, ID name);
VALUE rb_const_get_at(VALUE module, ID name);
VALUE rb_const_get_from(VALUE module, ID name);
VALUE rb_const_set(VALUE module, ID name, VALUE value);
VALUE rb_define_const(VALUE module, const char *name, VALUE value);
void rb_define_global_const(const char *name, VALUE value);

// Global variables

void rb_define_hooked_variable(
    const char *name,
    VALUE *var,
    VALUE (*getter)(ANYARGS),
    void  (*setter)(ANYARGS));

void rb_define_readonly_variable(const char *name, const VALUE *var);
void rb_define_variable(const char *name, VALUE *var);
VALUE rb_f_global_variables(void);
VALUE rb_gv_set(const char *name, VALUE val);
VALUE rb_gv_get(const char *name);
VALUE rb_lastline_get(void);
void rb_lastline_set(VALUE val);

// Exceptions

VALUE rb_exc_new(VALUE etype, const char *ptr, long len);
VALUE rb_exc_new_cstr(VALUE exception_class, const char *message);
VALUE rb_exc_new_str(VALUE exception_class, VALUE message);
#define rb_exc_new2 rb_exc_new_cstr
#define rb_exc_new3 rb_exc_new_str
NORETURN(void rb_exc_raise(VALUE exception));
NORETURN(void rb_raise(VALUE exception, const char *format, ...));
VALUE rb_protect(VALUE (*function)(VALUE), VALUE data, int *status);
void rb_jump_tag(int status);
void rb_set_errinfo(VALUE error);
void rb_syserr_fail(int errno, const char *message);
void rb_sys_fail(const char *message);
VALUE rb_ensure(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*e_proc)(ANYARGS), VALUE data2);
VALUE rb_rescue(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2);
VALUE rb_rescue2(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2, ...);
VALUE rb_make_backtrace(void);
void rb_throw_obj(VALUE tag, VALUE value);
void rb_throw(const char *tag, VALUE val);
VALUE rb_catch(const char *tag, VALUE (*func)(), VALUE data);
VALUE rb_catch_obj(VALUE t, VALUE (*func)(), VALUE data);

// Defining classes, modules and methods

VALUE rb_define_class(const char *name, VALUE superclass);
VALUE rb_define_class_under(VALUE module, const char *name, VALUE superclass);
VALUE rb_define_class_id_under(VALUE module, ID name, VALUE superclass);
VALUE rb_define_module(const char *name);
VALUE rb_define_module_under(VALUE module, const char *name);
void rb_include_module(VALUE module, VALUE to_include);
void rb_define_method(VALUE module, const char *name, void *function, int argc);
void rb_define_private_method(VALUE module, const char *name, void *function, int argc);
void rb_define_protected_method(VALUE module, const char *name, void *function, int argc);
void rb_define_module_function(VALUE module, const char *name, void *function, int argc);
void rb_define_global_function(const char *name, void *function, int argc);
void rb_define_singleton_method(VALUE object, const char *name, void *function, int argc);
void rb_define_alias(VALUE module, const char *new_name, const char *old_name);
void rb_alias(VALUE module, ID new_name, ID old_name);
void rb_undef_method(VALUE module, const char *name);
void rb_undef(VALUE module, ID name);
void rb_attr(VALUE ruby_class, ID name, int read, int write, int ex);
typedef VALUE (*rb_alloc_func_t)(VALUE ruby_class);
void rb_define_alloc_func(VALUE ruby_class, rb_alloc_func_t alloc_function);
VALUE rb_obj_method(VALUE obj, VALUE vid);

// Mutexes

VALUE rb_mutex_new(void);
VALUE rb_mutex_locked_p(VALUE mutex);
VALUE rb_mutex_trylock(VALUE mutex);
VALUE rb_mutex_lock(VALUE mutex);
VALUE rb_mutex_unlock(VALUE mutex);
VALUE rb_mutex_sleep(VALUE mutex, VALUE timeout);
VALUE rb_mutex_synchronize(VALUE mutex, VALUE (*func)(VALUE arg), VALUE arg);

// Rational

VALUE rb_Rational(VALUE num, VALUE den);
#define rb_Rational1(x) rb_Rational((x), INT2FIX(1))
#define rb_Rational2(x,y) rb_Rational((x), (y))
VALUE rb_rational_raw(VALUE num, VALUE den);
#define rb_rational_raw1(x) rb_rational_raw((x), INT2FIX(1))
#define rb_rational_raw2(x,y) rb_rational_raw((x), (y))
VALUE rb_rational_new(VALUE num, VALUE den);
#define rb_rational_new1(x) rb_rational_new((x), INT2FIX(1))
#define rb_rational_new2(x,y) rb_rational_new((x), (y))
VALUE rb_rational_num(VALUE rat);
VALUE rb_rational_den(VALUE rat);
VALUE rb_flt_rationalize_with_prec(VALUE value, VALUE precision);
VALUE rb_flt_rationalize(VALUE value);

// Complex

VALUE rb_Complex(VALUE real, VALUE imag);
#define rb_Complex1(x) rb_Complex((x), INT2FIX(0))
#define rb_Complex2(x,y) rb_Complex((x), (y))
VALUE rb_complex_new(VALUE real, VALUE imag);
#define rb_complex_new1(x) rb_complex_new((x), INT2FIX(0))
#define rb_complex_new2(x,y) rb_complex_new((x), (y))
VALUE rb_complex_raw(VALUE real, VALUE imag);
#define rb_complex_raw1(x) rb_complex_raw((x), INT2FIX(0))
#define rb_complex_raw2(x,y) rb_complex_raw((x), (y))
VALUE rb_complex_polar(VALUE r, VALUE theta);
VALUE rb_complex_set_real(VALUE complex, VALUE real);
VALUE rb_complex_set_imag(VALUE complex, VALUE imag);

// Range

VALUE rb_range_new(VALUE beg, VALUE end, int exclude_end);
int rb_range_values(VALUE range, VALUE *begp, VALUE *endp, int *exclp);

// Time

VALUE rb_time_new(time_t sec, long usec);
VALUE rb_time_nano_new(time_t sec, long nsec);
VALUE rb_time_num_new(VALUE timev, VALUE off);
struct timeval rb_time_interval(VALUE num);
struct timeval rb_time_timeval(VALUE time);
struct timespec rb_time_timespec(VALUE time);
VALUE rb_time_timespec_new(const struct timespec *ts, int offset);
void rb_timespec_now(struct timespec *ts);

// Regexp

VALUE rb_backref_get(void);
VALUE rb_reg_match_pre(VALUE match);
VALUE rb_reg_new(const char *s, long len, int options);
VALUE rb_reg_new_str(VALUE s, int options);
VALUE rb_reg_nth_match(int nth, VALUE match);
VALUE rb_reg_options(VALUE re);
VALUE rb_reg_regcomp(VALUE str);
VALUE rb_reg_match(VALUE re, VALUE str);

// Marshal

VALUE rb_marshal_dump(VALUE obj, VALUE port);
VALUE rb_marshal_load(VALUE port);

// GC

#define RB_GC_GUARD(v) \
    (*__extension__ ({volatile VALUE *rb_gc_guarded_ptr = &(v); rb_gc_guarded_ptr;}))

void rb_gc_register_address(VALUE *address);
#define rb_global_variable(address) ;
VALUE rb_gc_enable();
VALUE rb_gc_disable();

// Threads

typedef void *(*gvl_call)(void *);
typedef void rb_unblock_function_t(void *);
#define RUBY_UBF_IO ((rb_unblock_function_t *)-1)
#define RUBY_UBF_PROCESS ((rb_unblock_function_t *)-1)

void *rb_thread_call_with_gvl(gvl_call function, void *data1);
void *rb_thread_call_without_gvl(gvl_call function, void *data1, rb_unblock_function_t *unblock_function, void *data2);
void *rb_thread_call_without_gvl2(gvl_call function, void *data1, rb_unblock_function_t *unblock_function, void *data2);
int rb_thread_alone(void);
VALUE rb_thread_current(void);
VALUE rb_thread_local_aref(VALUE thread, ID id);
VALUE rb_thread_local_aset(VALUE thread, ID id, VALUE val);
void rb_thread_wait_for(struct timeval time);
VALUE rb_thread_wakeup(VALUE thread);
VALUE rb_thread_create(VALUE (*fn)(ANYARGS), void *arg);

typedef void *rb_nativethread_id_t;
typedef void *rb_nativethread_lock_t;

rb_nativethread_id_t rb_nativethread_self();
int rb_nativethread_lock_initialize(rb_nativethread_lock_t *lock);
int rb_nativethread_lock_destroy(rb_nativethread_lock_t *lock);
int rb_nativethread_lock_lock(rb_nativethread_lock_t *lock);
int rb_nativethread_lock_unlock(rb_nativethread_lock_t *lock);

// IO

typedef struct rb_io_t {
  int fd;
} rb_io_t;

#define rb_update_max_fd(fd) {}
void rb_io_check_writable(rb_io_t *io);
void rb_io_check_readable(rb_io_t *io);
int rb_cloexec_dup(int oldfd);
void rb_fd_fix_cloexec(int fd);
int rb_jt_io_handle(VALUE file);
#define GetOpenFile(file, pointer) ((pointer) = truffle_managed_malloc(sizeof(rb_io_t)), (pointer)->fd = rb_jt_io_handle(file))
int rb_io_wait_readable(int fd);
int rb_io_wait_writable(int fd);
void rb_thread_wait_fd(int fd);
NORETURN(void rb_eof_error(void));
VALUE rb_io_addstr(VALUE io, VALUE str);
VALUE rb_io_check_io(VALUE io);
void rb_io_check_closed(rb_io_t *fptr);
VALUE rb_io_taint_check(VALUE io);
VALUE rb_io_close(VALUE io);
VALUE rb_io_print(int argc, const VALUE *argv, VALUE out);
VALUE rb_io_printf(int argc, const VALUE *argv, VALUE out);
VALUE rb_io_puts(int argc, const VALUE *argv, VALUE out);
VALUE rb_io_write(VALUE io, VALUE str);
VALUE rb_io_binmode(VALUE io);
int rb_thread_fd_writable(int fd);
int rb_cloexec_open(const char *pathname, int flags, mode_t mode);
VALUE rb_file_open(const char *fname, const char *modestr);
VALUE rb_file_open_str(VALUE fname, const char *modestr);
VALUE rb_get_path(VALUE object);
#define FilePathValue(v) (RB_GC_GUARD(v) = rb_get_path(v))

// Structs

VALUE rb_struct_aref(VALUE s, VALUE idx);
VALUE rb_struct_aset(VALUE s, VALUE idx, VALUE val);
VALUE rb_struct_define(const char *name, ...);
VALUE rb_struct_new(VALUE klass, ...);
VALUE rb_struct_getmember(VALUE obj, ID id);

// Objects

struct RBasic {
  // Empty
};

// Data

typedef void (*RUBY_DATA_FUNC)(void *);

struct RData {
  struct RBasic basic;
  RUBY_DATA_FUNC dmark;
  RUBY_DATA_FUNC dfree;
  void *data;
};

VALUE rb_data_object_wrap(VALUE klass, void *datap, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree);

#define Data_Wrap_Struct(klass,mark,free,sval)\
    rb_data_object_wrap((klass),(sval),(RUBY_DATA_FUNC)(mark),(RUBY_DATA_FUNC)(free))

#define Data_Get_Struct(obj,type,sval) \
    ((sval) = (type *)rb_data_object_get(obj))

struct RData *rb_jt_adapt_rdata(VALUE value);
#define RDATA(value) rb_jt_adapt_rdata(value)
#define DATA_PTR(value) (RDATA(value)->data)
#define rb_data_object_get DATA_PTR

// Typed data

typedef struct rb_data_type_struct rb_data_type_t;

struct rb_data_type_struct {
  const char *wrap_struct_name;
  struct {
    RUBY_DATA_FUNC dmark;
    RUBY_DATA_FUNC dfree;
    size_t (*dsize)(const void *data);
    void *reserved[2];
  } function;
  const rb_data_type_t *parent;
  void *data;
  VALUE flags;
};

struct RTypedData {
  struct RBasic basic;
  const rb_data_type_t *type;
  VALUE typed_flag;
  void *data;
};

#define RUBY_TYPED_FREE_IMMEDIATELY 1

#define RTYPEDDATA(value) ((struct RTypedData *)RDATA(value))

#define RTYPEDDATA_DATA(value) (RTYPEDDATA(value)->data)

VALUE rb_data_typed_object_wrap(VALUE ruby_class, void *data, const rb_data_type_t *data_type);

#define TypedData_Wrap_Struct(ruby_class, data_type, data) rb_data_typed_object_wrap((ruby_class), (data), (data_type))

VALUE rb_data_typed_object_zalloc(VALUE ruby_class, size_t size, const rb_data_type_t *data_type);

#define TypedData_Make_Struct0(result, ruby_class, type, size, data_type, sval) \
    VALUE result = rb_data_typed_object_zalloc(ruby_class, size, data_type); \
    (void)((sval) = (type *)DATA_PTR(result));

VALUE rb_data_typed_object_make(VALUE ruby_class, const rb_data_type_t *type, void **data_pointer, size_t size);

#define TypedData_Make_Struct(ruby_class, type, data_type, sval) rb_data_typed_object_make((ruby_class), (data_type), (void **)&(sval), sizeof(type))

void *rb_check_typeddata(VALUE value, const rb_data_type_t *data_type);

#define TypedData_Get_Struct(value, type, data_type, variable) ((variable) = (type *)rb_check_typeddata((value), (data_type)))

// VM

VALUE *rb_ruby_verbose_ptr(void);
#define ruby_verbose (*rb_ruby_verbose_ptr())

VALUE *rb_ruby_debug_ptr(void);
#define ruby_debug (*rb_ruby_debug_ptr())

// Inline implementations

MUST_INLINE VALUE rb_string_value(VALUE *value_pointer) {
  VALUE value = *value_pointer;

  if (!RB_TYPE_P(value, T_STRING)) {
    value = rb_str_to_str(value);
    *value_pointer = value;
  }

  return value;
}

MUST_INLINE char *rb_string_value_ptr(VALUE *value_pointer) {
  VALUE string = rb_string_value(value_pointer);
  return RSTRING_PTR(string);
}

MUST_INLINE char *rb_string_value_cstr(VALUE *value_pointer) {
  VALUE string = rb_string_value(value_pointer);

  if (!truffle_invoke_b(RUBY_CEXT, "rb_string_value_cstr_check", string)) {
    rb_jt_error("rb_string_value_cstr failure case not implemented");
    abort();
  }

  return RSTRING_PTR(string);
}

MUST_INLINE int rb_jt_scan_args_0_hash(int argc, VALUE *argv, const char *format, VALUE *v1) {
  if (argc >= 1) *v1 = argv[0];
  return argc;
}

MUST_INLINE int rb_jt_scan_args_02(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2) {
  if (argc >= 1) *v1 = argv[0];
  if (argc >= 2) *v2 = argv[1];
  return argc;
}

MUST_INLINE int rb_jt_scan_args_11(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2) {
  if (argc < 1) {
    rb_jt_error("rb_jt_scan_args_11 error case not implemented");
    abort();
  }
  *v1 = argv[0];
  if (argc >= 2) *v2 = argv[1];
  return argc - 1;
}

MUST_INLINE int rb_jt_scan_args_12(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2, VALUE *v3) {
  if (argc < 1) {
    rb_jt_error("rb_jt_scan_args_12 error case not implemented");
    abort();
  }
  *v1 = argv[0];
  if (argc >= 2) *v2 = argv[1];
  if (argc >= 3) *v3 = argv[2];
  return argc - 1;
}

MUST_INLINE int rb_jt_scan_args_1_star(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2) {
  if (argc < 1) {
    rb_jt_error("rb_jt_scan_args_1_star error case not implemented");
    abort();
  }
  *v1 = argv[0];
  if (argc >= 2) {
    *v2 = rb_ary_new();
    for (int n = 1; n < argc; n++) {
      rb_ary_push(*v2, argv[n]);
    }
  }
  return argc - 1;
}

MUST_INLINE int rb_jt_scan_args(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2, VALUE *v3, VALUE *v4, VALUE *v5, VALUE *v6, VALUE *v7, VALUE *v8, VALUE *v9, VALUE *v10) {
  // Parse the format string

  // TODO CS 7-Feb-17 maybe we could inline cache this part?

  int required;
  int optional;
  bool rest;

  // TODO CS 27-Feb-17 can LLVM constant-fold through isdigit?

  if (isdigit(*format)) {
    required = *format - '0';

    if (isdigit(*format)) {
      optional = *format - '0';
    }
  }

  rest = *format == '*';

  int argn = 0;
  int valuen = 1; // We've numbered the v parameters from 1
  bool taken_rest = false;

  while (true) {
    // Get the next argument

    VALUE arg;

    if (required > 0 || optional > 0) {
      if (argn < argc) {
        arg = argv[argn];
        argn++;
      } else {
        if (required > 0) {
          rb_jt_error("not enough arguments for required");
          abort();
        } else {
          arg = Qnil;
        }
      }

      if (required > 0) {
        required--;
      } else {
        optional--;
      }
    } else if (rest && !taken_rest) {
      arg = rb_ary_new();
      while (argn < argc) {
        rb_ary_push(arg, argv[argn]);
        argn++;
      }
      taken_rest = true;
    } else {
      break;
    }

    // Put the argument into the current value pointer

    // Don't assign the correct v to a temporary VALUE* and then assign arg to it - this doesn't optimise well

    switch (valuen) {
      case 1: *v1 = arg; break;
      case 2: *v2 = arg; break;
      case 3: *v3 = arg; break;
      case 4: *v4 = arg; break;
      case 5: *v5 = arg; break;
      case 6: *v6 = arg; break;
      case 7: *v7 = arg; break;
      case 8: *v8 = arg; break;
      case 9: *v9 = arg; break;
      case 10: *v10 = arg; break;
    }

    valuen++;
  }

  return argc;
}

#if defined(__cplusplus)
}
#endif

#endif
