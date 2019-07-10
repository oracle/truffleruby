/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * This file contains code that is based on the Ruby API headers and implementation,
 * copyright (C) Yukihiro Matsumoto, licensed under the 2-clause BSD licence
 * as described in the file BSDL included with TruffleRuby.
 */

#include <ruby.h>
#include <ruby/debug.h>
#include <ruby/encoding.h>
#include <ruby/io.h>
#include <ruby/thread_native.h>

#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <printf.h>

/*
 * Note that some functions are #undef'd just before declaration.
 * This is needed because these functions are declared by MRI as macros in e.g., ruby.h,
 * and so would produce invalid syntax when using the function name for definition.
 */

void* rb_tr_cext;
void* (*rb_tr_unwrap)(VALUE obj);
void* (*rb_tr_wrap)(VALUE obj);
void* (*rb_tr_longwrap)(long obj);

#ifdef __APPLE__
static printf_domain_t printf_domain;

static int rb_tr_fprintf_value_arginfo(const struct printf_info *info,
                                       size_t n,
                                       int *argtypes) {
  if (n > 0) {
    *argtypes = PA_POINTER;
  }
  return 1;
}

#else
static int rb_tr_fprintf_value_arginfo(const struct printf_info *info,
                                       size_t n,
                                       int *argtypes, int *argsize) {
  if (n > 0) {
    *argtypes = PA_POINTER;
    *argsize = sizeof(VALUE);
  }
  return 1;
}
#endif

static int rb_tr_fprintf_value(FILE *stream,
                               const struct printf_info *info,
                               const void *const *args) {
  char *cstr = NULL;
  VALUE v;
  int len;

  v = *((const VALUE *) (args[0]));
  if (info->showsign) {
    if (RB_TYPE_P(v, T_CLASS)) {
      if (v == rb_cNilClass) {
        cstr = "nil";
      } else if (v == rb_cTrueClass) {
        cstr = "true";
      } else if (v == rb_cFalseClass) {
        cstr = "false";
      }
    }
    if (cstr == NULL) {
      VALUE str = rb_inspect(v);
      len = rb_str_len(str);
      cstr = RSTRING_PTR(str);
    }
  } else {
    VALUE str = rb_obj_as_string(v);
    len = rb_str_len(str);
    cstr = RSTRING_PTR(str);
  }
  len = fprintf(stream, "%s", cstr);
  return len;
}

// Run when loading C-extension support

void rb_tr_init(void *ruby_cext) {
  rb_tr_cext = ruby_cext;
  rb_tr_unwrap = polyglot_invoke(rb_tr_cext, "rb_tr_unwrap_function");
  rb_tr_wrap = polyglot_invoke(rb_tr_cext, "rb_tr_wrap_function");
  rb_tr_longwrap = polyglot_invoke(rb_tr_cext, "rb_tr_wrap_function");

  #ifdef __APPLE__
  printf_domain = new_printf_domain();
  register_printf_domain_function(printf_domain, 'Y', rb_tr_fprintf_value, rb_tr_fprintf_value_arginfo, NULL);
  #else
  register_printf_specifier('Y', rb_tr_fprintf_value, rb_tr_fprintf_value_arginfo);
  #endif
}

// Private helper macros just for ruby.c

#define rb_boolean(c) ((c) ? Qtrue : Qfalse)

// Helpers

VALUE rb_f_notimplement(int args_count, const VALUE *args, VALUE object) {
  rb_tr_error("rb_f_notimplement");
}

// Memory

void ruby_malloc_size_overflow(size_t count, size_t elsize) {
  rb_raise(rb_eArgError,
     "malloc: possible integer overflow (%"PRIdSIZE"*%"PRIdSIZE")",
     count, elsize);
}

size_t xmalloc2_size(const size_t count, const size_t elsize) {
  size_t ret;
  if (rb_mul_size_overflow(count, elsize, SSIZE_MAX, &ret)) {
    ruby_malloc_size_overflow(count, elsize);
  }
  return ret;
}

void *ruby_xmalloc(size_t size) {
  return malloc(size);
}

void *ruby_xmalloc2(size_t n, size_t size) {
  size_t total_size = xmalloc2_size(n, size);
  if (total_size == 0) {
    total_size = 1;
  }
  return malloc(xmalloc2_size(n, total_size));
}

void *ruby_xcalloc(size_t n, size_t size) {
  return calloc(n, size);
}

void *ruby_xrealloc(void *ptr, size_t new_size) {
  return realloc(ptr, new_size);
}

void *ruby_xrealloc2(void *ptr, size_t n, size_t size) {
  size_t len = size * n;
  if (n != 0 && size != len / n) {
    rb_raise(rb_eArgError, "realloc: possible integer overflow");
  }
  return realloc(ptr, len);
}

void ruby_xfree(void *address) {
  free(address);
}

void *rb_alloc_tmp_buffer(volatile VALUE *store, long len) {
  if (len == 0) {
    len = 1;
  }
  void *ptr = malloc(len);
  *((void**)store) = ptr;
  return ptr;
}

void *rb_alloc_tmp_buffer_with_count(volatile VALUE *store, size_t size, size_t cnt) {
  return rb_alloc_tmp_buffer(store, size);
}

void rb_free_tmp_buffer(volatile VALUE *store) {
  free(*((void**)store));
}

// Types

int rb_type(VALUE value) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_type", value));
}

bool RB_TYPE_P(VALUE value, int type) {
  return polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "RB_TYPE_P", rb_tr_unwrap(value), type));
}

void rb_check_type(VALUE value, int type) {
  polyglot_invoke(RUBY_CEXT, "rb_check_type", rb_tr_unwrap(value), type);
}

VALUE rb_obj_is_instance_of(VALUE object, VALUE ruby_class) {
  return RUBY_CEXT_INVOKE("rb_obj_is_instance_of", object, ruby_class);
}

VALUE rb_obj_is_kind_of(VALUE object, VALUE ruby_class) {
  return RUBY_CEXT_INVOKE("rb_obj_is_kind_of", object, ruby_class);
}

void rb_check_frozen(VALUE object) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_check_frozen", object);
}

void rb_insecure_operation(void) {
  rb_raise(rb_eSecurityError, "Insecure operation: -r");
}

int rb_safe_level(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_safe_level"));
}

void rb_set_safe_level_force(int level) {
  polyglot_invoke(RUBY_CEXT, "rb_set_safe_level_force", level);
}

void rb_set_safe_level(int level) {
  polyglot_invoke(RUBY_CEXT, "rb_set_safe_level", level);
}

void rb_check_safe_obj(VALUE object) {
  if (rb_safe_level() > 0 && OBJ_TAINTED(object)) {
    rb_insecure_operation();
  }
}

VALUE rb_obj_hide(VALUE obj) {
  // In MRI, this deletes the class information which is later set by rb_obj_reveal.
  // It also hides the object from each_object, we do not hide it.
  return obj;
}

VALUE rb_obj_reveal(VALUE obj, VALUE klass) {
  // In MRI, this sets the class of the object, we are not deleting the class in rb_obj_hide, so we
  // ensure that class matches.
  return RUBY_CEXT_INVOKE("ensure_class", obj, klass,
             rb_str_new_cstr("class %s supplied to rb_obj_reveal does not matches the obj's class %s"));
  return obj;
}

// Constants

#include "cext_constants.c"

// Conversions

unsigned long rb_num2ulong(VALUE val) {
  return (unsigned long)polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2ulong", val));
}

static char *out_of_range_float(char (*pbuf)[24], VALUE val) {
  char *const buf = *pbuf;
  char *s;

  snprintf(buf, sizeof(*pbuf), "%-.10g", RFLOAT_VALUE(val));
  if ((s = strchr(buf, ' ')) != 0) {
    *s = '\0';
  }
  return buf;
}

#define FLOAT_OUT_OF_RANGE(val, type) do { \
    char buf[24]; \
    rb_raise(rb_eRangeError, "float %s out of range of "type, \
       out_of_range_float(&buf, (val))); \
} while (0)

#define LLONG_MIN_MINUS_ONE ((double)LLONG_MIN-1)
#define LLONG_MAX_PLUS_ONE (2*(double)(LLONG_MAX/2+1))
#define ULLONG_MAX_PLUS_ONE (2*(double)(ULLONG_MAX/2+1))
#define LLONG_MIN_MINUS_ONE_IS_LESS_THAN(n) \
  (LLONG_MIN_MINUS_ONE == (double)LLONG_MIN ? \
   LLONG_MIN <= (n): \
   LLONG_MIN_MINUS_ONE < (n))

LONG_LONG rb_num2ll(VALUE val) {
  if (NIL_P(val)) {
    rb_raise(rb_eTypeError, "no implicit conversion from nil");
  }

  if (FIXNUM_P(val)) {
    return (LONG_LONG)FIX2LONG(val);
  } else if (RB_TYPE_P(val, T_FLOAT)) {
    if (RFLOAT_VALUE(val) < LLONG_MAX_PLUS_ONE
        && (LLONG_MIN_MINUS_ONE_IS_LESS_THAN(RFLOAT_VALUE(val)))) {
      return (LONG_LONG)(RFLOAT_VALUE(val));
    } else {
      FLOAT_OUT_OF_RANGE(val, "long long");
    }
  }
  else if (RB_TYPE_P(val, T_BIGNUM)) {
    return rb_big2ll(val);
  } else if (RB_TYPE_P(val, T_STRING)) {
    rb_raise(rb_eTypeError, "no implicit conversion from string");
  } else if (RB_TYPE_P(val, T_TRUE) || RB_TYPE_P(val, T_FALSE)) {
    rb_raise(rb_eTypeError, "no implicit conversion from boolean");
  }

  val = rb_to_int(val);
  return NUM2LL(val);
}

unsigned LONG_LONG rb_num2ull(VALUE val) {
  if (NIL_P(val)) {
    rb_raise(rb_eTypeError, "no implicit conversion from nil");
  }

  if (FIXNUM_P(val)) {
    return (unsigned LONG_LONG)FIX2ULONG(val);
  } else if (RB_TYPE_P(val, T_FLOAT)) {
    if (RFLOAT_VALUE(val) <= ULLONG_MAX
        && (RFLOAT_VALUE(val) >= 0)) {
      return (unsigned LONG_LONG)(RFLOAT_VALUE(val));
    } else {
      FLOAT_OUT_OF_RANGE(val, "unsigned long long");
    }
  }
  else if (RB_TYPE_P(val, T_BIGNUM)) {
    return rb_big2ull(val);
  } else if (RB_TYPE_P(val, T_STRING)) {
    rb_raise(rb_eTypeError, "no implicit conversion from string");
  } else if (RB_TYPE_P(val, T_TRUE) || RB_TYPE_P(val, T_FALSE)) {
    rb_raise(rb_eTypeError, "no implicit conversion from boolean");
  }

  val = rb_to_int(val);
  return NUM2ULL(val);
}

short rb_num2short(VALUE value) {
  rb_tr_error("rb_num2ushort not implemented");
}

unsigned short rb_num2ushort(VALUE value) {
  rb_tr_error("rb_num2ushort not implemented");
}

short rb_fix2short(VALUE value) {
  rb_tr_error("rb_num2ushort not implemented");
}

long rb_fix2int(VALUE value) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_fix2int", value));
}

unsigned long rb_fix2uint(VALUE value) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_fix2uint", value));
}

int rb_long2int(long value) {
  return polyglot_as_i64(polyglot_invoke(RUBY_CEXT, "rb_long2int", value));
}

int rb_cmpint(VALUE val, VALUE a, VALUE b) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_cmpint", val, a, b));
}

VALUE rb_int2inum(intptr_t n) {
  return LONG2NUM(n);
}

VALUE rb_uint2inum(uintptr_t n) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_ulong2num", n));
}

VALUE rb_ll2inum(LONG_LONG n) {
  /* Long and long long are both 64-bits with clang x86-64. */
  return LONG2NUM(n);
}

VALUE rb_ull2inum(unsigned LONG_LONG val) {
  /* Long and long long are both 64-bits with clang x86-64. */
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_ulong2num", val));
}

double rb_num2dbl(VALUE val) {
  return polyglot_as_double(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2dbl", val));
}

long rb_num2int(VALUE val) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2int", val));
}

unsigned long rb_num2uint(VALUE val) {
  return (unsigned long)polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2uint", val));
}

long rb_num2long(VALUE val) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2long", val));
}

VALUE rb_num_coerce_bin(VALUE x, VALUE y, ID func) {
  return RUBY_CEXT_INVOKE("rb_num_coerce_bin", x, y, ID2SYM(func));
}

VALUE rb_num_coerce_cmp(VALUE x, VALUE y, ID func) {
  return RUBY_CEXT_INVOKE("rb_num_coerce_cmp", x, y, ID2SYM(func));
}

VALUE rb_num_coerce_relop(VALUE x, VALUE y, ID func) {
  return RUBY_CEXT_INVOKE("rb_num_coerce_relop", x, y, ID2SYM(func));
}

void rb_num_zerodiv(void) {
  rb_raise(rb_eZeroDivError, "divided by 0");
}

// Type checks

int RB_FIXNUM_P(VALUE value) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("RB_FIXNUM_P", value));
}

int RB_FLOAT_TYPE_P(VALUE value) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("RB_FLOAT_TYPE_P", value));
}

int RTEST(VALUE value) {
  return value != NULL && polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("RTEST", value));
}

// Kernel

void rb_p(VALUE obj) {
  RUBY_INVOKE_NO_WRAP(rb_mKernel, "p", obj);
}

VALUE rb_require(const char *feature) {
  return RUBY_CEXT_INVOKE("rb_require", rb_str_new_cstr(feature));
}

VALUE rb_eval_string(const char *str) {
  return RUBY_CEXT_INVOKE("rb_eval_string", rb_str_new_cstr(str));
}

VALUE rb_exec_recursive(VALUE (*func) (VALUE, VALUE, int), VALUE obj, VALUE arg) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_exec_recursive", func, rb_tr_unwrap(obj), rb_tr_unwrap(arg)));
}

VALUE rb_f_sprintf(int argc, const VALUE *argv) {
  return RUBY_CEXT_INVOKE("rb_f_sprintf", rb_ary_new4(argc, argv));
}

VALUE rb_yield_block(VALUE val, VALUE arg, int argc, const VALUE *argv, VALUE blockarg) {
  rb_tr_error("rb_yield_block not implemented");
}

int ruby_snprintf(char *str, size_t n, char const *fmt, ...) {
  rb_tr_error("ruby_snprintf not implemented");
}

#undef vsnprintf
int ruby_vsnprintf(char *str, size_t n, char const *fmt, va_list ap) {
  return vsnprintf(str, n, fmt, ap);
}

void rb_need_block(void) {
  if (!rb_block_given_p()) {
    rb_raise(rb_eLocalJumpError, "no block given");
  }
}

void rb_set_end_proc(void (*func)(VALUE), VALUE data) {
  rb_tr_error("rb_set_end_proc not implemented");
}

void rb_iter_break(void) {
  rb_iter_break_value(Qnil);
}

void rb_iter_break_value(VALUE value) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_iter_break_value", value);
  rb_tr_error("rb_iter_break_value should not return");
}

const char *rb_sourcefile(void) {
  return RSTRING_PTR(RUBY_CEXT_INVOKE("rb_sourcefile"));
}

int rb_sourceline(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_sourceline"));
}

int rb_method_boundp(VALUE klass, ID id, int ex) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_method_boundp", rb_tr_unwrap(klass), rb_tr_unwrap(id), ex));
}

// Object

VALUE rb_obj_dup(VALUE object) {
  return RUBY_INVOKE(object, "dup");
}

VALUE rb_obj_as_string(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_obj_as_string", object);
}

VALUE rb_any_to_s(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_any_to_s", object);
}

VALUE rb_obj_instance_variables(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_obj_instance_variables", object);
}

VALUE rb_check_convert_type(VALUE val, int type, const char *type_name, const char *method) {
  return RUBY_CEXT_INVOKE("rb_check_convert_type", val, rb_str_new_cstr(type_name), rb_str_new_cstr(method));
}

VALUE rb_check_to_integer(VALUE object, const char *method) {
  return RUBY_CEXT_INVOKE("rb_check_to_integer", object, rb_str_new_cstr(method));
}

VALUE rb_check_string_type(VALUE object) {
  return rb_check_convert_type(object, T_STRING, "String", "to_str");
}

VALUE rb_convert_type(VALUE object, int type, const char *type_name, const char *method) {
  return RUBY_CEXT_INVOKE("rb_convert_type", object, rb_str_new_cstr(type_name), rb_str_new_cstr(method));
}

void rb_extend_object(VALUE object, VALUE module) {
  RUBY_INVOKE(module, "extend_object", object);
}

VALUE rb_inspect(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_inspect", object);
}

void rb_obj_call_init(VALUE object, int argc, const VALUE *argv) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_obj_call_init", object, rb_ary_new4(argc, argv));
}

const char *rb_obj_classname(VALUE object) {
  VALUE str = RUBY_CEXT_INVOKE("rb_obj_classname", object);
  if (str != Qnil) {
    return RSTRING_PTR(str);
  } else {
    return NULL;
  }
}

VALUE rb_obj_id(VALUE object) {
  return RUBY_INVOKE(object, "object_id");
}

void rb_tr_object_hidden_var_set(VALUE object, const char *name, VALUE value) {
  RUBY_CEXT_INVOKE_NO_WRAP("hidden_variable_set", object, rb_intern(name), value);
}

VALUE rb_tr_object_hidden_var_get(VALUE object, const char *name) {
  return RUBY_CEXT_INVOKE_NO_WRAP("hidden_variable_get", object, rb_intern(name));
}

int rb_obj_method_arity(VALUE object, ID id) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_obj_method_arity", object, id));
}

int rb_obj_respond_to(VALUE object, ID id, int priv) {
  return polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "rb_obj_respond_to", rb_tr_unwrap(object), rb_tr_unwrap(id), priv));
}

int rb_special_const_p(VALUE object) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_special_const_p", object));
}

VALUE rb_to_int(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_to_int", object);
}

VALUE rb_obj_instance_eval(int argc, const VALUE *argv, VALUE self) {
  return RUBY_CEXT_INVOKE("rb_obj_instance_eval", self, rb_ary_new4(argc, argv), rb_block_proc());
}

VALUE rb_ivar_defined(VALUE object, ID id) {
  return RUBY_CEXT_INVOKE("rb_ivar_defined", object, id);
}

VALUE rb_equal_opt(VALUE a, VALUE b) {
  rb_tr_error("rb_equal_opt not implemented");
}

VALUE rb_class_inherited_p(VALUE module, VALUE object) {
  return RUBY_CEXT_INVOKE("rb_class_inherited_p", module, object);
}

VALUE rb_equal(VALUE a, VALUE b) {
  return RUBY_INVOKE(a, "===", b);
}

VALUE rb_obj_taint(VALUE object) {
  return RUBY_INVOKE(object, "taint");
}

bool rb_tr_obj_taintable_p(VALUE object) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("RB_OBJ_TAINTABLE", object));
}

bool rb_tr_obj_tainted_p(VALUE object) {
  return polyglot_as_boolean(RUBY_INVOKE_NO_WRAP(object, "tainted?"));
}

void rb_tr_obj_infect(VALUE a, VALUE b) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_tr_obj_infect", a, b);
}

VALUE rb_obj_freeze(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_obj_freeze", object);
}

VALUE rb_obj_frozen_p(VALUE object) {
  return RUBY_INVOKE(object, "frozen?");
}

// Integer

VALUE rb_Integer(VALUE value) {
  return RUBY_CEXT_INVOKE("rb_Integer", value);
}

#define INTEGER_PACK_WORDORDER_MASK \
  (INTEGER_PACK_MSWORD_FIRST | \
   INTEGER_PACK_LSWORD_FIRST)
#define INTEGER_PACK_BYTEORDER_MASK \
  (INTEGER_PACK_MSBYTE_FIRST | \
   INTEGER_PACK_LSBYTE_FIRST | \
   INTEGER_PACK_NATIVE_BYTE_ORDER)
#define INTEGER_PACK_SUPPORTED_FLAG \
  (INTEGER_PACK_MSWORD_FIRST | \
   INTEGER_PACK_LSWORD_FIRST | \
   INTEGER_PACK_MSBYTE_FIRST | \
   INTEGER_PACK_LSBYTE_FIRST | \
   INTEGER_PACK_NATIVE_BYTE_ORDER | \
   INTEGER_PACK_2COMP | \
   INTEGER_PACK_FORCE_GENERIC_IMPLEMENTATION)

static void validate_integer_pack_format(size_t numwords, size_t wordsize, size_t nails, int flags, int supported_flags) {
  int wordorder_bits = flags & INTEGER_PACK_WORDORDER_MASK;
  int byteorder_bits = flags & INTEGER_PACK_BYTEORDER_MASK;

  if (flags & ~supported_flags) {
    rb_raise(rb_eArgError, "unsupported flags specified");
  }

  if (wordorder_bits == 0) {
    if (1 < numwords) {
      rb_raise(rb_eArgError, "word order not specified");
    }
  } else if (wordorder_bits != INTEGER_PACK_MSWORD_FIRST &&
      wordorder_bits != INTEGER_PACK_LSWORD_FIRST) {
    rb_raise(rb_eArgError, "unexpected word order");
  }

  if (byteorder_bits == 0) {
    rb_raise(rb_eArgError, "byte order not specified");
  } else if (byteorder_bits != INTEGER_PACK_MSBYTE_FIRST &&
    byteorder_bits != INTEGER_PACK_LSBYTE_FIRST &&
    byteorder_bits != INTEGER_PACK_NATIVE_BYTE_ORDER) {
      rb_raise(rb_eArgError, "unexpected byte order");
  }

  if (wordsize == 0) {
    rb_raise(rb_eArgError, "invalid wordsize: %lu", wordsize);
  }

  if (8 < wordsize) {
    rb_raise(rb_eArgError, "too big wordsize: %lu", wordsize);
  }

  if (wordsize <= nails / CHAR_BIT) {
    rb_raise(rb_eArgError, "too big nails: %lu", nails);
  }

  if (INT_MAX / wordsize < numwords) {
    rb_raise(rb_eArgError, "too big numwords * wordsize: %lu * %lu", numwords, wordsize);
  }
}

static int check_msw_first(int flags) {
  return flags & INTEGER_PACK_MSWORD_FIRST;
}

static int endian_swap(int flags) {
  return flags & INTEGER_PACK_MSBYTE_FIRST;
}

int rb_integer_pack(VALUE value, void *words, size_t numwords, size_t wordsize, size_t nails, int flags) {
  long i;
  VALUE msw_first, twosComp, swap, bytes;
  int sign, size, bytes_needed, words_needed, result;
  uint8_t *buf;
  msw_first = rb_boolean(check_msw_first(flags));
  twosComp = rb_boolean(((flags & INTEGER_PACK_2COMP) != 0));
  swap = rb_boolean(endian_swap(flags));
  // Test for fixnum and do the right things here.
  bytes = polyglot_invoke(RUBY_CEXT, "rb_integer_bytes", rb_tr_unwrap(value),
                          (int)numwords, (int)wordsize, rb_tr_unwrap(msw_first), rb_tr_unwrap(twosComp), rb_tr_unwrap(swap));
  size = (twosComp == Qtrue) ? polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_2scomp_bit_length", value))
    : polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_absint_bit_length", value));
  if (RB_FIXNUM_P(value)) {
    long l = NUM2LONG(value);
    sign = (l > 0) - (l < 0);
  } else {
    sign = polyglot_as_i32(polyglot_invoke(rb_tr_unwrap(value), "<=>", 0));
  }
  bytes_needed = size / 8 + (size % 8 == 0 ? 0 : 1);
  words_needed = bytes_needed / wordsize + (bytes_needed % wordsize == 0 ? 0 : 1);
  result = (words_needed <= numwords ? 1 : 2) * sign;

  buf = (uint8_t *)words;
  for (i = 0; i < numwords * wordsize; i++) {
    buf[i] = (uint8_t) polyglot_as_i32(polyglot_get_array_element(bytes, i));
  }
  return result;
}

VALUE rb_integer_unpack(const void *words, size_t numwords, size_t wordsize, size_t nails, int flags) {
  rb_tr_error("rb_integer_unpack not implemented");
}

size_t rb_absint_size(VALUE value, int *nlz_bits_ret) {
  int size = polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_absint_bit_length", value));
  if (nlz_bits_ret != NULL) {
    *nlz_bits_ret = size % 8;
  }
  int bytes = size / 8;
  if (size % 8 > 0) {
    bytes++;
  }
  return bytes;
}

VALUE rb_cstr_to_inum(const char* string, int base, int raise) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_cstr_to_inum", rb_tr_unwrap(rb_str_new_cstr(string)), base, raise));
}

double rb_cstr_to_dbl(const char* string, int badcheck) {
  return polyglot_as_double(RUBY_CEXT_INVOKE_NO_WRAP("rb_cstr_to_dbl", rb_str_new_cstr(string), rb_boolean(badcheck)));
}

double rb_big2dbl(VALUE x) {
  return polyglot_as_double(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2dbl", x));
}

VALUE rb_dbl2big(double d) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "DBL2BIG", d));
}

LONG_LONG rb_big2ll(VALUE x) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2long", x));
}

unsigned LONG_LONG rb_big2ull(VALUE x) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2ulong", x));
}

long rb_big2long(VALUE x) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2long", x));
}

VALUE rb_big2str(VALUE x, int base) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap((void *)x), "to_s", base));
}

unsigned long rb_big2ulong(VALUE x) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_num2ulong", x));
}

int rb_big_sign(VALUE x) {
  return RTEST(RUBY_INVOKE(x, ">=", INT2FIX(0))) ? 1 : 0;
}

VALUE rb_big_cmp(VALUE x, VALUE y) {
  return RUBY_INVOKE(x, "<=>", y);
}

void rb_big_pack(VALUE val, unsigned long *buf, long num_longs) {
  rb_integer_pack(val, buf, num_longs, 8, 0,
                  INTEGER_PACK_2COMP | INTEGER_PACK_NATIVE_BYTE_ORDER | INTEGER_PACK_LSWORD_FIRST);
}

// Float

VALUE rb_float_new(double value) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_float_new", value));
}

VALUE rb_Float(VALUE value) {
  return RUBY_CEXT_INVOKE("rb_Float", value);
}

double rb_float_value(VALUE value) {
  return polyglot_as_double(RUBY_CEXT_INVOKE_NO_WRAP("RFLOAT_VALUE", value));
}

// String

char *RSTRING_PTR_IMPL(VALUE string) {
  return RUBY_CEXT_INVOKE_NO_WRAP("RSTRING_PTR", string);
}

char *RSTRING_END(VALUE string) {
  return RUBY_CEXT_INVOKE_NO_WRAP("RSTRING_END", string);
}

int MBCLEN_NEEDMORE_P(int r) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "MBCLEN_NEEDMORE_P", r));
}

int MBCLEN_NEEDMORE_LEN(int r) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "MBCLEN_NEEDMORE_LEN", r));
}

int MBCLEN_CHARFOUND_P(int r) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "MBCLEN_CHARFOUND_P", r));
}

int MBCLEN_CHARFOUND_LEN(int r) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "MBCLEN_CHARFOUND_LEN", r));
}

int rb_str_len(VALUE string) {
  return polyglot_as_i32(polyglot_invoke(rb_tr_unwrap((void *)string), "bytesize"));
}

bool is_rstring_ptr(VALUE ptr) {
  return polyglot_is_value(ptr);
}

bool is_managed_rstring_ptr(VALUE ptr) {
  return is_rstring_ptr(ptr) &&
    !polyglot_as_boolean(polyglot_invoke(ptr, "native?"));
}

VALUE rb_str_new(const char *string, long length) {
  if (length < 0) {
    rb_raise(rb_eArgError, "negative string size (or size too big)");
  }

  if (string == NULL) {
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_new_nul", length));
  } else if (is_managed_rstring_ptr((VALUE) string)) {
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_new_rstring_ptr", string, length));
  } else {
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_new_native", string, length));
  }
}

VALUE rb_tainted_str_new(const char *ptr, long len) {
    VALUE str = rb_str_new(ptr, len);

    OBJ_TAINT(str);
    return str;
}

VALUE rb_str_new_cstr(const char *string) {
  // TODO CS 24-Oct-17 would be nice to read in one go rather than strlen followed by read
  return rb_str_new(string, strlen(string));
}

VALUE rb_str_new_shared(VALUE string) {
  return RUBY_INVOKE(string, "dup");
}

VALUE rb_str_new_with_class(VALUE str, const char *string, long len) {
  return RUBY_INVOKE(RUBY_INVOKE(str, "class"), "new", rb_str_new(string, len));
}

VALUE rb_tainted_str_new_cstr(const char *ptr) {
    VALUE str = rb_str_new_cstr(ptr);

    OBJ_TAINT(str);
    return str;
}

ID rb_intern_str(VALUE string) {
  return RUBY_CEXT_INVOKE("rb_intern_str", string);
}

VALUE rb_str_cat(VALUE string, const char *to_concat, long length) {
  if (length == 0) {
    return string;
  }
  if (length < 0) {
	rb_raise(rb_eArgError, "negative string size (or size too big)");
  }
  int old_length = RSTRING_LEN(string);
  rb_str_resize(string, old_length + length);
  // Resizing the string will clear out the code range, so there is no
  // need to do it explicitly.
  memcpy(RSTRING_PTR(string) + old_length, to_concat, length);
  return string;
}

VALUE rb_str_cat2(VALUE string, const char *to_concat) {
  return rb_str_cat(string, to_concat, strlen(to_concat));
}

VALUE rb_str_to_str(VALUE string) {
  return rb_convert_type(string, T_STRING, "String", "to_str");
}

VALUE rb_str_buf_new(long capacity) {
  VALUE str = rb_str_new(NULL, capacity);
  rb_str_set_len(str, 0);
  return str;
}

VALUE rb_sprintf(const char *format, ...) {
    VALUE result;
    va_list ap;

    va_start(ap, format);
    result = rb_vsprintf(format, ap);
    va_end(ap);

    return result;
}

VALUE rb_vsprintf(const char *format, va_list args) {
  return rb_enc_vsprintf(rb_ascii8bit_encoding(), format, args);
}

VALUE rb_str_append(VALUE string, VALUE to_append) {
  return RUBY_CEXT_INVOKE("rb_str_append", string, to_append);
}

VALUE rb_str_concat(VALUE string, VALUE to_concat) {
  return RUBY_CEXT_INVOKE("rb_str_concat", string, to_concat);
}

void rb_str_set_len(VALUE string, long length) {
  long capacity = rb_str_capacity(string);
  if (length > capacity || length < 0) {
    rb_raise(rb_eRuntimeError, "probable buffer overflow: %ld for %ld", length, capacity);
  }
  rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_set_len", rb_tr_unwrap(string), length));
}

VALUE rb_str_new_frozen(VALUE value) {
  return RUBY_CEXT_INVOKE("rb_str_new_frozen", value);
}

VALUE rb_String(VALUE value) {
  return RUBY_CEXT_INVOKE("rb_String", value);
}

VALUE rb_str_resize(VALUE string, long length) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_resize", rb_tr_unwrap(string), length));
}

VALUE rb_str_split(VALUE string, const char *split) {
  return RUBY_INVOKE(string, "split", rb_str_new_cstr(split));
}

void rb_str_modify(VALUE string) {
  ENC_CODERANGE_CLEAR(string);
}

VALUE rb_cstr2inum(const char *string, int base) {
  return rb_cstr_to_inum(string, base, base==0);
}

VALUE rb_str_to_inum(VALUE str, int base, int badcheck) {
  char *s;
  StringValue(str);
  rb_must_asciicompat(str);
  if (badcheck) {
    s = StringValueCStr(str);
  } else {
    s = RSTRING_PTR(str);
  }
  return rb_cstr_to_inum(s, base, badcheck);
}

VALUE rb_str2inum(VALUE string, int base) {
  return rb_str_to_inum(string, base, base==0);
}

VALUE rb_str_buf_new_cstr(const char *string) {
  return rb_str_new_cstr(string);
}

int rb_str_cmp(VALUE a, VALUE b) {
  return polyglot_as_i32(RUBY_INVOKE_NO_WRAP(a, "<=>", b));
}

VALUE rb_str_buf_cat(VALUE string, const char *to_concat, long length) {
  return rb_str_cat(string, to_concat, length);
}

POLYGLOT_DECLARE_STRUCT(rb_encoding)

// returns Truffle::CExt::RbEncoding, takes Encoding or String
rb_encoding *rb_to_encoding(VALUE encoding) {
  return polyglot_as_rb_encoding(RUBY_CEXT_INVOKE_NO_WRAP("rb_to_encoding", encoding));
}

VALUE rb_str_conv_enc(VALUE string, rb_encoding *from, rb_encoding *to) {
  return rb_str_conv_enc_opts(string, from, to, 0, Qnil);
}

VALUE rb_str_conv_enc_opts(VALUE str, rb_encoding *from, rb_encoding *to, int ecflags, VALUE ecopts) {
  if (!to) return str;
  if (!from) from = rb_enc_get(str);
  if (from == to) return str;
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_conv_enc_opts", rb_tr_unwrap(str), rb_tr_unwrap(rb_enc_from_encoding(from)), rb_tr_unwrap(rb_enc_from_encoding(to)), ecflags, rb_tr_unwrap(ecopts)));
}

VALUE
rb_tainted_str_new_with_enc(const char *ptr, long len, rb_encoding *enc) {
  VALUE str = rb_enc_str_new(ptr, len, enc);
  OBJ_TAINT(str);
  return str;
}

VALUE rb_external_str_new_with_enc(const char *ptr, long len, rb_encoding *eenc) {
  VALUE str;
  str = rb_tainted_str_new_with_enc(ptr, len, eenc);
  str = rb_external_str_with_enc(str, eenc);
  return str;
}

VALUE rb_external_str_with_enc(VALUE str, rb_encoding *eenc) {
  if (polyglot_as_boolean(RUBY_INVOKE_NO_WRAP(rb_enc_from_encoding(eenc), "==", rb_enc_from_encoding(rb_usascii_encoding()))) &&
    rb_enc_str_coderange(str) != ENC_CODERANGE_7BIT) {
    rb_enc_associate_index(str, rb_ascii8bit_encindex());
    return str;
  }
  rb_enc_associate(str, eenc);
  return rb_str_conv_enc(str, eenc, rb_default_internal_encoding());
}

VALUE rb_external_str_new(const char *string, long len) {
  return rb_external_str_new_with_enc(string, len, rb_default_external_encoding());
}

VALUE rb_external_str_new_cstr(const char *string) {
  return rb_external_str_new_with_enc(string, strlen(string), rb_default_external_encoding());
}

VALUE rb_locale_str_new(const char *string, long len) {
  return rb_external_str_new_with_enc(string, len, rb_locale_encoding());
}

VALUE rb_locale_str_new_cstr(const char *string) {
  return rb_external_str_new_with_enc(string, strlen(string), rb_locale_encoding());
}

VALUE rb_filesystem_str_new(const char *string, long len) {
  return rb_external_str_new_with_enc(string, len, rb_filesystem_encoding());
}

VALUE rb_filesystem_str_new_cstr(const char *string) {
  return rb_external_str_new_with_enc(string, strlen(string), rb_filesystem_encoding());
}

VALUE rb_str_export(VALUE string) {
  return rb_str_conv_enc(string, STR_ENC_GET(string), rb_default_external_encoding());
}

VALUE rb_str_export_locale(VALUE string) {
  return rb_str_conv_enc(string, STR_ENC_GET(string), rb_locale_encoding());
}

VALUE rb_str_export_to_enc(VALUE string, rb_encoding *enc) {
  return rb_str_conv_enc(string, STR_ENC_GET(string), enc);
}

rb_encoding *rb_default_external_encoding(void) {
  VALUE result = RUBY_CEXT_INVOKE("rb_default_external_encoding");
  if (NIL_P(result)) {
    return NULL;
  }
  return rb_to_encoding(result);
}

rb_encoding *rb_default_internal_encoding(void) {
  VALUE result = RUBY_CEXT_INVOKE("rb_default_internal_encoding");
  if (NIL_P(result)) {
    return NULL;
  }
  return rb_to_encoding(result);
}

rb_encoding *rb_locale_encoding(void) {
  VALUE result = RUBY_CEXT_INVOKE("rb_locale_encoding");
  if (NIL_P(result)) {
    return NULL;
  }
  return rb_to_encoding(result);
}

int rb_locale_encindex(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_locale_encindex"));
}

rb_encoding *rb_filesystem_encoding(void) {
  VALUE result = RUBY_CEXT_INVOKE("rb_filesystem_encoding");
  if (NIL_P(result)) {
    return NULL;
  }
  return rb_to_encoding(result);
}

int rb_filesystem_encindex(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_filesystem_encindex"));
}

rb_encoding *get_encoding(VALUE string) {
  return rb_to_encoding(RUBY_INVOKE(string, "encoding"));
}

VALUE rb_str_intern(VALUE string) {
  return RUBY_INVOKE(string, "intern");
}

VALUE rb_str_length(VALUE string) {
  return RUBY_INVOKE(string, "length");
}

VALUE rb_str_plus(VALUE a, VALUE b) {
  return RUBY_INVOKE(a, "+", b);
}

VALUE rb_str_subseq(VALUE string, long beg, long len) {
    return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(string), "byteslice", beg, len));
}

VALUE rb_str_substr(VALUE string, long beg, long len) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(string), "[]", beg, len));
}

st_index_t rb_str_hash(VALUE string) {
  return (st_index_t) polyglot_as_i64(polyglot_invoke(rb_tr_unwrap(string), "hash"));
}

void rb_str_update(VALUE string, long beg, long len, VALUE value) {
  polyglot_invoke(rb_tr_unwrap(string), "[]=", beg, len, rb_tr_unwrap(value));
}

VALUE rb_str_replace(VALUE str, VALUE by) {
  return RUBY_INVOKE(str, "replace", by);
}

VALUE rb_str_equal(VALUE a, VALUE b) {
  return RUBY_INVOKE(a, "==", b);
}

void rb_str_free(VALUE string) {
//  intentional noop here
}

unsigned int rb_enc_codepoint_len(const char *p, const char *e, int *len_p, rb_encoding *encoding) {
  int len = e - p;
  if (len <= 0) {
    rb_raise(rb_eArgError, "empty string");
  }
  VALUE array = RUBY_CEXT_INVOKE("rb_enc_codepoint_len", rb_str_new(p, len), rb_enc_from_encoding(encoding));
  if (len_p) *len_p = polyglot_as_i32(polyglot_invoke(rb_tr_unwrap(array), "[]", 0));
  return (unsigned int)polyglot_as_i32(polyglot_invoke(rb_tr_unwrap(array), "[]", 1));
}

rb_encoding *rb_enc_get(VALUE object) {
  return rb_to_encoding(RUBY_CEXT_INVOKE("rb_enc_get", object));
}

void rb_enc_set_index(VALUE obj, int idx) {
  polyglot_invoke(RUBY_CEXT, "rb_enc_set_index", rb_tr_unwrap(obj), idx);
}

rb_encoding *rb_ascii8bit_encoding(void) {
  return rb_to_encoding(RUBY_CEXT_INVOKE("ascii8bit_encoding"));
}

int rb_ascii8bit_encindex(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_ascii8bit_encindex"));
}

rb_encoding *rb_usascii_encoding(void) {
  return rb_to_encoding(RUBY_CEXT_INVOKE("usascii_encoding"));
}

int rb_enc_asciicompat(rb_encoding *enc) {
  return polyglot_as_boolean(RUBY_INVOKE_NO_WRAP(rb_enc_from_encoding(enc), "ascii_compatible?"));
}

void rb_must_asciicompat(VALUE str) {
  rb_encoding *enc = rb_enc_get(str);
  if (!rb_enc_asciicompat(enc)) {
    rb_raise(rb_eEncCompatError, "ASCII incompatible encoding: %s", rb_enc_name(enc));
  }
}

int rb_usascii_encindex(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_usascii_encindex"));
}

rb_encoding *rb_utf8_encoding(void) {
  return rb_to_encoding(RUBY_CEXT_INVOKE("utf8_encoding"));
}

int rb_utf8_encindex(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_utf8_encindex"));
}

enum ruby_coderange_type RB_ENC_CODERANGE(VALUE obj) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("RB_ENC_CODERANGE", obj));
}

void rb_enc_coderange_clear(VALUE obj) {
  RUBY_CEXT_INVOKE("rb_enc_coderange_clear", obj);
}

int rb_encdb_alias(const char *alias, const char *orig) {
  rb_tr_error("rb_encdb_alias not implemented");
}

int rb_enc_alias(const char *alias, const char *orig) {
  rb_tr_error("rb_enc_alias not implemented");
}

VALUE rb_enc_associate(VALUE obj, rb_encoding *enc) {
  return rb_enc_associate_index(obj, rb_enc_to_index(enc));
}

VALUE rb_enc_associate_index(VALUE obj, int idx) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enc_associate_index", rb_tr_unwrap(obj), idx));
}

rb_encoding* rb_enc_compatible(VALUE str1, VALUE str2) {
  VALUE result = RUBY_INVOKE(rb_cEncoding, "compatible?", str1, str2);
  if (!NIL_P(result)) {
    return rb_to_encoding(result);
  }
  return NULL;
}

void rb_enc_copy(VALUE obj1, VALUE obj2) {
  rb_enc_associate_index(obj1, rb_enc_get_index(obj2));
}

int rb_enc_find_index(const char *name) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_enc_find_index", rb_str_new_cstr(name)));
}

rb_encoding *rb_enc_find(const char *name) {
  int idx = rb_enc_find_index(name);
  if (idx < 0) idx = 0;
  return rb_enc_from_index(idx);
}

// returns Encoding, takes rb_encoding struct or RbEncoding
VALUE rb_enc_from_encoding(rb_encoding *encoding) {
  if (polyglot_is_value(encoding)) {
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enc_from_encoding", encoding));
  } else {
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enc_from_native_encoding", (long)encoding));
  }
}

rb_encoding *rb_enc_from_index(int index) {
  return rb_to_encoding(rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enc_from_index", index)));
}

void RB_ENC_CODERANGE_SET(VALUE obj, int cr) {
  rb_tr_error("RB_ENC_CODERANGE_SET not implemented");
}

int rb_enc_mbc_to_codepoint(char *p, char *e, rb_encoding *enc) {
  rb_tr_error("rb_enc_mbc_to_codepoint not implemented");
}

int rb_enc_str_coderange(VALUE str) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_enc_str_coderange", str));
}

int rb_tr_obj_equal(VALUE first, VALUE second) {
  return RTEST(rb_funcall(first, rb_intern("equal?"), 1, second));
}

int rb_tr_flags(VALUE value) {
  int flags = 0;
  if (OBJ_FROZEN(value)) {
    flags |= RUBY_FL_FREEZE;
  }
  if (OBJ_TAINTED(value)) {
    flags |= RUBY_FL_TAINT;
  }
  if (RARRAY_LEN(rb_obj_instance_variables(value)) > 0) {
    flags |= RUBY_FL_EXIVAR;
  }
  // TODO BJF Nov-11-2017 Implement more flags
  return flags;
}

void rb_tr_add_flags(VALUE value, int flags) {
  if (flags & RUBY_FL_TAINT) {
    rb_obj_taint(value);
  }
  if (flags & RUBY_FL_FREEZE) {
    rb_obj_freeze(value);
  }
}

bool rb_tr_hidden_p(VALUE value) {
  return false;
}

#undef rb_enc_str_new
VALUE rb_enc_str_new(const char *ptr, long len, rb_encoding *enc) {
  return RUBY_INVOKE(rb_str_new(ptr, len), "force_encoding", rb_enc_from_encoding(enc));
}

void rb_enc_raise(rb_encoding *enc, VALUE exc, const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    VALUE mesg = rb_vsprintf(fmt, args);
    va_end(args);
    rb_exc_raise(rb_exc_new_str(exc, RUBY_INVOKE(mesg, "force_encoding", rb_enc_from_encoding(enc))));
}

VALUE rb_enc_sprintf(rb_encoding *enc, const char *format, ...) {
  VALUE result;
  va_list ap;

  va_start(ap, format);
  result = rb_enc_vsprintf(enc, format, ap);
  va_end(ap);

  return result;
}

int rb_enc_to_index(rb_encoding *enc) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_enc_to_index", rb_enc_from_encoding(enc)));
}

VALUE rb_obj_encoding(VALUE obj) {
  return RUBY_INVOKE(obj, "encoding");
}

VALUE rb_str_encode(VALUE str, VALUE to, int ecflags, VALUE ecopts) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_str_encode", rb_tr_unwrap(str), rb_tr_unwrap(to), ecflags, rb_tr_unwrap(ecopts)));
}

VALUE rb_usascii_str_new(const char *ptr, long len) {
  return RUBY_INVOKE(rb_str_new(ptr, len), "force_encoding", rb_enc_from_encoding(rb_usascii_encoding()));
}

VALUE rb_usascii_str_new_cstr(const char *ptr) {
  return RUBY_INVOKE(rb_str_new_cstr(ptr), "force_encoding", rb_enc_from_encoding(rb_usascii_encoding()));
}

int rb_to_encoding_index(VALUE enc) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_to_encoding_index", enc));
}

char* rb_enc_nth(const char *p, const char *e, long nth, rb_encoding *enc) {
  rb_tr_error("rb_enc_nth not implemented");
}

int rb_enc_get_index(VALUE obj) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_enc_get_index", obj));
}

char* rb_enc_left_char_head(char *start, char *p, char *end, rb_encoding *enc) {
  int length = start-end;
  int position = polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_enc_left_char_head",
      rb_tr_unwrap(rb_enc_from_encoding(enc)),
      rb_tr_unwrap(rb_str_new(start, length)),
      0,
      p-start,
      length));
  return start+position;
}

int rb_enc_precise_mbclen(const char *p, const char *e, rb_encoding *enc) {
  int length = p-e;
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_enc_precise_mbclen",
      rb_tr_unwrap(rb_enc_from_encoding(enc)),
      rb_tr_unwrap(rb_str_new(p, length)),
      0,
      length));
}

VALUE rb_str_times(VALUE string, VALUE times) {
  return RUBY_INVOKE(string, "*", times);
}

int rb_enc_dummy_p(rb_encoding *enc) {
  return polyglot_as_i32(RUBY_INVOKE_NO_WRAP(rb_enc_from_encoding(enc), "dummy?"));
}

int rb_enc_mbmaxlen(rb_encoding *enc) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_enc_mbmaxlen", rb_enc_from_encoding(enc)));
}

int rb_enc_mbminlen(rb_encoding *enc) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_enc_mbminlen", rb_enc_from_encoding(enc)));
}

int rb_enc_mbclen(const char *p, const char *e, rb_encoding *enc) {
  int length = e-p;
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_enc_mbclen",
      rb_tr_unwrap(rb_enc_from_encoding(enc)),
      rb_tr_unwrap(rb_str_new(p, length)),
      0,
      length));
}

void rb_econv_close(rb_econv_t *ec) {
  rb_tr_error("rb_econv_close not implemented");
}

rb_econv_t *rb_econv_open_opts(const char *source_encoding, const char *destination_encoding, int ecflags, VALUE opthash) {
  rb_tr_error("rb_econv_open_opts not implemented");
}

VALUE rb_econv_str_convert(rb_econv_t *ec, VALUE src, int flags) {
  rb_tr_error("rb_econv_str_convert not implemented");
}

rb_econv_result_t rb_econv_convert(rb_econv_t *ec, const unsigned char **input_ptr, const unsigned char *input_stop, unsigned char **output_ptr, unsigned char *output_stop, int flags) {
  rb_tr_error("rb_econv_convert not implemented");
}

void rb_econv_check_error(rb_econv_t *ec) {
  rb_tr_error("rb_econv_check_error not implemented");
}

int rb_econv_prepare_opts(VALUE opthash, VALUE *opts) {
  rb_tr_error("rb_econv_prepare_opts not implemented");
}

// Symbol

ID rb_to_id(VALUE name) {
  return SYM2ID(RUBY_INVOKE(name, "to_sym"));
}

ID rb_intern2(const char *string, long length) {
  return (ID) SYM2ID(RUBY_CEXT_INVOKE("rb_intern", rb_str_new(string, length)));
}

ID rb_intern3(const char *name, long len, rb_encoding *enc) {
  return (ID) SYM2ID(RUBY_CEXT_INVOKE("rb_intern3", rb_str_new(name, len), rb_enc_from_encoding(enc)));
}

VALUE rb_sym2str(VALUE string) {
  return RUBY_INVOKE(string, "to_s");
}

const char *rb_id2name(ID id) {
    return RSTRING_PTR(rb_id2str(id));
}

VALUE rb_id2str(ID id) {
  return RUBY_CEXT_INVOKE("rb_id2str", ID2SYM(id));
}

int rb_is_class_id(ID id) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_is_class_id", ID2SYM(id)));
}

int rb_is_const_id(ID id) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_is_const_id", ID2SYM(id)));
}

int rb_is_instance_id(ID id) {
  return polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("rb_is_instance_id", ID2SYM(id)));
}

// Array

long rb_array_len(VALUE array) {
  return polyglot_get_array_size(rb_tr_unwrap(array));
}

int RARRAY_LENINT(VALUE array) {
  return polyglot_get_array_size(rb_tr_unwrap(array));
}

VALUE RARRAY_AREF(VALUE array, long index) {
  return rb_tr_wrap(polyglot_get_array_element(rb_tr_unwrap(array), (int) index));
}

VALUE rb_Array(VALUE array) {
  return RUBY_CEXT_INVOKE("rb_Array", array);
}

VALUE *RARRAY_PTR_IMPL(VALUE array) {
  return (VALUE *) RUBY_CEXT_INVOKE_NO_WRAP("RARRAY_PTR", array);
}

VALUE rb_ary_new() {
  return RUBY_CEXT_INVOKE("rb_ary_new");
}

VALUE rb_ary_new_capa(long capacity) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_ary_new_capa", capacity));
}

VALUE rb_ary_resize(VALUE ary, long len) {
  rb_tr_error("rb_ary_resize not implemented");
}

VALUE rb_ary_new_from_args(long n, ...) {
  VALUE array = rb_ary_new_capa(n);
  va_list args;
  va_start(args, n);
  for (int i = 0; i < n; i++) {
    rb_ary_store(array, i, va_arg(args, VALUE));
  }
  va_end(args);
  return array;
}

VALUE rb_ary_new_from_values(long n, const VALUE *values) {
  VALUE array = rb_ary_new_capa(n);
  for (int i = 0; i < n; i++) {
    rb_ary_store(array, i, values[i]);
  }
  return array;
}

VALUE rb_ary_push(VALUE array, VALUE value) {
  polyglot_invoke(rb_tr_unwrap(array), "push", rb_tr_unwrap(value));
  return array;
}

VALUE rb_ary_pop(VALUE array) {
  return RUBY_INVOKE(array, "pop");
}

void rb_ary_store(VALUE array, long index, VALUE value) {
  polyglot_set_array_element(rb_tr_unwrap(array), (int) index, rb_tr_unwrap(value));
}

VALUE rb_ary_entry(VALUE array, long index) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(array), "[]", index));
}

VALUE rb_ary_each(VALUE array) {
  rb_tr_error("rb_ary_each not implemented");
}

VALUE rb_ary_unshift(VALUE array, VALUE value) {
  return RUBY_INVOKE(array, "unshift", value);
}

VALUE rb_ary_aref(int n, const VALUE* values, VALUE array) {
  return RUBY_CEXT_INVOKE("send_splatted", array, rb_str_new_cstr("[]"), rb_ary_new4(n, values));
}

VALUE rb_ary_clear(VALUE array) {
  return RUBY_INVOKE(array, "clear");
}

VALUE rb_ary_delete(VALUE array, VALUE value) {
  return RUBY_INVOKE(array, "delete", value);
}

VALUE rb_ary_delete_at(VALUE array, long n) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(array), "delete_at", n));
}

VALUE rb_ary_includes(VALUE array, VALUE value) {
  return RUBY_INVOKE(array, "include?", value);
}

VALUE rb_ary_join(VALUE array, VALUE sep) {
  return RUBY_INVOKE(array, "join", sep);
}

VALUE rb_ary_to_s(VALUE array) {
  return RUBY_INVOKE(array, "to_s");
}

VALUE rb_ary_reverse(VALUE array) {
  return RUBY_INVOKE(array, "reverse!");
}

VALUE rb_ary_shift(VALUE array) {
  return RUBY_INVOKE(array, "shift");
}

VALUE rb_ary_concat(VALUE a, VALUE b) {
  return RUBY_INVOKE(a, "concat", b);
}

VALUE rb_ary_plus(VALUE a, VALUE b) {
  return RUBY_INVOKE(a, "+", b);
}

VALUE rb_iterate(VALUE (*function)(), VALUE arg1, VALUE (*block)(), VALUE arg2) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_iterate", function, rb_tr_unwrap(arg1), block, rb_tr_unwrap(arg2)));
}

VALUE rb_each(VALUE array) {
  if (rb_block_given_p()) {
    return rb_funcall_with_block(array, rb_intern("each"), 0, NULL, rb_block_proc());
  } else {
    return RUBY_INVOKE(array, "each");
  }
}

void rb_mem_clear(VALUE *mem, long n) {
  for (int i = 0; i < n; i++) {
    mem[i] = Qnil;
  }
}

VALUE rb_ary_to_ary(VALUE array) {
  VALUE tmp = rb_check_array_type(array);

  if (!NIL_P(tmp)) return tmp;
  return rb_ary_new_from_args(1, array);
}

VALUE rb_ary_subseq(VALUE array, long start, long length) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(array), "[]", start, length));
}

VALUE rb_check_array_type(VALUE array) {
  return rb_check_convert_type(array, T_ARRAY, "Array", "to_ary");
}

VALUE rb_ary_cat(VALUE array, const VALUE *cat, long n) {
  return RUBY_INVOKE(array, "concat", rb_ary_new4(n, cat));
}

VALUE rb_ary_rotate(VALUE array, long n) {
  if (n != 0) {
    return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(array), "rotate!", n));
  }
  return Qnil;
}

// Hash

VALUE rb_Hash(VALUE obj) {
  return RUBY_CEXT_INVOKE("rb_Hash", obj);
}

VALUE rb_hash(VALUE obj) {
  return RUBY_CEXT_INVOKE("rb_hash", obj);
}

VALUE rb_hash_new() {
  return RUBY_CEXT_INVOKE("rb_hash_new");
}

VALUE rb_hash_aref(VALUE hash, VALUE key) {
  return RUBY_CEXT_INVOKE("rb_hash_aref", hash, key);
}

VALUE rb_hash_fetch(VALUE hash, VALUE key) {
  return RUBY_INVOKE(hash, "fetch", key);
}

VALUE rb_hash_aset(VALUE hash, VALUE key, VALUE value) {
  return RUBY_INVOKE(hash, "[]=", key, value);
}

VALUE rb_hash_dup(VALUE hash) {
  return rb_obj_dup(hash);
}

VALUE rb_hash_lookup(VALUE hash, VALUE key) {
  return rb_hash_lookup2(hash, key, Qnil);
}

VALUE rb_hash_lookup2(VALUE hash, VALUE key, VALUE default_value) {
  VALUE result = RUBY_INVOKE(hash, "_get_or_undefined", key);
  if (result == Qundef) {
    result = default_value;
  }
  return result;
}

VALUE rb_hash_set_ifnone(VALUE hash, VALUE if_none) {
  return RUBY_CEXT_INVOKE("rb_hash_set_ifnone", hash, if_none);
}

VALUE rb_hash_keys(VALUE hash) {
  return RUBY_INVOKE(hash, "keys");
}

VALUE rb_hash_key_str(VALUE hash) {
  rb_tr_error("rb_hash_key_str not yet implemented");
}

st_index_t rb_memhash(const void *data, long length) {
  // Not a proper hash - just something that produces a stable result for now

  long hash = 0;

  for (long n = 0; n < length; n++) {
    hash = (hash << 1) ^ ((uint8_t*) data)[n];
  }

  return (st_index_t) hash;
}

VALUE rb_hash_clear(VALUE hash) {
  return RUBY_INVOKE(hash, "clear");
}

VALUE rb_hash_delete(VALUE hash, VALUE key) {
  return RUBY_INVOKE(hash, "delete", key);
}

VALUE rb_hash_delete_if(VALUE hash) {
  if (rb_block_given_p()) {
    return rb_funcall_with_block(hash, rb_intern("delete_if"), 0, NULL, rb_block_proc());
  } else {
    return RUBY_INVOKE(hash, "delete_if");
  }
}

void rb_hash_foreach(VALUE hash, int (*func)(ANYARGS), VALUE farg) {
  polyglot_invoke(RUBY_CEXT, "rb_hash_foreach", rb_tr_unwrap(hash), (void (*)(void *)) func, farg);
}

VALUE rb_hash_size(VALUE hash) {
  return RUBY_INVOKE(hash, "size");
}

size_t rb_hash_size_num(VALUE hash) {
  return (size_t) FIX2ULONG(rb_hash_size(hash));
}

// Class

const char* rb_class2name(VALUE ruby_class) {
  return RSTRING_PTR(rb_class_name(ruby_class));
}

VALUE rb_class_real(VALUE ruby_class) {
  if (ruby_class == NULL) {
    return NULL;
  }
  return RUBY_CEXT_INVOKE("rb_class_real", ruby_class);
}

VALUE rb_class_superclass(VALUE ruby_class) {
  return RUBY_INVOKE(ruby_class, "superclass");
}

VALUE rb_obj_class(VALUE object) {
  return rb_class_real(rb_class_of(object));
}

VALUE rb_singleton_class(VALUE object) {
  return RUBY_INVOKE(object, "singleton_class");
}

VALUE rb_class_of(VALUE object) {
  return RUBY_CEXT_INVOKE("rb_class_of", object);
}

VALUE rb_obj_alloc(VALUE ruby_class) {
  return RUBY_INVOKE(ruby_class, "__allocate__");
}

VALUE rb_class_path(VALUE ruby_class) {
  return RUBY_INVOKE(ruby_class, "name");
}

VALUE rb_path2class(const char *string) {
  return RUBY_CEXT_INVOKE("rb_path_to_class", rb_str_new_cstr(string));
}

VALUE rb_path_to_class(VALUE pathname) {
  return RUBY_CEXT_INVOKE("rb_path_to_class", pathname);
}

VALUE rb_class_name(VALUE ruby_class) {
  VALUE name = RUBY_INVOKE(ruby_class, "name");

  if (NIL_P(name)) {
    return rb_class_name(rb_obj_class(ruby_class));
  } else {
    return name;
  }
}

VALUE rb_class_new(VALUE super) {
  return RUBY_CEXT_INVOKE("rb_class_new", super);
}

VALUE rb_class_new_instance(int argc, const VALUE *argv, VALUE klass) {
  return RUBY_CEXT_INVOKE("rb_class_new_instance", klass, rb_ary_new4(argc, argv));
}

VALUE rb_cvar_defined(VALUE klass, ID id) {
  return RUBY_CEXT_INVOKE("rb_cvar_defined", klass, id);
}

VALUE rb_cvar_get(VALUE klass, ID id) {
  return RUBY_CEXT_INVOKE("rb_cvar_get", klass, id);
}

void rb_cvar_set(VALUE klass, ID id, VALUE val) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_cvar_set", klass, id, val);
}

VALUE rb_cv_get(VALUE klass, const char *name) {
  return RUBY_CEXT_INVOKE("rb_cv_get", klass, rb_str_new_cstr(name));
}

void rb_cv_set(VALUE klass, const char *name, VALUE val) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_cv_set", klass, rb_str_new_cstr(name), val);
}

void rb_define_attr(VALUE klass, const char *name, int read, int write) {
  polyglot_invoke(RUBY_CEXT, "rb_define_attr", rb_tr_unwrap(klass), rb_tr_unwrap(ID2SYM(rb_intern(name))), read, write);
}

void rb_define_class_variable(VALUE klass, const char *name, VALUE val) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_cv_set", klass, rb_str_new_cstr(name), val);
}

VALUE rb_mod_ancestors(VALUE mod) {
  return RUBY_INVOKE(mod, "ancestors");
}

// Proc

VALUE rb_proc_new(VALUE (*function)(ANYARGS), VALUE value) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_proc_new", (void (*)(void *)) function, rb_tr_unwrap(value)));
}

VALUE rb_proc_call(VALUE self, VALUE args) {
  return RUBY_CEXT_INVOKE("rb_proc_call", self, args);
}

int rb_proc_arity(VALUE self) {
  return polyglot_as_i32(polyglot_invoke(rb_tr_unwrap(self), "arity"));
}

// Utilities

void rb_bug(const char *fmt, ...) {
  rb_tr_error("rb_bug not yet implemented");
}

int rb_tr_to_int_const(VALUE value) {
  if (value == Qfalse) {
    return Qfalse_int_const;
  } else if (value == Qtrue) {
    return Qtrue_int_const;
  } else if (value == Qnil) {
    return Qnil_int_const;
  } else {
    return 8;
  }
}

VALUE rb_enumeratorize(VALUE obj, VALUE meth, int argc, const VALUE *argv) {
  return RUBY_CEXT_INVOKE("rb_enumeratorize", obj, meth, rb_ary_new4(argc, argv));
}

#undef rb_enumeratorize_with_size
VALUE
rb_enumeratorize_with_size(VALUE obj, VALUE meth, int argc, const VALUE *argv, rb_enumerator_size_func * size_fn) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_enumeratorize_with_size", rb_tr_unwrap(obj), rb_tr_unwrap(meth), rb_tr_unwrap(rb_ary_new4(argc, argv)), size_fn));
}

void rb_check_arity(int argc, int min, int max) {
  polyglot_invoke(RUBY_CEXT, "rb_check_arity", argc, min, max);
}

char* ruby_strdup(const char *str) {
  char *tmp;
  size_t len = strlen(str) + 1;

  tmp = xmalloc(len);
  memcpy(tmp, str, len);

  return tmp;
}

// Calls

int rb_respond_to(VALUE object, ID name) {
  return polyglot_as_boolean(RUBY_INVOKE_NO_WRAP(object, "respond_to?", name));
}

VALUE rb_funcallv(VALUE object, ID name, int args_count, const VALUE *args) {
  return RUBY_CEXT_INVOKE("rb_funcallv", object, ID2SYM(name), rb_ary_new4(args_count, args));
}

VALUE rb_funcallv_public(VALUE object, ID name, int args_count, const VALUE *args) {
  return RUBY_CEXT_INVOKE("rb_funcallv_public", object, ID2SYM(name), rb_ary_new4(args_count, args));
}

VALUE rb_apply(VALUE object, ID name, VALUE args) {
  return RUBY_CEXT_INVOKE("rb_apply", object, ID2SYM(name), args);
}

VALUE rb_block_call(VALUE object, ID name, int args_count, const VALUE *args, rb_block_call_func_t block_call_func, VALUE data) {
  if (rb_block_given_p()) {
    return rb_funcall_with_block(object, name, args_count, args, rb_block_proc());
  } else if (block_call_func == NULL) {
    return rb_funcallv(object, name, args_count, args);
  } else {
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_block_call", rb_tr_unwrap(object), rb_tr_unwrap(ID2SYM(name)), rb_tr_unwrap(rb_ary_new4(args_count, args)), block_call_func, data));
  }
}

VALUE rb_call_super(int args_count, const VALUE *args) {
  return RUBY_CEXT_INVOKE("rb_call_super", rb_ary_new4(args_count, args));
}

int rb_block_given_p() {
  return !NIL_P(rb_block_proc());
}

VALUE rb_block_proc(void) {
  return RUBY_CEXT_INVOKE("rb_block_proc");
}

VALUE rb_block_lambda(void) {
  return rb_block_proc();
}

VALUE rb_yield(VALUE value) {
  if (rb_block_given_p()) {
    return RUBY_CEXT_INVOKE("rb_yield", value);
  } else {
    return RUBY_CEXT_INVOKE("yield_no_block");
  }
}

VALUE rb_funcall_with_block(VALUE recv, ID mid, int argc, const VALUE *argv, VALUE pass_procval) {
  return RUBY_CEXT_INVOKE("rb_funcall_with_block", recv, ID2SYM(mid), rb_ary_new4(argc, argv), pass_procval);
}

VALUE rb_yield_splat(VALUE values) {
  if (rb_block_given_p()) {
    return RUBY_CEXT_INVOKE("rb_yield_splat", values);
  } else {
    return RUBY_CEXT_INVOKE("yield_no_block");
  }
}

VALUE rb_yield_values(int n, ...) {
  VALUE values = rb_ary_new_capa(n);
  for (int i = 0; i < n; i++) {
    rb_ary_store(values, i, (VALUE) polyglot_get_arg(1+i));
  }
  return rb_yield_splat(values);
}

// Instance variables

#undef rb_iv_get
VALUE rb_iv_get(VALUE object, const char *name) {
  return RUBY_CEXT_INVOKE("rb_ivar_get", object, rb_str_new_cstr(name));
}

VALUE rb_iv_set(VALUE object, const char *name, VALUE value) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_ivar_set", object, rb_str_new_cstr(name), value);
  return value;
}

VALUE rb_ivar_get(VALUE object, ID name) {
  return RUBY_CEXT_INVOKE("rb_ivar_get", object, name);
}

VALUE rb_ivar_set(VALUE object, ID name, VALUE value) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_ivar_set", object, name, value);
  return value;
}

VALUE rb_ivar_lookup(VALUE object, const char *name, VALUE default_value) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_ivar_lookup", rb_tr_unwrap(object), name, rb_tr_unwrap(default_value)));
}

VALUE rb_attr_get(VALUE object, ID name) {
  return RUBY_CEXT_INVOKE("rb_ivar_lookup", object, name, Qnil);
}

// Accessing constants

int rb_const_defined(VALUE module, ID name) {
  return polyglot_as_boolean(RUBY_INVOKE_NO_WRAP(module, "const_defined?", name));
}

int rb_const_defined_at(VALUE module, ID name) {
  return polyglot_as_boolean(RUBY_INVOKE_NO_WRAP(module, "const_defined?", name, Qfalse));
}

VALUE rb_const_get(VALUE module, ID name) {
  return RUBY_CEXT_INVOKE("rb_const_get", module, name);
}

VALUE rb_const_get_at(VALUE module, ID name) {
  return RUBY_INVOKE(module, "const_get", name, Qfalse);
}

VALUE rb_const_get_from(VALUE module, ID name) {
  return RUBY_CEXT_INVOKE("rb_const_get_from", module, name);
}

void rb_const_set(VALUE module, ID name, VALUE value) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_const_set", module, name, value);
}

void rb_define_const(VALUE module, const char *name, VALUE value) {
  rb_const_set(module, rb_str_new_cstr(name), value);
}

void rb_define_global_const(const char *name, VALUE value) {
  rb_define_const(rb_cObject, name, value);
}

// Global variables

VALUE rb_gvar_var_getter(ID id, void *data, struct rb_global_variable *gvar) {
  return *(VALUE*)data;
}

void rb_gvar_var_setter(VALUE val, ID id, void *data, struct rb_global_variable *gvar) {
  *((VALUE*)data) = val;
}

void rb_define_hooked_variable(const char *name, VALUE *var, VALUE (*getter)(ANYARGS), void (*setter)(ANYARGS)) {
  if (!getter) {
    getter = rb_gvar_var_getter;
  }

  if (!setter) {
    setter = rb_gvar_var_setter;
  }

  polyglot_invoke(RUBY_CEXT, "rb_define_hooked_variable", rb_tr_unwrap(rb_str_new_cstr(name)), var, getter, setter);
}

void rb_gvar_readonly_setter(VALUE val, ID id, void *data, struct rb_global_variable *gvar) {
  rb_raise(rb_eNameError, "read-only variable");
}

void rb_define_readonly_variable(const char *name, const VALUE *var) {
  rb_define_hooked_variable(name, (VALUE *)var, NULL, rb_gvar_readonly_setter);
}

void rb_define_variable(const char *name, VALUE *var) {
  rb_define_hooked_variable(name, var, 0, 0);
}

VALUE rb_f_global_variables(void) {
  return RUBY_CEXT_INVOKE("rb_f_global_variables");
}

VALUE rb_gv_set(const char *name, VALUE value) {
  return RUBY_CEXT_INVOKE("rb_gv_set", rb_str_new_cstr(name), value);
}

VALUE rb_gv_get(const char *name) {
  return RUBY_CEXT_INVOKE("rb_gv_get", rb_str_new_cstr(name));
}

VALUE rb_lastline_get(void) {
  rb_tr_error("rb_lastline_get not implemented");
}

void rb_lastline_set(VALUE val) {
  rb_tr_error("rb_lastline_set not implemented");
}

void rb_secure(int safe_level) {
  rb_gv_set("$SAFE", INT2FIX(safe_level));
}

// Exceptions

VALUE rb_exc_new(VALUE etype, const char *ptr, long len) {
  return RUBY_INVOKE(etype, "new", rb_str_new(ptr, len));
}

VALUE rb_exc_new_cstr(VALUE exception_class, const char *message) {
  return RUBY_INVOKE(exception_class, "new", rb_str_new_cstr(message));
}

VALUE rb_exc_new_str(VALUE exception_class, VALUE message) {
  return RUBY_INVOKE(exception_class, "new", message);
}

void rb_exc_raise(VALUE exception) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_exc_raise", exception);
  rb_tr_error("rb_exc_raise should not return");
}

VALUE rb_protect(VALUE (*function)(VALUE), VALUE data, int *status) {
  VALUE ary = polyglot_invoke(RUBY_CEXT, "rb_protect_with_block", function, data);
  *status = NUM2INT(rb_tr_wrap(polyglot_get_array_element(ary, 1)));
  return polyglot_get_array_element(ary, 0);
}

void rb_jump_tag(int status) {
  if (status) {
    polyglot_invoke(RUBY_CEXT, "rb_jump_tag", status);
  }
  rb_tr_error("rb_jump_tag should not return");
}

void rb_set_errinfo(VALUE error) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_set_errinfo", error);
}

VALUE rb_errinfo(void) {
  return RUBY_CEXT_INVOKE("rb_errinfo");
}

void rb_syserr_fail(int eno, const char *message) {
  polyglot_invoke(RUBY_CEXT, "rb_syserr_fail", eno, rb_tr_unwrap(message == NULL ? Qnil : rb_str_new_cstr(message)));
  rb_tr_error("rb_syserr_fail should not return");
}

void rb_sys_fail(const char *message) {
  int n = errno;
  errno = 0;

  if (n == 0) {
    rb_bug("rb_sys_fail(%s) - errno == 0", message ? message : "");
  }
  rb_syserr_fail(n, message);
}

VALUE rb_ensure(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*e_proc)(ANYARGS), VALUE data2) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_ensure", b_proc, data1, e_proc, data2));
}

VALUE rb_rescue(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2) {
  return polyglot_invoke(RUBY_CEXT, "rb_rescue", b_proc, data1, r_proc, data2);
}

VALUE rb_rescue2(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2, ...) {
  VALUE rescued = rb_ary_new();
  int n = 4;
  while (true) {
    VALUE arg = polyglot_get_arg(n);
    if (arg == NULL) {
      break;
    }
    rb_ary_push(rescued, arg);
    n++;
  }
  return polyglot_invoke(RUBY_CEXT, "rb_rescue2", b_proc, data1, r_proc, data2, rb_tr_unwrap(rescued));
}

VALUE rb_make_backtrace(void) {
  return RUBY_CEXT_INVOKE("rb_make_backtrace");
}

void rb_throw(const char *tag, VALUE val) {
  rb_throw_obj(rb_intern(tag), val);
}

void rb_throw_obj(VALUE tag, VALUE value) {
  RUBY_INVOKE_NO_WRAP(rb_mKernel, "throw", tag, value == NULL ? Qnil : value);
  rb_tr_error("rb_throw_obj should not return");
}

VALUE rb_catch(const char *tag, VALUE (*func)(), VALUE data) {
  return rb_catch_obj(rb_intern(tag), func, data);
}

VALUE rb_catch_obj(VALUE t, VALUE (*func)(), VALUE data) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_catch_obj", rb_tr_unwrap(t), func, rb_tr_unwrap(data)));
}

void rb_memerror(void) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_memerror");
  rb_tr_error("rb_memerror should not return");
}

void rb_name_error_str(VALUE str, const char *fmt, ...) {
  rb_tr_error("rb_name_error_str not implemented");
}

void rb_sys_fail_str(VALUE mesg) {
  rb_tr_error("rb_sys_fail_str not implemented");
}

// Defining classes, modules and methods

VALUE rb_define_class(const char *name, VALUE superclass) {
  return rb_define_class_under(rb_cObject, name, superclass);
}

VALUE rb_define_class_under(VALUE module, const char *name, VALUE superclass) {
  return rb_define_class_id_under(module, rb_str_new_cstr(name), superclass);
}

VALUE rb_define_class_id_under(VALUE module, ID name, VALUE superclass) {
  if (superclass == NULL) {
    // Handle the horrid semantics of what 0 means in this case.
    return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_define_class_under", rb_tr_unwrap(module), rb_tr_unwrap(name), superclass));
  }
  return RUBY_CEXT_INVOKE("rb_define_class_under", module, name, superclass);
}

VALUE rb_define_module(const char *name) {
  return rb_define_module_under(rb_cObject, name);
}

VALUE rb_define_module_under(VALUE module, const char *name) {
  return RUBY_CEXT_INVOKE("rb_define_module_under", module, rb_str_new_cstr(name));
}

void rb_include_module(VALUE module, VALUE to_include) {
  RUBY_INVOKE_NO_WRAP(module, "include", to_include);
}

void rb_define_method(VALUE module, const char *name, VALUE (*function)(ANYARGS), int argc) {
  if (function == rb_f_notimplement) {
    RUBY_CEXT_INVOKE("rb_define_method_undefined", module, rb_str_new_cstr(name));
  } else {
    rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_define_method", rb_tr_unwrap(module), rb_tr_unwrap(rb_str_new_cstr(name)), (void (*)(void *)) function, argc));
  }
}

void rb_define_private_method(VALUE module, const char *name, VALUE (*function)(ANYARGS), int argc) {
  rb_define_method(module, name, function, argc);
  RUBY_INVOKE_NO_WRAP(module, "private", rb_str_new_cstr(name));
}

void rb_define_protected_method(VALUE module, const char *name, VALUE (*function)(ANYARGS), int argc) {
  rb_define_method(module, name, function, argc);
  RUBY_INVOKE_NO_WRAP(module, "protected", rb_str_new_cstr(name));
}

void rb_define_module_function(VALUE module, const char *name, VALUE (*function)(ANYARGS), int argc) {
  rb_define_method(module, name, function, argc);
  RUBY_CEXT_INVOKE_NO_WRAP("cext_module_function", module, rb_intern(name));
}

void rb_define_global_function(const char *name, VALUE (*function)(ANYARGS), int argc) {
  rb_define_module_function(rb_mKernel, name, function, argc);
}

void rb_define_singleton_method(VALUE object, const char *name, VALUE (*function)(ANYARGS), int argc) {
  rb_define_method(rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(object), "singleton_class")), name, function, argc);
}

void rb_define_alias(VALUE module, const char *new_name, const char *old_name) {
  rb_alias(module, rb_str_new_cstr(new_name), rb_str_new_cstr(old_name));
}

void rb_alias(VALUE module, ID new_name, ID old_name) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_alias", module, new_name, old_name);
}

void rb_undef_method(VALUE module, const char *name) {
  rb_undef(module, rb_str_new_cstr(name));
}

void rb_undef(VALUE module, ID name) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_undef", module, name);
}

void rb_attr(VALUE ruby_class, ID name, int read, int write, int ex) {
  polyglot_invoke(RUBY_CEXT, "rb_attr", rb_tr_unwrap(ruby_class), rb_tr_unwrap(name), read, write, ex);
}

void rb_define_alloc_func(VALUE ruby_class, rb_alloc_func_t alloc_function) {
  polyglot_invoke(RUBY_CEXT, "rb_define_alloc_func", rb_tr_unwrap(ruby_class), (void (*)(void *)) alloc_function);
}

void rb_undef_alloc_func(VALUE ruby_class) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_undef_alloc_func", ruby_class);
}

VALUE rb_obj_method(VALUE obj, VALUE vid) {
  return RUBY_INVOKE(obj, "method", rb_intern_str(vid));
}

// Rational

VALUE rb_Rational(VALUE num, VALUE den) {
  return RUBY_CEXT_INVOKE("rb_Rational", num, den);
}

VALUE rb_rational_raw(VALUE num, VALUE den) {
  return RUBY_CEXT_INVOKE("rb_rational_raw", num, den);
}

VALUE rb_rational_new(VALUE num, VALUE den) {
  return RUBY_CEXT_INVOKE("rb_rational_new", num, den);
}

VALUE rb_rational_num(VALUE rat) {
  return RUBY_INVOKE(rat, "numerator");
}

VALUE rb_rational_den(VALUE rat) {
  return RUBY_INVOKE(rat, "denominator");
}

VALUE rb_flt_rationalize_with_prec(VALUE value, VALUE precision) {
  return RUBY_INVOKE(value, "rationalize", precision);
}

VALUE rb_flt_rationalize(VALUE value) {
    return RUBY_INVOKE(value, "rationalize");
}

// Complex

VALUE rb_Complex(VALUE real, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_Complex", real, imag);
}

VALUE rb_complex_new(VALUE real, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_complex_new", real, imag);
}

VALUE rb_complex_raw(VALUE real, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_complex_raw", real, imag);
}

VALUE rb_complex_polar(VALUE r, VALUE theta) {
  return RUBY_CEXT_INVOKE("rb_complex_polar", r, theta);
}

VALUE rb_complex_set_real(VALUE complex, VALUE real) {
  return RUBY_CEXT_INVOKE("rb_complex_set_real", complex, real);
}

VALUE rb_complex_set_imag(VALUE complex, VALUE imag) {
  return RUBY_CEXT_INVOKE("rb_complex_set_imag", complex, imag);
}

// Range

VALUE rb_range_new(VALUE beg, VALUE end, int exclude_end) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_range_new", rb_tr_unwrap(beg), rb_tr_unwrap(end), exclude_end));
}

/* This function can not be inlined as the two rb_intern macros
   generate static variables, and would produce unwanted
   warnings. This does mean that the start and end VALUEs will be
   converted to native handles and back if Sulong doesn't choose to
   inline this function, but this is unlikely to cause a major
   performance issue.
 */
int rb_range_values(VALUE range, VALUE *begp, VALUE *endp, int *exclp) {
  if (!rb_obj_is_kind_of(range, rb_cRange)) {
    if (!RTEST(RUBY_INVOKE(range, "respond_to?", rb_intern("begin")))) return Qfalse_int_const;
    if (!RTEST(RUBY_INVOKE(range, "respond_to?", rb_intern("end")))) return Qfalse_int_const;
  }

  *begp = RUBY_INVOKE(range, "begin");
  *endp = RUBY_INVOKE(range, "end");
  *exclp = (int) RTEST(RUBY_INVOKE(range, "exclude_end?"));
  return Qtrue_int_const;
}

VALUE rb_range_beg_len(VALUE range, long *begp, long *lenp, long len, int err) {
  long beg, end, origbeg, origend;
  VALUE b, e;
  int excl;

  if (!rb_range_values(range, &b, &e, &excl)) {
    return Qfalse;
  }

  beg = NUM2LONG(b);
  end = NUM2LONG(e);
  origbeg = beg;
  origend = end;
  if (beg < 0) {
    beg += len;
    if (beg < 0) {
      goto out_of_range;
    }
  }
  if (end < 0) {
    end += len;
  }
  if (!excl) {
    end++;                        /* include end point */
  }
  if (err == 0 || err == 2) {
    if (beg > len) {
      goto out_of_range;
    }
    if (end > len) {
      end = len;
    }
  }
  len = end - beg;
  if (len < 0) {
    len = 0;
  }

  *begp = beg;
  *lenp = len;
  return Qtrue;

out_of_range:
  if (err) {
    rb_raise(rb_eRangeError, "%ld..%s%ld out of range",
             origbeg, excl ? "." : "", origend);
  }
  return Qnil;
}

int rb_arithmetic_sequence_extract(VALUE obj, rb_arithmetic_sequence_components_t *component) {
  rb_tr_error("rb_arithmetic_sequence_extract not implemented");
}

// Time

VALUE rb_time_new(time_t sec, long usec) {
  return rb_tr_wrap(polyglot_invoke(rb_tr_unwrap(rb_cTime), "at", sec, usec));
}

VALUE rb_time_nano_new(time_t sec, long nsec) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_time_nano_new", sec, nsec));
}

VALUE rb_time_num_new(VALUE timev, VALUE off) {
  return RUBY_CEXT_INVOKE("rb_time_num_new", timev, off);
}

struct timeval rb_time_interval(VALUE time_val) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_time_interval_acceptable", time_val);

  struct timeval result;

  VALUE time = rb_time_num_new(time_val, Qnil);
  result.tv_sec = polyglot_as_i64(RUBY_INVOKE_NO_WRAP(time, "tv_sec"));
  result.tv_usec = polyglot_as_i64(RUBY_INVOKE_NO_WRAP(time, "tv_usec"));

  return result;
}

struct timeval rb_time_timeval(VALUE time_val) {
  struct timeval result;

  VALUE time = rb_time_num_new(time_val, Qnil);
  result.tv_sec = polyglot_as_i64(RUBY_INVOKE_NO_WRAP(time, "tv_sec"));
  result.tv_usec = polyglot_as_i64(RUBY_INVOKE_NO_WRAP(time, "tv_usec"));

  return result;
}

struct timespec rb_time_timespec(VALUE time_val) {
  struct timespec result;

  VALUE time = rb_time_num_new(time_val, Qnil);
  result.tv_sec = polyglot_as_i64(RUBY_INVOKE_NO_WRAP(time, "tv_sec"));
  result.tv_nsec = polyglot_as_i64(RUBY_INVOKE_NO_WRAP(time, "tv_nsec"));

  return result;
}

VALUE rb_time_timespec_new(const struct timespec *ts, int offset) {
  VALUE is_utc = rb_tr_unwrap(rb_boolean(offset == INT_MAX-1));
  VALUE is_local = rb_tr_unwrap(rb_boolean(offset == INT_MAX));
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_time_timespec_new", ts->tv_sec, ts->tv_nsec, offset, is_utc, is_local));
}

void rb_timespec_now(struct timespec *ts) {
  struct timeval tv = rb_time_timeval(RUBY_INVOKE(rb_cTime, "now"));
  ts->tv_sec = tv.tv_sec;
  ts->tv_nsec = tv.tv_usec * 1000;
}

// Regexp

VALUE rb_backref_get(void) {
  return RUBY_CEXT_INVOKE("rb_backref_get");
}

VALUE rb_reg_match_pre(VALUE match) {
  return RUBY_CEXT_INVOKE("rb_reg_match_pre", match);
}

VALUE rb_reg_new(const char *s, long len, int options) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_new", rb_tr_unwrap(rb_str_new(s, len)), options));
}

VALUE rb_reg_new_str(VALUE s, int options) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_new_str", rb_tr_unwrap(s), options));
}

VALUE rb_reg_nth_match(int nth, VALUE match) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_reg_nth_match", nth, rb_tr_unwrap(match)));
}

int rb_reg_options(VALUE re) {
  return FIX2INT(RUBY_CEXT_INVOKE("rb_reg_options", re));
}

VALUE rb_reg_regcomp(VALUE str) {
  return RUBY_CEXT_INVOKE("rb_reg_regcomp", str);
}

VALUE rb_reg_match(VALUE re, VALUE str) {
  return RUBY_CEXT_INVOKE("rb_reg_match", re, str);
}

// Marshal

VALUE rb_marshal_dump(VALUE obj, VALUE port) {
  return RUBY_CEXT_INVOKE("rb_marshal_dump", obj, port);
}

VALUE rb_marshal_load(VALUE port) {
  return RUBY_CEXT_INVOKE("rb_marshal_load", port);
}

// Mutexes

VALUE rb_mutex_new(void) {
  return RUBY_CEXT_INVOKE("rb_mutex_new");
}

VALUE rb_mutex_locked_p(VALUE mutex) {
  return RUBY_CEXT_INVOKE("rb_mutex_locked_p", mutex);
}

VALUE rb_mutex_trylock(VALUE mutex) {
  return RUBY_CEXT_INVOKE("rb_mutex_trylock", mutex);
}

VALUE rb_mutex_lock(VALUE mutex) {
  return RUBY_CEXT_INVOKE("rb_mutex_lock", mutex);
}

VALUE rb_mutex_unlock(VALUE mutex) {
  return RUBY_CEXT_INVOKE("rb_mutex_unlock", mutex);
}

VALUE rb_mutex_sleep(VALUE mutex, VALUE timeout) {
  return RUBY_CEXT_INVOKE("rb_mutex_sleep", mutex, timeout);
}

VALUE rb_mutex_synchronize(VALUE mutex, VALUE (*func)(VALUE arg), VALUE arg) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_mutex_synchronize", rb_tr_unwrap(mutex), func, rb_tr_unwrap(arg)));
}

// GC

void rb_gc_register_address(VALUE *address) {
}

void rb_gc_unregister_address(VALUE *address) {
  // VALUE is only ever in managed memory. So, it is already garbage collected.
}

void rb_gc_mark(VALUE ptr) {
  polyglot_invoke(RUBY_CEXT, "rb_gc_mark", ptr);
}

void rb_gc_mark_maybe(VALUE obj) {
  rb_tr_error("rb_gc_mark_maybe not implemented");
}

VALUE rb_gc_enable() {
  return RUBY_CEXT_INVOKE("rb_gc_enable");
}

VALUE rb_gc_disable() {
  return RUBY_CEXT_INVOKE("rb_gc_disable");
}

void rb_gc(void) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_gc");
}

// Threads

void *rb_thread_call_with_gvl(gvl_call function, void *data1) {
  return polyglot_invoke(RUBY_CEXT, "rb_thread_call_with_gvl", function, data1);
}

void *rb_thread_call_without_gvl(gvl_call function, void *data1, rb_unblock_function_t *unblock_function, void *data2) {
  if (unblock_function == RUBY_UBF_IO) {
    unblock_function = (rb_unblock_function_t*) rb_tr_unwrap(Qnil);
  }
  return polyglot_invoke(RUBY_CEXT, "rb_thread_call_without_gvl", function, data1, unblock_function, data2);
}

int rb_thread_alone(void) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_thread_alone"));
}

VALUE rb_thread_current(void) {
  return RUBY_INVOKE(rb_tr_get_Thread(), "current");
}

VALUE rb_thread_local_aref(VALUE thread, ID id) {
  return RUBY_INVOKE(thread, "[]", ID2SYM(id));
}

VALUE rb_thread_local_aset(VALUE thread, ID id, VALUE val) {
  return RUBY_INVOKE(thread, "[]=", ID2SYM(id), val);
}

void rb_thread_wait_for(struct timeval time) {
  double seconds = (double)time.tv_sec + (double)time.tv_usec/1000000;
  polyglot_invoke(rb_tr_unwrap(rb_mKernel), "sleep", seconds);
}

VALUE rb_thread_wakeup(VALUE thread) {
  return RUBY_INVOKE(thread, "wakeup");
}

VALUE rb_thread_create(VALUE (*fn)(ANYARGS), void *arg) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_thread_create", fn, arg));
}

void rb_thread_schedule(void) {
  polyglot_invoke(rb_tr_unwrap(rb_cThread), "pass");
}

rb_nativethread_id_t rb_nativethread_self() {
  return RUBY_CEXT_INVOKE("rb_nativethread_self");
}

// IO

void rb_io_check_writable(rb_io_t *io) {
  if (!rb_tr_writable(io->mode)) {
    rb_raise(rb_eIOError, "not opened for writing");
  }
}

void rb_io_check_readable(rb_io_t *io) {
  if (!rb_tr_readable(io->mode)) {
    rb_raise(rb_eIOError, "not opened for reading");
  }
}

int rb_cloexec_dup(int oldfd) {
  rb_tr_error("rb_cloexec_dup not implemented");
}

void rb_update_max_fd(int fd) {
}

void rb_fd_fix_cloexec(int fd) {
  fcntl(fd, F_SETFD, fcntl(fd, F_GETFD) | FD_CLOEXEC);
}

int rb_io_wait_readable(int fd) {
  if (fd < 0) {
    rb_raise(rb_eIOError, "closed stream");
  }

  switch (errno) {
    case EAGAIN:
  #if defined(EWOULDBLOCK) && EWOULDBLOCK != EAGAIN
    case EWOULDBLOCK:
  #endif
      rb_thread_wait_fd(fd);
      return true;

    default:
      return false;
  }
}

int rb_io_wait_writable(int fd) {
  if (fd < 0) {
    rb_raise(rb_eIOError, "closed stream");
  }

  switch (errno) {
    case EAGAIN:
  #if defined(EWOULDBLOCK) && EWOULDBLOCK != EAGAIN
    case EWOULDBLOCK:
  #endif
      rb_thread_fd_writable(fd);
      return true;

    default:
      return false;
  }
}

void rb_thread_wait_fd(int fd) {
  polyglot_invoke(RUBY_CEXT, "rb_thread_wait_fd", fd);
}

int rb_wait_for_single_fd(int fd, int events, struct timeval *tv) {
  long tv_sec = -1;
  long tv_usec = -1;
  if (tv != NULL) {
    tv_sec = tv->tv_sec;
    tv_usec = tv->tv_usec;
  }
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_wait_for_single_fd", fd, events, tv_sec, tv_usec));
}

NORETURN(void rb_eof_error(void)) {
  rb_raise(rb_eEOFError, "end of file reached");
}

VALUE rb_io_addstr(VALUE io, VALUE str) {
  // use write instead of just #<<, it's closer to what MRI does
  // and avoids stack-overflow in zlib where #<< is defined with this method
  rb_io_write(io, str);
  return io;
}

VALUE rb_io_check_io(VALUE io) {
  return rb_check_convert_type(io, T_FILE, "IO", "to_io");
}

void rb_io_check_closed(rb_io_t *fptr) {
  if (fptr->fd < 0) {
    rb_raise(rb_eIOError, "closed stream");
  }
}

VALUE rb_io_taint_check(VALUE io) {
  rb_check_frozen(io);
  return io;
}

VALUE rb_io_close(VALUE io) {
  return RUBY_INVOKE(io, "close");
}

VALUE rb_io_print(int argc, const VALUE *argv, VALUE out) {
  return RUBY_CEXT_INVOKE("rb_io_print", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_printf(int argc, const VALUE *argv, VALUE out) {
  return RUBY_CEXT_INVOKE("rb_io_printf", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_puts(int argc, const VALUE *argv, VALUE out) {
  return RUBY_CEXT_INVOKE("rb_io_puts", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_write(VALUE io, VALUE str) {
  return RUBY_INVOKE(io, "write", str);
}

VALUE rb_io_binmode(VALUE io) {
  return RUBY_INVOKE(io, "binmode");
}

int rb_thread_fd_writable(int fd) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_thread_fd_writable", fd));
}

int rb_cloexec_open(const char *pathname, int flags, mode_t mode) {
  int fd = open(pathname, flags, mode);
  if (fd >= 0) {
    rb_fd_fix_cloexec(fd);
  }
  return fd;
}

VALUE rb_file_open(const char *fname, const char *modestr) {
  return RUBY_INVOKE(rb_cFile, "open", rb_str_new_cstr(fname), rb_str_new_cstr(modestr));
}

VALUE rb_file_open_str(VALUE fname, const char *modestr) {
  return RUBY_INVOKE(rb_cFile, "open", fname, rb_str_new_cstr(modestr));
}

VALUE rb_get_path(VALUE object) {
  return RUBY_INVOKE(rb_cFile, "path", object);
}

int rb_tr_readable(int mode) {
  return polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "rb_tr_readable", mode));
}

int rb_tr_writable(int mode) {
  return polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "rb_tr_writable", mode));
}

MUST_INLINE
int rb_io_extract_encoding_option(VALUE opt, rb_encoding **enc_p, rb_encoding **enc2_p, int *fmode_p) {
  // TODO (pitr-ch 12-Jun-2017): review, just approximate implementation
  VALUE encoding = rb_cEncoding;
  VALUE external_encoding = RUBY_INVOKE(encoding, "default_external");
  VALUE internal_encoding = RUBY_INVOKE(encoding, "default_internal");
  if (!NIL_P(external_encoding)) {
    *enc_p = rb_to_encoding(external_encoding);
  }
  if (!NIL_P(internal_encoding)) {
    *enc2_p = rb_to_encoding(internal_encoding);
  }
  return 1;
}

// Structs

VALUE rb_struct_aref(VALUE s, VALUE idx) {
  return RUBY_CEXT_INVOKE("rb_struct_aref", s, idx);
}

VALUE rb_struct_aset(VALUE s, VALUE idx, VALUE val) {
  return RUBY_CEXT_INVOKE("rb_struct_aset", s, idx, val);
}

VALUE rb_struct_define(const char *name, ...) {
  VALUE rb_name = name == NULL ? RUBY_CEXT_INVOKE("Qnil") : rb_str_new_cstr(name);
  VALUE ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  while ((arg = (char *)polyglot_get_arg(1+i)) != NULL) {
    rb_ary_push(ary, rb_str_new_cstr(arg));
    i++;
  }
  return RUBY_CEXT_INVOKE("rb_struct_define_no_splat", rb_name, ary);
}

VALUE rb_struct_define_under(VALUE outer, const char *name, ...) {
  VALUE rb_name = name == NULL ? RUBY_CEXT_INVOKE("Qnil") : rb_str_new_cstr(name);
  VALUE ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  while ((arg = (char *)polyglot_get_arg(2+i)) != NULL) {
    rb_ary_push(ary, rb_str_new_cstr(arg));
    i++;
  }
  return RUBY_CEXT_INVOKE("rb_struct_define_under_no_splat", outer, rb_name, ary);
}

VALUE rb_struct_new(VALUE klass, ...) {
  int members = polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_struct_size", klass));
  VALUE ary = rb_ary_new();
  int i = 0;
  while (i < members) {
    VALUE arg = polyglot_get_arg(1+i);
    rb_ary_push(ary, arg);
    i++;
  }
  return RUBY_CEXT_INVOKE("rb_struct_new_no_splat", klass, ary);
}

VALUE rb_struct_size(VALUE s) {
  return RUBY_INVOKE(s, "size");
}

VALUE rb_struct_getmember(VALUE obj, ID id) {
  rb_tr_error("rb_struct_getmember not implemented");
}

VALUE rb_struct_s_members(VALUE klass) {
  rb_tr_error("rb_struct_s_members not implemented");
}

VALUE rb_struct_members(VALUE s) {
  rb_tr_error("rb_struct_members not implemented");
}

// Data

POLYGLOT_DECLARE_STRUCT(RData)

struct RData *RDATA(VALUE value) {
  return polyglot_as_RData(RUBY_CEXT_INVOKE_NO_WRAP("RDATA", value));
}

static RUBY_DATA_FUNC rb_tr_free_function(RUBY_DATA_FUNC dfree) {
  return (dfree == (RUBY_DATA_FUNC)RUBY_DEFAULT_FREE) ? free : dfree;
}

#undef rb_data_object_wrap
VALUE rb_data_object_wrap(VALUE klass, void *data, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_data_object_wrap",
                                            rb_tr_unwrap(klass), data, dmark, rb_tr_free_function(dfree) ));
}

VALUE rb_data_object_zalloc(VALUE klass, size_t size, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree) {
  void *data = calloc(1, size);
  return rb_data_object_wrap(klass, data, dmark, dfree);
}

VALUE rb_data_object_alloc_managed(VALUE klass, size_t size, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree, void *interoptypeid) {
  void *data = rb_tr_new_managed_struct_internal(interoptypeid);
  return rb_data_object_wrap(klass, data, dmark, (dfree == (RUBY_DATA_FUNC)RUBY_DEFAULT_FREE) ? NULL : dfree);
}

// Typed data

VALUE rb_data_typed_object_wrap(VALUE ruby_class, void *data, const rb_data_type_t *data_type) {
  return rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_data_typed_object_wrap",
                                    rb_tr_unwrap(ruby_class), data, data_type, data_type->function.dmark, rb_tr_free_function(data_type->function.dfree), data_type->function.dsize));
}

VALUE rb_data_typed_object_zalloc(VALUE ruby_class, size_t size, const rb_data_type_t *data_type) {
  void *data = calloc(1, size);
  return rb_data_typed_object_wrap(ruby_class, data, data_type);
}

VALUE rb_data_typed_object_alloc_managed(VALUE ruby_class, size_t size, const rb_data_type_t *data_type, void *interoptypeid) {
  void *data = rb_tr_new_managed_struct_internal(interoptypeid);
  return rb_data_typed_object_wrap(ruby_class, data, data_type);
}

VALUE rb_data_typed_object_make(VALUE ruby_class, const rb_data_type_t *type, void **data_pointer, size_t size) {
  TypedData_Make_Struct0(result, ruby_class, void, size, type, *data_pointer);
  return result;
}

void *rb_check_typeddata(VALUE value, const rb_data_type_t *data_type) {
  if (rb_tr_object_hidden_var_get(value, "data_type") != data_type) {
    rb_raise(rb_eTypeError, "wrong argument type");
  }
  return RTYPEDDATA_DATA(value);
}

// VM

VALUE rb_tr_ruby_verbose_ptr;

VALUE *rb_ruby_verbose_ptr(void) {
  rb_tr_ruby_verbose_ptr = RUBY_CEXT_INVOKE("rb_ruby_verbose_ptr");
  return &rb_tr_ruby_verbose_ptr;
}

VALUE rb_tr_ruby_debug_ptr;

VALUE *rb_ruby_debug_ptr(void) {
  rb_tr_ruby_debug_ptr = RUBY_CEXT_INVOKE("rb_ruby_debug_ptr");
  return &rb_tr_ruby_debug_ptr;
}

// Non-standard

void rb_tr_error(const char *message) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_tr_error", rb_str_new_cstr(message));
  abort();
}

void rb_tr_log_warning(const char *message) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_tr_log_warning", rb_str_new_cstr(message));
}

long rb_tr_obj_id(VALUE object) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_tr_obj_id", object));
}

VALUE rb_java_class_of(VALUE obj) {
  return RUBY_CEXT_INVOKE("rb_java_class_of", obj);
}

VALUE rb_java_to_string(VALUE obj) {
  return RUBY_CEXT_INVOKE("rb_java_to_string", obj);
}

// Managed Structs

void* rb_tr_new_managed_struct_internal(void *type) {
  return polyglot_invoke(RUBY_CEXT, "rb_tr_new_managed_struct", type);
}

// Remaining functions

void rb_big_2comp(VALUE x) {
  rb_tr_error("rb_big_2comp not implemented");
}

int rb_profile_frames(int start, int limit, VALUE *buff, int *lines) {
  rb_tr_error("rb_profile_frames not implemented");
}

VALUE rb_profile_frame_path(VALUE frame) {
  rb_tr_error("rb_profile_frame_path not implemented");
}

VALUE rb_profile_frame_absolute_path(VALUE frame) {
  rb_tr_error("rb_profile_frame_absolute_path not implemented");
}

VALUE rb_profile_frame_label(VALUE frame) {
  rb_tr_error("rb_profile_frame_label not implemented");
}

VALUE rb_profile_frame_base_label(VALUE frame) {
  rb_tr_error("rb_profile_frame_base_label not implemented");
}

VALUE rb_profile_frame_full_label(VALUE frame) {
  rb_tr_error("rb_profile_frame_full_label not implemented");
}

VALUE rb_profile_frame_first_lineno(VALUE frame) {
  rb_tr_error("rb_profile_frame_first_lineno not implemented");
}

VALUE rb_profile_frame_classpath(VALUE frame) {
  rb_tr_error("rb_profile_frame_classpath not implemented");
}

VALUE rb_profile_frame_singleton_method_p(VALUE frame) {
  rb_tr_error("rb_profile_frame_singleton_method_p not implemented");
}

VALUE rb_profile_frame_method_name(VALUE frame) {
  rb_tr_error("rb_profile_frame_method_name not implemented");
}

VALUE rb_profile_frame_qualified_method_name(VALUE frame) {
  rb_tr_error("rb_profile_frame_qualified_method_name not implemented");
}

VALUE rb_debug_inspector_open(rb_debug_inspector_func_t func, void *data) {
  rb_tr_error("rb_debug_inspector_open not implemented");
}

VALUE rb_debug_inspector_frame_self_get(const rb_debug_inspector_t *dc, long index) {
  rb_tr_error("rb_debug_inspector_frame_self_get not implemented");
}

VALUE rb_debug_inspector_frame_class_get(const rb_debug_inspector_t *dc, long index) {
  rb_tr_error("rb_debug_inspector_frame_class_get not implemented");
}

VALUE rb_debug_inspector_frame_binding_get(const rb_debug_inspector_t *dc, long index) {
  rb_tr_error("rb_debug_inspector_frame_binding_get not implemented");
}

VALUE rb_debug_inspector_frame_iseq_get(const rb_debug_inspector_t *dc, long index) {
  rb_tr_error("rb_debug_inspector_frame_iseq_get not implemented");
}

VALUE rb_debug_inspector_backtrace_locations(const rb_debug_inspector_t *dc) {
  rb_tr_error("rb_debug_inspector_backtrace_locations not implemented");
}

void rb_add_event_hook(rb_event_hook_func_t func, rb_event_flag_t events, VALUE data) {
  rb_tr_error("rb_add_event_hook not implemented");
}

int rb_remove_event_hook(rb_event_hook_func_t func) {
  rb_tr_error("rb_remove_event_hook not implemented");
}

int rb_remove_event_hook_with_data(rb_event_hook_func_t func, VALUE data) {
  rb_tr_error("rb_remove_event_hook_with_data not implemented");
}

void rb_thread_add_event_hook(VALUE thval, rb_event_hook_func_t func, rb_event_flag_t events, VALUE data) {
  rb_tr_error("rb_thread_add_event_hook not implemented");
}

int rb_thread_remove_event_hook(VALUE thval, rb_event_hook_func_t func) {
  rb_tr_error("rb_thread_remove_event_hook not implemented");
}

int rb_thread_remove_event_hook_with_data(VALUE thval, rb_event_hook_func_t func, VALUE data) {
  rb_tr_error("rb_thread_remove_event_hook_with_data not implemented");
}

VALUE rb_tracepoint_new(VALUE target_thval, rb_event_flag_t events, void (*func)(VALUE, void *), void *data) {
  rb_tr_error("rb_tracepoint_new not implemented");
}

VALUE rb_tracepoint_enable(VALUE tpval) {
  rb_tr_error("rb_tracepoint_enable not implemented");
}

VALUE rb_tracepoint_disable(VALUE tpval) {
  rb_tr_error("rb_tracepoint_disable not implemented");
}

VALUE rb_tracepoint_enabled_p(VALUE tpval) {
  rb_tr_error("rb_tracepoint_enabled_p not implemented");
}

rb_event_flag_t rb_tracearg_event_flag(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_event_flag not implemented");
}

VALUE rb_tracearg_event(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_event not implemented");
}

VALUE rb_tracearg_lineno(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_lineno not implemented");
}

VALUE rb_tracearg_path(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_path not implemented");
}

VALUE rb_tracearg_method_id(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_method_id not implemented");
}

VALUE rb_tracearg_defined_class(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_defined_class not implemented");
}

VALUE rb_tracearg_binding(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_binding not implemented");
}

VALUE rb_tracearg_self(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_self not implemented");
}

VALUE rb_tracearg_return_value(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_return_value not implemented");
}

VALUE rb_tracearg_raised_exception(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_raised_exception not implemented");
}

VALUE rb_tracearg_object(rb_trace_arg_t *trace_arg) {
  rb_tr_error("rb_tracearg_object not implemented");
}

int rb_postponed_job_register(unsigned int flags, rb_postponed_job_func_t func, void *data) {
  rb_tr_error("rb_postponed_job_register not implemented");
}

int rb_postponed_job_register_one(unsigned int flags, rb_postponed_job_func_t func, void *data) {
  rb_tr_error("rb_postponed_job_register_one not implemented");
}

void rb_add_event_hook2(rb_event_hook_func_t func, rb_event_flag_t events, VALUE data, rb_event_hook_flag_t hook_flags) {
  rb_tr_error("rb_add_event_hook2 not implemented");
}

void rb_thread_add_event_hook2(VALUE thval, rb_event_hook_func_t func, rb_event_flag_t events, VALUE data, rb_event_hook_flag_t hook_flags) {
  rb_tr_error("rb_thread_add_event_hook2 not implemented");
}

void rb_sparc_flush_register_windows(void) {
  rb_tr_error("rb_sparc_flush_register_windows not implemented");
}

int rb_char_to_option_kcode(int c, int *option, int *kcode) {
  rb_tr_error("rb_char_to_option_kcode not implemented");
}

int rb_enc_replicate(const char *name, rb_encoding *encoding) {
  rb_tr_error("rb_enc_replicate not implemented");
}

int rb_define_dummy_encoding(const char *name) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_define_dummy_encoding", rb_str_new_cstr(name)));
}

#undef rb_enc_str_new_cstr
VALUE rb_enc_str_new_cstr(const char *ptr, rb_encoding *enc) {
  if (rb_enc_mbminlen(enc) != 1) {
    rb_raise(rb_eArgError, "wchar encoding given");
  }

  VALUE string = rb_str_new_cstr(ptr);
  rb_enc_associate(string, enc);
  return string;
}

VALUE rb_enc_str_new_static(const char *ptr, long len, rb_encoding *enc) {
  if (len < 0) {
    rb_raise(rb_eArgError, "negative string size (or size too big)");
  }

  VALUE string = rb_enc_str_new(ptr, len, enc);
  return string;
}

VALUE rb_enc_reg_new(const char *s, long len, rb_encoding *enc, int options) {
  rb_tr_error("rb_enc_reg_new not implemented");
}

VALUE rb_enc_vsprintf(rb_encoding *enc, const char *format, va_list args) {
  char *buffer;
  #ifdef __APPLE__
  if (vasxprintf(&buffer, printf_domain, NULL, format, args) < 0) {
  #else
  if (vasprintf(&buffer, format, args) < 0) {
  #endif
    rb_tr_error("vasprintf error");
  }
  VALUE string = rb_enc_str_new_cstr(buffer, enc);
  free(buffer);
  return string;
}

long rb_enc_strlen(const char *p, const char *e, rb_encoding *enc) {
  rb_tr_error("rb_enc_strlen not implemented");
}

VALUE rb_enc_str_buf_cat(VALUE str, const char *ptr, long len, rb_encoding *ptr_enc) {
  rb_tr_error("rb_enc_str_buf_cat not implemented");
}

VALUE rb_enc_uint_chr(unsigned int code, rb_encoding *enc) {
  rb_tr_error("rb_enc_uint_chr not implemented");
}

int rb_enc_fast_mbclen(const char *p, const char *e, rb_encoding *enc) {
  rb_tr_error("rb_enc_fast_mbclen not implemented");
}

int rb_enc_ascget(const char *p, const char *e, int *len, rb_encoding *enc) {
  rb_tr_error("rb_enc_ascget not implemented");
}

int rb_enc_codelen(int c, rb_encoding *enc) {
  rb_tr_error("rb_enc_codelen not implemented");
}

#undef rb_enc_code_to_mbclen
int rb_enc_code_to_mbclen(int code, rb_encoding *enc) {
  rb_tr_error("rb_enc_code_to_mbclen not implemented");
}

int rb_enc_toupper(int c, rb_encoding *enc) {
  rb_tr_error("rb_enc_toupper not implemented");
}

int rb_enc_tolower(int c, rb_encoding *enc) {
  rb_tr_error("rb_enc_tolower not implemented");
}

int rb_enc_symname_p(const char *name, rb_encoding *enc) {
  rb_tr_error("rb_enc_symname_p not implemented");
}

int rb_enc_symname2_p(const char *name, long len, rb_encoding *enc) {
  rb_tr_error("rb_enc_symname2_p not implemented");
}

long rb_str_coderange_scan_restartable(const char *s, const char *e, rb_encoding *enc, int *cr) {
  rb_tr_error("rb_str_coderange_scan_restartable not implemented");
}

int rb_enc_str_asciionly_p(VALUE str) {
  rb_tr_error("rb_enc_str_asciionly_p not implemented");
}

int rb_enc_unicode_p(rb_encoding *enc) {
  rb_tr_error("rb_enc_unicode_p not implemented");
}

VALUE rb_enc_default_external(void) {
  rb_tr_error("rb_enc_default_external not implemented");
}

VALUE rb_enc_default_internal(void) {
  rb_tr_error("rb_enc_default_internal not implemented");
}

void rb_enc_set_default_external(VALUE encoding) {
  rb_tr_error("rb_enc_set_default_external not implemented");
}

void rb_enc_set_default_internal(VALUE encoding) {
  rb_tr_error("rb_enc_set_default_internal not implemented");
}

VALUE rb_locale_charmap(VALUE klass) {
  rb_tr_error("rb_locale_charmap not implemented");
}

long rb_memsearch(const void *x0, long m, const void *y0, long n, rb_encoding *enc) {
  rb_tr_error("rb_memsearch not implemented");
}

ID rb_check_id_cstr(const char *ptr, long len, rb_encoding *enc) {
  rb_tr_error("rb_check_id_cstr not implemented");
}

VALUE rb_check_symbol_cstr(const char *ptr, long len, rb_encoding *enc) {
  rb_tr_error("rb_check_symbol_cstr not implemented");
}

int rb_econv_has_convpath_p(const char* from_encoding, const char* to_encoding) {
  rb_tr_error("rb_econv_has_convpath_p not implemented");
}

int rb_econv_prepare_options(VALUE opthash, VALUE *opts, int ecflags) {
  rb_tr_error("rb_econv_prepare_options not implemented");
}

int rb_econv_set_replacement(rb_econv_t *ec, const unsigned char *str, size_t len, const char *encname) {
  rb_tr_error("rb_econv_set_replacement not implemented");
}

int rb_econv_decorate_at_first(rb_econv_t *ec, const char *decorator_name) {
  rb_tr_error("rb_econv_decorate_at_first not implemented");
}

int rb_econv_decorate_at_last(rb_econv_t *ec, const char *decorator_name) {
  rb_tr_error("rb_econv_decorate_at_last not implemented");
}

VALUE rb_econv_open_exc(const char *sname, const char *dname, int ecflags) {
  rb_tr_error("rb_econv_open_exc not implemented");
}

int rb_econv_insert_output(rb_econv_t *ec, const unsigned char *str, size_t len, const char *str_encoding) {
  rb_tr_error("rb_econv_insert_output not implemented");
}

VALUE rb_econv_make_exception(rb_econv_t *ec) {
  rb_tr_error("rb_econv_make_exception not implemented");
}

int rb_econv_putbackable(rb_econv_t *ec) {
  rb_tr_error("rb_econv_putbackable not implemented");
}

void rb_econv_putback(rb_econv_t *ec, unsigned char *p, int n) {
  rb_tr_error("rb_econv_putback not implemented");
}

VALUE rb_econv_substr_convert(rb_econv_t *ec, VALUE src, long byteoff, long bytesize, int flags) {
  rb_tr_error("rb_econv_substr_convert not implemented");
}

VALUE rb_econv_str_append(rb_econv_t *ec, VALUE src, VALUE dst, int flags) {
  rb_tr_error("rb_econv_str_append not implemented");
}

VALUE rb_econv_substr_append(rb_econv_t *ec, VALUE src, long off, long len, VALUE dst, int flags) {
  rb_tr_error("rb_econv_substr_append not implemented");
}

VALUE rb_econv_append(rb_econv_t *ec, const char *ss, long len, VALUE dst, int flags) {
  rb_tr_error("rb_econv_append not implemented");
}

void rb_econv_binmode(rb_econv_t *ec) {
  rb_tr_error("rb_econv_binmode not implemented");
}

VALUE rb_ary_tmp_new(long capa) {
  return rb_ary_new_capa(capa);
}

void rb_ary_free(VALUE ary) {
  rb_tr_error("rb_ary_free not implemented");
}

void rb_ary_modify(VALUE ary) {
  rb_tr_error("rb_ary_modify not implemented");
}

VALUE rb_ary_shared_with_p(VALUE ary1, VALUE ary2) {
  rb_tr_error("rb_ary_shared_with_p not implemented");
}

VALUE rb_ary_resurrect(VALUE ary) {
  rb_tr_error("rb_ary_resurrect not implemented");
}

VALUE rb_ary_sort(VALUE ary) {
  rb_tr_error("rb_ary_sort not implemented");
}

VALUE rb_ary_sort_bang(VALUE ary) {
  rb_tr_error("rb_ary_sort_bang not implemented");
}

VALUE rb_ary_assoc(VALUE ary, VALUE key) {
  rb_tr_error("rb_ary_assoc not implemented");
}

VALUE rb_ary_rassoc(VALUE ary, VALUE value) {
  rb_tr_error("rb_ary_rassoc not implemented");
}

VALUE rb_ary_cmp(VALUE ary1, VALUE ary2) {
  rb_tr_error("rb_ary_cmp not implemented");
}

VALUE rb_ary_replace(VALUE copy, VALUE orig) {
  rb_tr_error("rb_ary_replace not implemented");
}

VALUE rb_get_values_at(VALUE obj, long olen, int argc, const VALUE *argv, VALUE (*func) (VALUE, long)) {
  rb_tr_error("rb_get_values_at not implemented");
}

VALUE rb_big_new(size_t len, int sign) {
  rb_tr_error("rb_big_new not implemented");
}

int rb_bigzero_p(VALUE x) {
  rb_tr_error("rb_bigzero_p not implemented");
}

VALUE rb_big_clone(VALUE x) {
  rb_tr_error("rb_big_clone not implemented");
}

VALUE rb_big_norm(VALUE x) {
  rb_tr_error("rb_big_norm not implemented");
}

void rb_big_resize(VALUE big, size_t len) {
  rb_tr_error("rb_big_resize not implemented");
}

VALUE rb_big_unpack(unsigned long *buf, long num_longs) {
  rb_tr_error("rb_big_unpack not implemented");
}

int rb_uv_to_utf8(char buf[6], unsigned long uv) {
  rb_tr_error("rb_uv_to_utf8 not implemented");
}

VALUE rb_big_eq(VALUE x, VALUE y) {
  rb_tr_error("rb_big_eq not implemented");
}

VALUE rb_big_eql(VALUE x, VALUE y) {
  rb_tr_error("rb_big_eql not implemented");
}

VALUE rb_big_plus(VALUE x, VALUE y) {
  rb_tr_error("rb_big_plus not implemented");
}

VALUE rb_big_minus(VALUE x, VALUE y) {
  rb_tr_error("rb_big_minus not implemented");
}

VALUE rb_big_mul(VALUE x, VALUE y) {
  rb_tr_error("rb_big_mul not implemented");
}

VALUE rb_big_div(VALUE x, VALUE y) {
  rb_tr_error("rb_big_div not implemented");
}

VALUE rb_big_idiv(VALUE x, VALUE y) {
  rb_tr_error("rb_big_idiv not implemented");
}

VALUE rb_big_modulo(VALUE x, VALUE y) {
  rb_tr_error("rb_big_modulo not implemented");
}

VALUE rb_big_divmod(VALUE x, VALUE y) {
  rb_tr_error("rb_big_divmod not implemented");
}

VALUE rb_big_pow(VALUE x, VALUE y) {
  rb_tr_error("rb_big_pow not implemented");
}

VALUE rb_big_and(VALUE x, VALUE y) {
  rb_tr_error("rb_big_and not implemented");
}

VALUE rb_big_or(VALUE x, VALUE y) {
  rb_tr_error("rb_big_or not implemented");
}

VALUE rb_big_xor(VALUE x, VALUE y) {
  rb_tr_error("rb_big_xor not implemented");
}

VALUE rb_big_lshift(VALUE x, VALUE y) {
  rb_tr_error("rb_big_lshift not implemented");
}

VALUE rb_big_rshift(VALUE x, VALUE y) {
  rb_tr_error("rb_big_rshift not implemented");
}

VALUE rb_big_hash(VALUE x) {
  rb_tr_error("rb_big_hash not implemented");
}

size_t rb_absint_numwords(VALUE val, size_t word_numbits, size_t *nlz_bits_ret) {
  rb_tr_error("rb_absint_numwords not implemented");
}

int rb_absint_singlebit_p(VALUE val) {
  return polyglot_as_i32(RUBY_CEXT_INVOKE_NO_WRAP("rb_absint_singlebit_p", val));
}

VALUE rb_class_boot(VALUE super) {
  rb_tr_error("rb_class_boot not implemented");
}

VALUE rb_mod_init_copy(VALUE clone, VALUE orig) {
  rb_tr_error("rb_mod_init_copy not implemented");
}

VALUE rb_singleton_class_clone(VALUE obj) {
  rb_tr_error("rb_singleton_class_clone not implemented");
}

void rb_singleton_class_attached(VALUE klass, VALUE obj) {
  rb_tr_error("rb_singleton_class_attached not implemented");
}

VALUE rb_make_metaclass(VALUE obj, VALUE unused) {
  rb_tr_error("rb_make_metaclass not implemented");
}

void rb_check_inheritable(VALUE super) {
  rb_tr_error("rb_check_inheritable not implemented");
}

VALUE rb_class_inherited(VALUE super, VALUE klass) {
  rb_tr_error("rb_class_inherited not implemented");
}

VALUE rb_define_class_id(ID id, VALUE super) {
  rb_tr_error("rb_define_class_id not implemented");
}

VALUE rb_module_new(void) {
  rb_tr_error("rb_module_new not implemented");
}

VALUE rb_define_module_id(ID id) {
  rb_tr_error("rb_define_module_id not implemented");
}

VALUE rb_define_module_id_under(VALUE outer, ID id) {
  rb_tr_error("rb_define_module_id_under not implemented");
}

VALUE rb_include_class_new(VALUE module, VALUE super) {
  rb_tr_error("rb_include_class_new not implemented");
}

VALUE rb_mod_included_modules(VALUE mod) {
  rb_tr_error("rb_mod_included_modules not implemented");
}

VALUE rb_mod_include_p(VALUE mod, VALUE mod2) {
  rb_tr_error("rb_mod_include_p not implemented");
}

VALUE rb_class_instance_methods(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_class_instance_methods not implemented");
}

VALUE rb_class_public_instance_methods(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_class_public_instance_methods not implemented");
}

VALUE rb_class_protected_instance_methods(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_class_protected_instance_methods not implemented");
}

VALUE rb_class_private_instance_methods(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_class_private_instance_methods not implemented");
}

VALUE rb_obj_singleton_methods(int argc, const VALUE *argv, VALUE obj) {
  rb_tr_error("rb_obj_singleton_methods not implemented");
}

void rb_define_method_id(VALUE klass, ID mid, VALUE (*func)(ANYARGS), int argc) {
  rb_tr_error("rb_define_method_id not implemented");
}

void rb_frozen_class_p(VALUE klass) {
  rb_tr_error("rb_frozen_class_p not implemented");
}

VALUE rb_fiber_new(VALUE (*func)(ANYARGS), VALUE obj) {
  rb_tr_error("rb_fiber_new not implemented");
}

VALUE rb_fiber_resume(VALUE fibval, int argc, const VALUE *argv) {
  rb_tr_error("rb_fiber_resume not implemented");
}

VALUE rb_fiber_yield(int argc, const VALUE *argv) {
  rb_tr_error("rb_fiber_yield not implemented");
}

VALUE rb_fiber_current(void) {
  rb_tr_error("rb_fiber_current not implemented");
}

VALUE rb_fiber_alive_p(VALUE fibval) {
  rb_tr_error("rb_fiber_alive_p not implemented");
}

VALUE rb_enum_values_pack(int argc, const VALUE *argv) {
  rb_tr_error("rb_enum_values_pack not implemented");
}


NORETURN(void rb_error_arity(int argc, int min, int max)) {
  rb_exc_raise(rb_exc_new3(rb_eArgError, rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_arity_error_string", argc, min, max))));
}

void rb_error_untrusted(VALUE obj) {
  rb_tr_error("rb_error_untrusted not implemented");
}

void rb_check_trusted(VALUE obj) {
  // This function intentionally does nothing to match MRI.
}

void rb_check_copyable(VALUE obj, VALUE orig) {
  rb_tr_error("rb_check_copyable not implemented");
}

VALUE rb_check_funcall(VALUE recv, ID mid, int argc, const VALUE *argv) {
  rb_tr_error("rb_check_funcall not implemented");
}

void rb_fd_init(rb_fdset_t *set) {
  rb_tr_error("rb_fd_init not implemented");
}

void rb_fd_term(rb_fdset_t *set) {
  rb_tr_error("rb_fd_term not implemented");
}

void rb_fd_zero(rb_fdset_t *fds) {
  rb_tr_error("rb_fd_zero not implemented");
}

void rb_fd_set(int fd, rb_fdset_t *set) {
  rb_tr_error("rb_fd_set not implemented");
}

void rb_fd_clr(int n, rb_fdset_t *fds) {
  rb_tr_error("rb_fd_clr not implemented");
}

int rb_fd_isset(int n, const rb_fdset_t *fds) {
  rb_tr_error("rb_fd_isset not implemented");
}

void rb_fd_copy(rb_fdset_t *dst, const fd_set *src, int max) {
  rb_tr_error("rb_fd_copy not implemented");
}

void rb_fd_dup(rb_fdset_t *dst, const rb_fdset_t *src) {
  rb_tr_error("rb_fd_dup not implemented");
}

int rb_fd_select(int n, rb_fdset_t *readfds, rb_fdset_t *writefds, rb_fdset_t *exceptfds, struct timeval *timeout) {
  rb_tr_error("rb_fd_select not implemented");
}

void rb_w32_fd_copy(rb_fdset_t *dst, const fd_set *src, int max) {
  rb_tr_error("rb_w32_fd_copy not implemented");
}

void rb_w32_fd_dup(rb_fdset_t *dst, const rb_fdset_t *src) {
  rb_tr_error("rb_w32_fd_dup not implemented");
}

VALUE rb_f_exit(int argc, const VALUE *argv) {
  rb_tr_error("rb_f_exit not implemented");
}

VALUE rb_f_abort(int argc, const VALUE *argv) {
  rb_tr_error("rb_f_abort not implemented");
}

void rb_remove_method(VALUE klass, const char *name) {
  rb_tr_error("rb_remove_method not implemented");
}

void rb_remove_method_id(VALUE klass, ID mid) {
  rb_tr_error("rb_remove_method_id not implemented");
}

rb_alloc_func_t rb_get_alloc_func(VALUE klass) {
  rb_tr_error("rb_get_alloc_func not implemented");
}

void rb_clear_constant_cache(void) {
  rb_tr_error("rb_clear_constant_cache not implemented");
}

void rb_clear_method_cache_by_class(VALUE klass) {
  rb_tr_error("rb_clear_method_cache_by_class not implemented");
}

int rb_method_basic_definition_p(VALUE klass, ID id) {
  rb_tr_error("rb_method_basic_definition_p not implemented");
}

VALUE rb_eval_cmd(VALUE cmd, VALUE arg, int level) {
  rb_tr_error("rb_eval_cmd not implemented");
}

void rb_interrupt(void) {
  rb_tr_error("rb_interrupt not implemented");
}

void rb_backtrace(void) {
  rb_tr_error("rb_backtrace not implemented");
}

ID rb_frame_this_func(void) {
  return SYM2ID(RUBY_CEXT_INVOKE("rb_frame_this_func"));
}

VALUE rb_obj_instance_exec(int argc, const VALUE *argv, VALUE self) {
  rb_tr_error("rb_obj_instance_exec not implemented");
}

VALUE rb_mod_module_eval(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_mod_module_eval not implemented");
}

VALUE rb_mod_module_exec(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_mod_module_exec not implemented");
}

void rb_load(VALUE fname, int wrap) {
  rb_tr_error("rb_load not implemented");
}

void rb_load_protect(VALUE fname, int wrap, int *state) {
  rb_tr_error("rb_load_protect not implemented");
}

int rb_provided(const char *feature) {
  rb_tr_error("rb_provided not implemented");
}

int rb_feature_provided(const char *feature, const char **loading) {
  rb_tr_error("rb_feature_provided not implemented");
}

void rb_provide(const char *feature) {
  rb_tr_error("rb_provide not implemented");
}

VALUE rb_f_require(VALUE obj, VALUE fname) {
  rb_tr_error("rb_f_require not implemented");
}

VALUE rb_require_safe(VALUE fname, int safe) {
  rb_tr_error("rb_require_safe not implemented");
}

VALUE rb_obj_is_proc(VALUE proc) {
  rb_tr_error("rb_obj_is_proc not implemented");
}

VALUE rb_proc_call_with_block(VALUE self, int argc, const VALUE *argv, VALUE pass_procval) {
  rb_tr_error("rb_proc_call_with_block not implemented");
}

VALUE rb_proc_lambda_p(VALUE procval) {
  rb_tr_error("rb_proc_lambda_p not implemented");
}

VALUE rb_binding_new(void) {
  rb_tr_error("rb_binding_new not implemented");
}

VALUE rb_obj_is_method(VALUE m) {
  rb_tr_error("rb_obj_is_method not implemented");
}

VALUE rb_method_call(int argc, const VALUE *argv, VALUE method) {
  rb_tr_error("rb_method_call not implemented");
}

VALUE rb_method_call_with_block(int argc, const VALUE *argv, VALUE method, VALUE pass_procval) {
  rb_tr_error("rb_method_call_with_block not implemented");
}

int rb_mod_method_arity(VALUE mod, ID id) {
  rb_tr_error("rb_mod_method_arity not implemented");
}

void rb_exec_end_proc(void) {
  rb_tr_error("rb_exec_end_proc not implemented");
}

void rb_thread_fd_close(int fd) {
  rb_tr_error("rb_thread_fd_close not implemented");
}

void rb_thread_sleep(int sec) {
  rb_tr_error("rb_thread_sleep not implemented");
}

void rb_thread_sleep_forever(void) {
  rb_tr_error("rb_thread_sleep_forever not implemented");
}

void rb_thread_sleep_deadly(void) {
  rb_tr_error("rb_thread_sleep_deadly not implemented");
}

VALUE rb_thread_stop(void) {
  rb_tr_error("rb_thread_stop not implemented");
}

VALUE rb_thread_wakeup_alive(VALUE thread) {
  rb_tr_error("rb_thread_wakeup_alive not implemented");
}

VALUE rb_thread_run(VALUE thread) {
  rb_tr_error("rb_thread_run not implemented");
}

VALUE rb_thread_kill(VALUE thread) {
  rb_tr_error("rb_thread_kill not implemented");
}

int rb_thread_fd_select(int max, rb_fdset_t * read, rb_fdset_t * write, rb_fdset_t * except, struct timeval *timeout) {
  rb_tr_error("rb_thread_fd_select not implemented");
}

VALUE rb_thread_main(void) {
  rb_tr_error("rb_thread_main not implemented");
}

void rb_thread_atfork(void) {
  rb_tr_error("rb_thread_atfork not implemented");
}

void rb_thread_atfork_before_exec(void) {
  rb_tr_error("rb_thread_atfork_before_exec not implemented");
}

VALUE rb_exec_recursive_paired(VALUE (*func) (VALUE, VALUE, int), VALUE obj, VALUE paired_obj, VALUE arg) {
  rb_tr_error("rb_exec_recursive_paired not implemented");
}

VALUE rb_exec_recursive_outer(VALUE (*func) (VALUE, VALUE, int), VALUE obj, VALUE arg) {
  rb_tr_error("rb_exec_recursive_outer not implemented");
}

VALUE rb_exec_recursive_paired_outer(VALUE (*func) (VALUE, VALUE, int), VALUE obj, VALUE paired_obj, VALUE arg) {
  rb_tr_error("rb_exec_recursive_paired_outer not implemented");
}

VALUE rb_dir_getwd(void) {
  rb_tr_error("rb_dir_getwd not implemented");
}

VALUE rb_file_s_expand_path(int argc, const VALUE *argv) {
  rb_tr_error("rb_file_s_expand_path not implemented");
}

VALUE rb_file_expand_path(VALUE fname, VALUE dname) {
  rb_tr_error("rb_file_expand_path not implemented");
}

VALUE rb_file_s_absolute_path(int argc, const VALUE *argv) {
  rb_tr_error("rb_file_s_absolute_path not implemented");
}

VALUE rb_file_absolute_path(VALUE fname, VALUE dname) {
  rb_tr_error("rb_file_absolute_path not implemented");
}

VALUE rb_file_dirname(VALUE fname) {
  rb_tr_error("rb_file_dirname not implemented");
}

int rb_find_file_ext_safe(VALUE *filep, const char *const *ext, int safe_level) {
  rb_tr_error("rb_find_file_ext_safe not implemented");
}

VALUE rb_find_file_safe(VALUE path, int safe_level) {
  rb_tr_error("rb_find_file_safe not implemented");
}

int rb_find_file_ext(VALUE *filep, const char *const *ext) {
  rb_tr_error("rb_find_file_ext not implemented");
}

VALUE rb_find_file(VALUE path) {
  rb_tr_error("rb_find_file not implemented");
}

VALUE rb_file_directory_p(VALUE obj, VALUE fname) {
  rb_tr_error("rb_file_directory_p not implemented");
}

VALUE rb_str_encode_ospath(VALUE path) {
  rb_tr_error("rb_str_encode_ospath not implemented");
}

int rb_is_absolute_path(const char *path) {
  rb_tr_error("rb_is_absolute_path not implemented");
}

int rb_during_gc(void) {
  rb_tr_error("rb_during_gc not implemented");
}

void rb_gc_mark_locations(const VALUE *start, const VALUE *end) {
  rb_tr_error("rb_gc_mark_locations not implemented");
}

void rb_mark_tbl(st_table *tbl) {
  rb_tr_error("rb_mark_tbl not implemented");
}

void rb_mark_set(st_table *tbl) {
  rb_tr_error("rb_mark_set not implemented");
}

void rb_mark_hash(st_table *tbl) {
  rb_tr_error("rb_mark_hash not implemented");
}

void rb_gc_force_recycle(VALUE obj) {
  // Comments in MRI imply rb_gc_force_recycle functions as a GC guard
  RB_GC_GUARD(obj);
}

void rb_gc_copy_finalizer(VALUE dest, VALUE obj) {
  rb_tr_error("rb_gc_copy_finalizer not implemented");
}

void rb_gc_finalize_deferred(void) {
  rb_tr_error("rb_gc_finalize_deferred not implemented");
}

void rb_gc_call_finalizer_at_exit(void) {
  rb_tr_error("rb_gc_call_finalizer_at_exit not implemented");
}

VALUE rb_gc_start(void) {
  rb_tr_error("rb_gc_start not implemented");
}

VALUE rb_define_finalizer(VALUE obj, VALUE block) {
  rb_tr_error("rb_define_finalizer not implemented");
}

VALUE rb_undefine_finalizer(VALUE obj) {
  rb_tr_error("rb_undefine_finalizer not implemented");
}

size_t rb_gc_count(void) {
  rb_tr_error("rb_gc_count not implemented");
}

size_t rb_gc_stat(VALUE key) {
  rb_tr_error("rb_gc_stat not implemented");
}

VALUE rb_gc_latest_gc_info(VALUE key) {
  rb_tr_error("rb_gc_latest_gc_info not implemented");
}

VALUE rb_check_hash_type(VALUE hash) {
  return rb_check_convert_type(hash, T_HASH, "Hash", "to_hash");
}

VALUE rb_hash_update_by(VALUE hash1, VALUE hash2, rb_hash_update_func *func) {
  rb_tr_error("rb_hash_update_by not implemented");
}

int rb_path_check(const char *path) {
  rb_tr_error("rb_path_check not implemented");
}

int rb_env_path_tainted(void) {
  rb_tr_error("rb_env_path_tainted not implemented");
}

VALUE rb_env_clear(void) {
  rb_tr_error("rb_env_clear not implemented");
}

VALUE rb_io_gets(VALUE io) {
  rb_tr_error("rb_io_gets not implemented");
}

VALUE rb_io_getbyte(VALUE io) {
  rb_tr_error("rb_io_getbyte not implemented");
}

VALUE rb_io_ungetc(VALUE io, VALUE c) {
  rb_tr_error("rb_io_ungetc not implemented");
}

VALUE rb_io_ungetbyte(VALUE io, VALUE b) {
  rb_tr_error("rb_io_ungetbyte not implemented");
}

VALUE rb_io_flush(VALUE io) {
  rb_tr_error("rb_io_flush not implemented");
}

VALUE rb_io_eof(VALUE io) {
  rb_tr_error("rb_io_eof not implemented");
}

VALUE rb_io_ascii8bit_binmode(VALUE io) {
  rb_tr_error("rb_io_ascii8bit_binmode not implemented");
}

VALUE rb_io_fdopen(int fd, int oflags, const char *path) {
  rb_tr_error("rb_io_fdopen not implemented");
}

VALUE rb_io_get_io(VALUE io) {
  rb_tr_error("rb_io_get_io not implemented");
}

VALUE rb_gets(void) {
  rb_tr_error("rb_gets not implemented");
}

void rb_write_error(const char *mesg) {
  rb_tr_error("rb_write_error not implemented");
}

void rb_write_error2(const char *mesg, long len) {
  rb_tr_error("rb_write_error2 not implemented");
}

void rb_close_before_exec(int lowfd, int maxhint, VALUE noclose_fds) {
  rb_tr_error("rb_close_before_exec not implemented");
}

int rb_pipe(int *pipes) {
  rb_tr_error("rb_pipe not implemented");
}

int rb_reserved_fd_p(int fd) {
  rb_tr_error("rb_reserved_fd_p not implemented");
}

int rb_cloexec_dup2(int oldfd, int newfd) {
  rb_tr_error("rb_cloexec_dup2 not implemented");
}

int rb_cloexec_pipe(int fildes[2]) {
  rb_tr_error("rb_cloexec_pipe not implemented");
}

int rb_cloexec_fcntl_dupfd(int fd, int minfd) {
  rb_tr_error("rb_cloexec_fcntl_dupfd not implemented");
}

void rb_marshal_define_compat(VALUE newclass, VALUE oldclass, VALUE (*dumper)(VALUE), VALUE (*loader)(VALUE, VALUE)) {
  rb_tr_error("rb_marshal_define_compat not implemented");
}

VALUE rb_num_coerce_bit(VALUE x, VALUE y, ID func) {
  rb_tr_error("rb_num_coerce_bit not implemented");
}

VALUE rb_num2fix(VALUE val) {
  rb_tr_error("rb_num2fix not implemented");
}

VALUE rb_fix2str(VALUE x, int base) {
  return RUBY_CEXT_INVOKE("rb_fix2str", x, INT2FIX(base));
}

VALUE rb_dbl_cmp(double a, double b) {
  rb_tr_error("rb_dbl_cmp not implemented");
}

int rb_eql(VALUE obj1, VALUE obj2) {
  rb_tr_error("rb_eql not implemented");
}

VALUE rb_obj_clone(VALUE obj) {
  return rb_funcall(obj, rb_intern("clone"), 0);
}

VALUE rb_obj_init_copy(VALUE obj, VALUE orig) {
  rb_tr_error("rb_obj_init_copy not implemented");
}

VALUE rb_obj_tainted(VALUE obj) {
  return rb_funcall(obj, rb_intern("tainted?"), 0);
}

VALUE rb_obj_untaint(VALUE obj) {
  return rb_funcall(obj, rb_intern("untaint"), 0);
}

VALUE rb_obj_untrust(VALUE obj) {
  return rb_funcall(obj, rb_intern("untrust"), 0);
}

VALUE rb_obj_untrusted(VALUE obj) {
  return rb_funcall(obj, rb_intern("untrusted?"), 0);
}

VALUE rb_obj_trust(VALUE obj) {
  return rb_funcall(obj, rb_intern("trust"), 0);
}

VALUE rb_class_get_superclass(VALUE klass) {
  rb_tr_error("rb_class_get_superclass not implemented");
}

VALUE rb_check_to_float(VALUE val) {
  rb_tr_error("rb_check_to_float not implemented");
}

VALUE rb_check_to_int(VALUE val) {
  rb_tr_error("rb_check_to_int not implemented");
}

VALUE rb_to_float(VALUE val) {
  rb_tr_error("rb_to_float not implemented");
}

double rb_str_to_dbl(VALUE str, int badcheck) {
  rb_tr_error("rb_str_to_dbl not implemented");
}

ID rb_id_attrset(ID id) {
  rb_tr_error("rb_id_attrset not implemented");
}

int rb_is_global_id(ID id) {
  rb_tr_error("rb_is_global_id not implemented");
}

int rb_is_attrset_id(ID id) {
  rb_tr_error("rb_is_attrset_id not implemented");
}

int rb_is_local_id(ID id) {
  rb_tr_error("rb_is_local_id not implemented");
}

int rb_is_junk_id(ID id) {
  rb_tr_error("rb_is_junk_id not implemented");
}

int rb_symname_p(const char *name) {
  rb_tr_error("rb_symname_p not implemented");
}

void rb_backref_set(VALUE val) {
  rb_tr_error("rb_backref_set not implemented");
}

void rb_last_status_set(int status, rb_pid_t pid) {
  rb_tr_error("rb_last_status_set not implemented");
}

VALUE rb_last_status_get(void) {
  rb_tr_error("rb_last_status_get not implemented");
}

int rb_proc_exec(const char *str) {
  rb_tr_error("rb_proc_exec not implemented");
}

VALUE rb_f_exec(int argc, const VALUE *argv) {
  rb_tr_error("rb_f_exec not implemented");
}

rb_pid_t rb_waitpid(rb_pid_t pid, int *st, int flags) {
  rb_tr_error("rb_waitpid not implemented");
}

void rb_syswait(rb_pid_t pid) {
  rb_tr_error("rb_syswait not implemented");
}

rb_pid_t rb_spawn(int argc, const VALUE *argv) {
  rb_tr_error("rb_spawn not implemented");
}

rb_pid_t rb_spawn_err(int argc, const VALUE *argv, char *errmsg, size_t errmsg_buflen) {
  rb_tr_error("rb_spawn_err not implemented");
}

VALUE rb_proc_times(VALUE obj) {
  rb_tr_error("rb_proc_times not implemented");
}

VALUE rb_detach_process(rb_pid_t pid) {
  rb_tr_error("rb_detach_process not implemented");
}

double rb_genrand_real(void) {
  rb_tr_error("rb_genrand_real not implemented");
}

void rb_reset_random_seed(void) {
  rb_tr_error("rb_reset_random_seed not implemented");
}

VALUE rb_random_bytes(VALUE obj, long n) {
  rb_tr_error("rb_random_bytes not implemented");
}

double rb_random_real(VALUE obj) {
  rb_tr_error("rb_random_real not implemented");
}

int rb_memcicmp(const void *x, const void *y, long len) {
  rb_tr_error("rb_memcicmp not implemented");
}

void rb_match_busy(VALUE match) {
  rb_tr_error("rb_match_busy not implemented");
}

VALUE rb_reg_nth_defined(int nth, VALUE match) {
  rb_tr_error("rb_reg_nth_defined not implemented");
}

int rb_reg_backref_number(VALUE match, VALUE backref) {
  rb_tr_error("rb_reg_backref_number not implemented");
}

VALUE rb_reg_last_match(VALUE match) {
  rb_tr_error("rb_reg_last_match not implemented");
}

VALUE rb_reg_match_post(VALUE match) {
  rb_tr_error("rb_reg_match_post not implemented");
}

VALUE rb_reg_match_last(VALUE match) {
  rb_tr_error("rb_reg_match_last not implemented");
}

VALUE rb_reg_alloc(void) {
  rb_tr_error("rb_reg_alloc not implemented");
}

VALUE rb_reg_init_str(VALUE re, VALUE s, int options) {
  rb_tr_error("rb_reg_init_str not implemented");
}

VALUE rb_reg_match2(VALUE re) {
  rb_tr_error("rb_reg_match2 not implemented");
}

VALUE rb_get_argv(void) {
  rb_tr_error("rb_get_argv not implemented");
}

VALUE rb_f_kill(int argc, const VALUE *argv) {
  rb_tr_error("rb_f_kill not implemented");
}

void rb_trap_exit(void) {
  rb_tr_error("rb_trap_exit not implemented");
}

VALUE rb_str_vcatf(VALUE str, const char *fmt, va_list ap) {
  rb_tr_error("rb_str_vcatf not implemented");
}

VALUE rb_str_format(int argc, const VALUE *argv, VALUE fmt) {
  rb_tr_error("rb_str_format not implemented");
}

VALUE rb_str_tmp_new(long len) {
  rb_tr_error("rb_str_tmp_new not implemented");
}

#undef rb_utf8_str_new
VALUE rb_utf8_str_new(const char *ptr, long len) {
  return rb_enc_str_new(ptr, len, rb_utf8_encoding());
}

#undef rb_utf8_str_new_cstr
VALUE rb_utf8_str_new_cstr(const char *ptr) {
  // TODO CS 11-Oct-19 would be nice to read in one go rather than strlen followed by read
  return rb_utf8_str_new(ptr, strlen(ptr));
}

VALUE rb_str_new_static(const char *ptr, long len) {
  rb_tr_error("rb_str_new_static not implemented");
}

VALUE rb_usascii_str_new_static(const char *ptr, long len) {
  rb_tr_error("rb_usascii_str_new_static not implemented");
}

VALUE rb_utf8_str_new_static(const char *ptr, long len) {
  return rb_utf8_str_new(ptr, len);
}

void rb_str_shared_replace(VALUE str, VALUE str2) {
  rb_tr_error("rb_str_shared_replace not implemented");
}

VALUE rb_str_buf_append(VALUE str, VALUE str2) {
  rb_tr_error("rb_str_buf_append not implemented");
}

VALUE rb_str_buf_cat_ascii(VALUE str, const char *ptr) {
  rb_tr_error("rb_str_buf_cat_ascii not implemented");
}

VALUE rb_str_locktmp(VALUE str) {
  rb_tr_error("rb_str_locktmp not implemented");
}

VALUE rb_str_unlocktmp(VALUE str) {
  rb_tr_error("rb_str_unlocktmp not implemented");
}

long rb_str_sublen(VALUE str, long pos) {
  rb_tr_error("rb_str_sublen not implemented");
}

void rb_str_modify_expand(VALUE str, long expand) {
  long len = RSTRING_LEN(str);
  if (expand < 0) {
    rb_raise(rb_eArgError, "negative expanding string size");
  }
  if (expand > INT_MAX - len) {
    rb_raise(rb_eArgError, "string size too big");
  }

  if (expand > 0) {
    // rb_str_modify_expand() resizes the native buffer but does not change
    // RSTRING_LEN() (and therefore String#bytesize).
    // TODO (eregon, 26 Apr 2018): Do this more directly.
    rb_str_resize(str, len + expand);
    rb_str_set_len(str, len);
  }

  ENC_CODERANGE_CLEAR(str);
}

#undef rb_str_cat_cstr
VALUE rb_str_cat_cstr(VALUE str, const char *ptr) {
  rb_tr_error("rb_str_cat_cstr not implemented");
}

st_index_t rb_hash_start(st_index_t h) {
  rb_tr_error("rb_hash_start not implemented");
}

int rb_str_hash_cmp(VALUE str1, VALUE str2) {
  rb_tr_error("rb_str_hash_cmp not implemented");
}

int rb_str_comparable(VALUE str1, VALUE str2) {
  rb_tr_error("rb_str_comparable not implemented");
}

VALUE rb_str_drop_bytes(VALUE str, long len) {
  long olen = RSTRING_LEN(str);
  if (len > olen) {
    len = olen;
  }
  return rb_str_replace(str, rb_str_subseq(str, len, olen - len));
}

VALUE rb_str_dump(VALUE str) {
  rb_tr_error("rb_str_dump not implemented");
}

void rb_str_setter(VALUE val, ID id, VALUE *var) {
  rb_tr_error("rb_str_setter not implemented");
}

VALUE rb_sym_to_s(VALUE sym) {
  return RUBY_INVOKE(sym, "to_s");
}

long rb_str_strlen(VALUE str) {
  rb_tr_error("rb_str_strlen not implemented");
}

long rb_str_offset(VALUE str, long pos) {
  rb_tr_error("rb_str_offset not implemented");
}

size_t rb_str_capacity(VALUE str) {
  return polyglot_as_i64(RUBY_CEXT_INVOKE_NO_WRAP("rb_str_capacity", str));
}

VALUE rb_str_ellipsize(VALUE str, long len) {
  rb_tr_error("rb_str_ellipsize not implemented");
}

VALUE rb_str_scrub(VALUE str, VALUE repl) {
  rb_tr_error("rb_str_scrub not implemented");
}

VALUE rb_sym_all_symbols(void) {
  rb_tr_error("rb_sym_all_symbols not implemented");
}

VALUE rb_struct_alloc(VALUE klass, VALUE values) {
  rb_tr_error("rb_struct_alloc not implemented");
}

VALUE rb_struct_initialize(VALUE self, VALUE values) {
  rb_tr_error("rb_struct_initialize not implemented");
}

VALUE rb_struct_alloc_noinit(VALUE klass) {
  rb_tr_error("rb_struct_alloc_noinit not implemented");
}

VALUE rb_struct_define_without_accessor(const char *class_name, VALUE super, rb_alloc_func_t alloc, ...) {
  rb_tr_error("rb_struct_define_without_accessor not implemented");
}

VALUE rb_struct_define_without_accessor_under(VALUE outer, const char *class_name, VALUE super, rb_alloc_func_t alloc, ...) {
  rb_tr_error("rb_struct_define_without_accessor_under not implemented");
}

void rb_thread_check_ints(void) {
  rb_tr_error("rb_thread_check_ints not implemented");
}

int rb_thread_interrupted(VALUE thval) {
  rb_tr_error("rb_thread_interrupted not implemented");
}

VALUE rb_mod_name(VALUE mod) {
  rb_tr_error("rb_mod_name not implemented");
}

VALUE rb_class_path_cached(VALUE klass) {
  rb_tr_error("rb_class_path_cached not implemented");
}

void rb_set_class_path(VALUE klass, VALUE under, const char *name) {
  rb_tr_error("rb_set_class_path not implemented");
}

void rb_set_class_path_string(VALUE klass, VALUE under, VALUE name) {
  rb_tr_error("rb_set_class_path_string not implemented");
}

void rb_name_class(VALUE klass, ID id) {
  rb_tr_error("rb_name_class not implemented");
}

VALUE rb_autoload_load(VALUE mod, ID id) {
  rb_tr_error("rb_autoload_load not implemented");
}

VALUE rb_autoload_p(VALUE mod, ID id) {
  rb_tr_error("rb_autoload_p not implemented");
}

VALUE rb_f_trace_var(int argc, const VALUE *argv) {
  rb_tr_error("rb_f_trace_var not implemented");
}

VALUE rb_f_untrace_var(int argc, const VALUE *argv) {
  rb_tr_error("rb_f_untrace_var not implemented");
}

void rb_alias_variable(ID name1, ID name2) {
  rb_tr_error("rb_alias_variable not implemented");
}

void rb_copy_generic_ivar(VALUE clone, VALUE obj) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_copy_generic_ivar", clone, obj);
}

void rb_free_generic_ivar(VALUE obj) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_free_generic_ivar", obj);
}

void rb_ivar_foreach(VALUE obj, int (*func)(ANYARGS), st_data_t arg) {
  rb_tr_error("rb_ivar_foreach not implemented");
}

st_index_t rb_ivar_count(VALUE obj) {
  rb_tr_error("rb_ivar_count not implemented");
}

VALUE rb_obj_remove_instance_variable(VALUE obj, VALUE name) {
  rb_tr_error("rb_obj_remove_instance_variable not implemented");
}

VALUE rb_const_list(void *data) {
  rb_tr_error("rb_const_list not implemented");
}

VALUE rb_mod_constants(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_mod_constants not implemented");
}

VALUE rb_mod_remove_const(VALUE mod, VALUE name) {
  rb_tr_error("rb_mod_remove_const not implemented");
}

int rb_const_defined_from(VALUE klass, ID id) {
  rb_tr_error("rb_const_defined_from not implemented");
}

VALUE rb_const_remove(VALUE mod, ID id) {
  rb_tr_error("rb_const_remove not implemented");
}

VALUE rb_mod_const_missing(VALUE klass, VALUE name) {
  rb_tr_error("rb_mod_const_missing not implemented");
}

VALUE rb_mod_class_variables(int argc, const VALUE *argv, VALUE mod) {
  rb_tr_error("rb_mod_class_variables not implemented");
}

VALUE rb_mod_remove_cvar(VALUE mod, VALUE name) {
  rb_tr_error("rb_mod_remove_cvar not implemented");
}

ID rb_frame_callee(void) {
  rb_tr_error("rb_frame_callee not implemented");
}

VALUE rb_str_succ(VALUE orig) {
  rb_tr_error("rb_str_succ not implemented");
}

VALUE rb_time_succ(VALUE time) {
  rb_tr_error("rb_time_succ not implemented");
}

int rb_frame_method_id_and_class(ID *idp, VALUE *klassp) {
  rb_tr_error("rb_frame_method_id_and_class not implemented");
}

VALUE rb_make_exception(int argc, const VALUE *argv) {
  rb_tr_error("rb_make_exception not implemented");
}

int rb_io_modestr_fmode(const char *modestr) {
  rb_tr_error("rb_io_modestr_fmode not implemented");
}

int rb_io_modestr_oflags(const char *modestr) {
  rb_tr_error("rb_io_modestr_oflags not implemented");
}

int rb_io_oflags_fmode(int oflags) {
  rb_tr_error("rb_io_oflags_fmode not implemented");
}

void rb_io_check_char_readable(rb_io_t *fptr) {
  rb_tr_error("rb_io_check_char_readable not implemented");
}

void rb_io_check_byte_readable(rb_io_t *fptr) {
  rb_tr_error("rb_io_check_byte_readable not implemented");
}

int rb_io_fptr_finalize(rb_io_t *fptr) {
  rb_tr_error("rb_io_fptr_finalize not implemented");
}

void rb_io_synchronized(rb_io_t *fptr) {
  rb_tr_error("rb_io_synchronized not implemented");
}

void rb_io_check_initialized(rb_io_t *fptr) {
  rb_tr_error("rb_io_check_initialized not implemented");
}

VALUE rb_io_get_write_io(VALUE io) {
  rb_tr_error("rb_io_get_write_io not implemented");
}

VALUE rb_io_set_write_io(VALUE io, VALUE w) {
  rb_tr_error("rb_io_set_write_io not implemented");
}

void rb_io_set_nonblock(rb_io_t *fptr) {
  rb_tr_error("rb_io_set_nonblock not implemented");
}

ssize_t rb_io_bufwrite(VALUE io, const void *buf, size_t size) {
  rb_tr_error("rb_io_bufwrite not implemented");
}

void rb_io_read_check(rb_io_t *fptr) {
  rb_tr_error("rb_io_read_check not implemented");
}

int rb_io_read_pending(rb_io_t *fptr) {
  rb_tr_error("rb_io_read_pending not implemented");
}

VALUE rb_stat_new(const struct stat *st) {
  rb_tr_error("rb_stat_new not implemented");
}

long rb_reg_search(VALUE re, VALUE str, long pos, int reverse) {
  rb_tr_error("rb_reg_search not implemented");
}

VALUE rb_reg_regsub(VALUE str, VALUE src, struct re_registers *regs, VALUE regexp) {
  rb_tr_error("rb_reg_regsub not implemented");
}

long rb_reg_adjust_startpos(VALUE re, VALUE str, long pos, int reverse) {
  rb_tr_error("rb_reg_adjust_startpos not implemented");
}

VALUE rb_reg_quote(VALUE str) {
  rb_tr_error("rb_reg_quote not implemented");
}

int rb_reg_region_copy(struct re_registers *to, const struct re_registers *from) {
  rb_tr_error("rb_reg_region_copy not implemented");
}

ID rb_sym2id(VALUE sym) {
  return (ID) sym;
}

VALUE rb_id2sym(ID x) {
  return (VALUE) x;
}

VALUE rb_get_path_no_checksafe(VALUE obj) {
  rb_tr_error("rb_get_path_no_checksafe not implemented");
}

void rb_secure_update(VALUE obj) {
  rb_tr_error("rb_secure_update not implemented");
}

VALUE rb_uint2big(uintptr_t n) {
  rb_tr_error("rb_uint2big not implemented");
}

VALUE rb_int2big(intptr_t n) {
  // it cannot overflow Fixnum
  return LONG2FIX(n);
}

VALUE rb_newobj(void) {
  rb_tr_error("rb_newobj not implemented");
}

VALUE rb_newobj_of(VALUE klass, VALUE flags) {
  rb_tr_error("rb_newobj_of not implemented");
}

VALUE rb_obj_setup(VALUE obj, VALUE klass, VALUE type) {
  rb_tr_error("rb_obj_setup not implemented");
}

VALUE rb_float_new_in_heap(double d) {
  rb_tr_error("rb_float_new_in_heap not implemented");
}

int rb_typeddata_inherited_p(const rb_data_type_t *child, const rb_data_type_t *parent) {
  rb_tr_error("rb_typeddata_inherited_p not implemented");
}

int rb_typeddata_is_kind_of(VALUE obj, const rb_data_type_t *data_type) {
  rb_tr_error("rb_typeddata_is_kind_of not implemented");
}

void rb_freeze_singleton_class(VALUE x) {
  rb_tr_error("rb_freeze_singleton_class not implemented");
}

void rb_gc_writebarrier(VALUE a, VALUE b) {
  rb_tr_error("rb_gc_writebarrier not implemented");
}

void rb_gc_writebarrier_unprotect(VALUE obj) {
  rb_tr_error("rb_gc_writebarrier_unprotect not implemented");
}

void rb_gc_unprotect_logging(void *objptr, const char *filename, int line) {
  rb_tr_error("rb_gc_unprotect_logging not implemented");
}

void rb_obj_infect(VALUE obj1, VALUE obj2) {
  rb_tr_error("rb_obj_infect not implemented");
}

void rb_glob(const char *path, void (*func)(const char *, VALUE, void *), VALUE arg) {
  rb_tr_error("rb_glob not implemented");
}

void rb_prepend_module(VALUE klass, VALUE module) {
  rb_tr_error("rb_prepend_module not implemented");
}

VALUE rb_gvar_undef_getter(ID id, void *data, struct rb_global_variable *var) {
  rb_tr_error("rb_gvar_undef_getter not implemented");
}

VALUE rb_gvar_val_getter(ID id, void *data, struct rb_global_variable *var) {
  rb_tr_error("rb_gvar_val_getter not implemented");
}

void rb_define_virtual_variable(const char *name, VALUE (*getter)(ANYARGS), void (*setter)(ANYARGS)) {
  rb_tr_error("rb_define_virtual_variable not implemented");
}

void rb_gc_register_mark_object(VALUE obj) {
  rb_tr_error("rb_gc_register_mark_object not implemented");
}

ID rb_check_id(volatile VALUE *namep) {
  rb_tr_error("rb_check_id not implemented");
}

VALUE rb_to_symbol(VALUE name) {
  rb_tr_error("rb_to_symbol not implemented");
}

VALUE rb_check_symbol(volatile VALUE *namep) {
  rb_tr_error("rb_check_symbol not implemented");
}

VALUE rb_eval_string_protect(const char *str, int *state) {
  return rb_protect((VALUE (*)(VALUE))rb_eval_string, (VALUE)str, state);
}

VALUE rb_eval_string_wrap(const char *str, int *state) {
  rb_tr_error("rb_eval_string_wrap not implemented");
}

VALUE rb_funcall_passing_block(VALUE recv, ID mid, int argc, const VALUE *argv) {
  rb_tr_error("rb_funcall_passing_block not implemented");
}

VALUE rb_current_receiver(void) {
  rb_tr_error("rb_current_receiver not implemented");
}

static VALUE rb_keyword_error_new(const char *error, VALUE keys) {
  long i = 0, len = RARRAY_LEN(keys);
  VALUE error_message = rb_sprintf("%s keyword%.*s", error, len > 1, "s");

  if (len > 0) {
    rb_str_append(error_message, rb_str_new_cstr(": "));
    while (1) {
      const VALUE k = RARRAY_AREF(keys, i);
      Check_Type(k, T_SYMBOL); /* wrong hash is given to rb_get_kwargs */
      rb_str_append(error_message, rb_sym2str(k));
      if (++i >= len) break;
      rb_str_append(error_message, rb_str_new_cstr(", "));
    }
  }

  return rb_exc_new_str(rb_eArgError, error_message);
}

NORETURN(static void rb_keyword_error(const char *error, VALUE keys)) {
  rb_exc_raise(rb_keyword_error_new(error, keys));
}

NORETURN(static void unknown_keyword_error(VALUE hash, const ID *table, int keywords)) {
  int i;
  for (i = 0; i < keywords; i++) {
    VALUE key = table[i];
    rb_hash_delete(hash, key);
  }
  rb_keyword_error("unknown", rb_hash_keys(hash));
}

 static VALUE rb_tr_extract_keyword(VALUE keyword_hash, ID key, VALUE *values) {
   VALUE val = rb_hash_lookup2(keyword_hash, key, Qundef);
   if (values) {
     rb_hash_delete(keyword_hash, key);
   }
   return val;
}

int rb_get_kwargs(VALUE keyword_hash, const ID *table, int required, int optional, VALUE *values) {
  int rest = 0;
  int extracted = 0;
  VALUE missing = Qnil;

  if (optional < 0) {
	rest = 1;
	optional = -1-optional;
  }

  for (int n = 0; n < required; n++) {
    VALUE val = rb_tr_extract_keyword(keyword_hash, table[n], values);
    if (values) {
      values[n] = val;
    }
    if (val == Qundef) {
      if (NIL_P(missing)) {
        missing = rb_ary_new();
      }
      rb_ary_push(missing, table[n]);
      rb_keyword_error("missing", missing);
    }
    extracted++;
  }

  if (optional && !NIL_P(keyword_hash)) {
    for (int m = required; m < required + optional; m++) {
      VALUE val = rb_tr_extract_keyword(keyword_hash, table[m], values);
      if (values) {
        values[m] = val;
      }
      if (val != Qundef) {
        extracted++;
      }
    }
  }

  if (!rest && !NIL_P(keyword_hash)) {
	if (RHASH_SIZE(keyword_hash) > (unsigned int)(values ? 0 : extracted)) {
	    unknown_keyword_error(keyword_hash, table, required + optional);
	}
  }

  for (int i = extracted; i < required + optional; i++) {
    values[i] = Qundef;
  }

  return extracted;
}

VALUE rb_extract_keywords(VALUE *orighash) {
  rb_tr_error("rb_extract_keywords not implemented");
}

VALUE rb_syserr_new(int n, const char *mesg) {
  rb_tr_error("rb_syserr_new not implemented");
}

VALUE rb_syserr_new_str(int n, VALUE arg) {
  rb_tr_error("rb_syserr_new_str not implemented");
}

VALUE rb_yield_values2(int argc, const VALUE *argv) {
  rb_tr_error("rb_yield_values2 not implemented");
}

#ifndef HAVE_GNU_QSORT_R
typedef int (cmpfunc_t)(const void*, const void*, void*);

void ruby_qsort(void* base, const size_t nel, const size_t size, cmpfunc_t *cmp, void *d) {
  rb_tr_error("ruby_qsort not implemented");
}
#endif

#undef rb_intern

ID rb_intern(const char *string) {
  return (ID) RUBY_CEXT_INVOKE("rb_intern", rb_str_new_cstr(string));
}
