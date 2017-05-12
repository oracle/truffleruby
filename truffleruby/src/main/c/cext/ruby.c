/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * This file contains code that is based on the Ruby API headers and implementation,
 * copyright (C) Yukihiro Matsumoto, licensed under the 2-clause BSD licence
 * as described in the file BSDL included with JRuby+Truffle.
 */
 
// Needed for vasprintf
#define _GNU_SOURCE
 
#include <ruby.h>

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <errno.h>
#include <limits.h>
#include <fcntl.h>

// Private helper macros just for ruby.c

#define rb_boolean(c) ((c) ? Qtrue : Qfalse)

// Helpers

VALUE rb_f_notimplement(int args_count, const VALUE *args, VALUE object) {
  rb_tr_error("rb_f_notimplement");
}

// Memory

void *rb_alloc_tmp_buffer(VALUE *buffer_pointer, long length) {
  // TODO CS 13-Apr-17 MRI sometimes uses alloc and sometimes malloc, and wraps it in a Ruby object - is rb_free_tmp_buffer guaranteed to be called or do we need to free in a finalizer?
  void *space = malloc(length);
  *((void**) buffer_pointer) = space;
  return space;
}

void rb_free_tmp_buffer(VALUE *buffer_pointer) {
  free(*((void**) buffer_pointer));
}

// Types

int rb_type(VALUE value) {
  return truffle_invoke_i(RUBY_CEXT, "rb_type", value);
}

bool RB_TYPE_P(VALUE value, int type) {
  return truffle_invoke_b(RUBY_CEXT, "RB_TYPE_P", value, type);
}

void rb_check_type(VALUE value, int type) {
  truffle_invoke(RUBY_CEXT, "rb_check_type", value, type);
}

VALUE rb_obj_is_instance_of(VALUE object, VALUE ruby_class) {
  return truffle_invoke(RUBY_CEXT, "rb_obj_is_instance_of", object, ruby_class);
}

VALUE rb_obj_is_kind_of(VALUE object, VALUE ruby_class) {
  return truffle_invoke(RUBY_CEXT, "rb_obj_is_kind_of", object, ruby_class);
}

void rb_check_frozen(VALUE object) {
  truffle_invoke(RUBY_CEXT, "rb_check_frozen", object);
}

void rb_check_safe_obj(VALUE object) {
  rb_tr_error("rb_check_safe_obj not implemented");
}

bool SYMBOL_P(VALUE value) {
  return truffle_invoke_b(RUBY_CEXT, "SYMBOL_P", value);
}

// Constants

// START from tool/generate-cext-constants.rb

VALUE rb_tr_get_undef(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "Qundef");
}

VALUE rb_tr_get_true(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "Qtrue");
}

VALUE rb_tr_get_false(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "Qfalse");
}

VALUE rb_tr_get_nil(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "Qnil");
}

VALUE rb_tr_get_Array(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cArray");
}

VALUE rb_tr_get_Bignum(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cBignum");
}

VALUE rb_tr_get_Class(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cClass");
}

VALUE rb_tr_get_Comparable(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_mComparable");
}

VALUE rb_tr_get_Data(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cData");
}

VALUE rb_tr_get_Encoding(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cEncoding");
}

VALUE rb_tr_get_Enumerable(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_mEnumerable");
}

VALUE rb_tr_get_FalseClass(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cFalseClass");
}

VALUE rb_tr_get_File(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cFile");
}

VALUE rb_tr_get_Fixnum(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cFixnum");
}

VALUE rb_tr_get_Float(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cFloat");
}

VALUE rb_tr_get_Hash(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cHash");
}

VALUE rb_tr_get_Integer(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cInteger");
}

VALUE rb_tr_get_IO(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cIO");
}

VALUE rb_tr_get_Kernel(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_mKernel");
}

VALUE rb_tr_get_Match(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cMatch");
}

VALUE rb_tr_get_Module(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cModule");
}

VALUE rb_tr_get_NilClass(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cNilClass");
}

VALUE rb_tr_get_Numeric(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cNumeric");
}

VALUE rb_tr_get_Object(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cObject");
}

VALUE rb_tr_get_Range(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cRange");
}

VALUE rb_tr_get_Regexp(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cRegexp");
}

VALUE rb_tr_get_String(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cString");
}

VALUE rb_tr_get_Struct(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cStruct");
}

VALUE rb_tr_get_Symbol(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cSymbol");
}

VALUE rb_tr_get_Time(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cTime");
}

VALUE rb_tr_get_Thread(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cThread");
}

VALUE rb_tr_get_TrueClass(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cTrueClass");
}

VALUE rb_tr_get_Proc(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cProc");
}

VALUE rb_tr_get_Method(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cMethod");
}

VALUE rb_tr_get_Dir(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_cDir");
}

VALUE rb_tr_get_ArgError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eArgError");
}

VALUE rb_tr_get_EOFError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eEOFError");
}

VALUE rb_tr_get_Errno(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_mErrno");
}

VALUE rb_tr_get_Exception(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eException");
}

VALUE rb_tr_get_FloatDomainError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eFloatDomainError");
}

VALUE rb_tr_get_IndexError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eIndexError");
}

VALUE rb_tr_get_Interrupt(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eInterrupt");
}

VALUE rb_tr_get_IOError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eIOError");
}

VALUE rb_tr_get_LoadError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eLoadError");
}

VALUE rb_tr_get_LocalJumpError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eLocalJumpError");
}

VALUE rb_tr_get_MathDomainError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eMathDomainError");
}

VALUE rb_tr_get_EncCompatError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eEncCompatError");
}

VALUE rb_tr_get_NameError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eNameError");
}

VALUE rb_tr_get_NoMemError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eNoMemError");
}

VALUE rb_tr_get_NoMethodError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eNoMethodError");
}

VALUE rb_tr_get_NotImpError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eNotImpError");
}

VALUE rb_tr_get_RangeError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eRangeError");
}

VALUE rb_tr_get_RegexpError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eRegexpError");
}

VALUE rb_tr_get_RuntimeError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eRuntimeError");
}

VALUE rb_tr_get_ScriptError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eScriptError");
}

VALUE rb_tr_get_SecurityError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eSecurityError");
}

VALUE rb_tr_get_Signal(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eSignal");
}

VALUE rb_tr_get_StandardError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eStandardError");
}

VALUE rb_tr_get_SyntaxError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eSyntaxError");
}

VALUE rb_tr_get_SystemCallError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eSystemCallError");
}

VALUE rb_tr_get_SystemExit(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eSystemExit");
}

VALUE rb_tr_get_SysStackError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eSysStackError");
}

VALUE rb_tr_get_TypeError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eTypeError");
}

VALUE rb_tr_get_ThreadError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eThreadError");
}

VALUE rb_tr_get_WaitReadable(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_mWaitReadable");
}

VALUE rb_tr_get_WaitWritable(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_mWaitWritable");
}

VALUE rb_tr_get_ZeroDivError(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_eZeroDivError");
}

VALUE rb_tr_get_stdin(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_stdin");
}

VALUE rb_tr_get_stdout(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_stdout");
}

VALUE rb_tr_get_stderr(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_stderr");
}

VALUE rb_tr_get_output_fs(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_output_fs");
}

VALUE rb_tr_get_rs(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_rs");
}

VALUE rb_tr_get_output_rs(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_output_rs");
}

VALUE rb_tr_get_default_rs(void) {
  return (VALUE) truffle_read(RUBY_CEXT, "rb_default_rs");
}

// END from tool/generate-cext-constants.rb

// Conversions

VALUE CHR2FIX(char ch) {
  return INT2FIX((unsigned char) ch);
}

int NUM2INT(VALUE value) {
  return truffle_invoke_i(RUBY_CEXT, "NUM2INT", value);
}

unsigned int NUM2UINT(VALUE value) {
  return (unsigned int) truffle_invoke_l(RUBY_CEXT, "NUM2LONG", value);
}

long NUM2LONG(VALUE value) {
  return truffle_invoke_l(RUBY_CEXT, "NUM2LONG", value);
}

unsigned long rb_num2ulong(VALUE val) {
  return (unsigned long)truffle_invoke_l(RUBY_CEXT, "rb_num2ulong", val);
}

unsigned long NUM2ULONG(VALUE value) {
  // TODO CS 24-Jul-16 _invoke_l but what about the unsigned part?
  return truffle_invoke_l(RUBY_CEXT, "NUM2ULONG", value);
}

double NUM2DBL(VALUE value) {
  return truffle_invoke_d(RUBY_CEXT, "NUM2DBL", value);
}

int FIX2INT(VALUE value) {
  return truffle_invoke_i(RUBY_CEXT, "FIX2INT", value);
}

unsigned int FIX2UINT(VALUE value) {
  return (unsigned int) truffle_invoke_i(RUBY_CEXT, "FIX2UINT", value);
}

long FIX2LONG(VALUE value) {
  return truffle_invoke_l(RUBY_CEXT, "FIX2LONG", value);
}

VALUE INT2NUM(long value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "INT2NUM", value);
}

VALUE INT2FIX(long value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "INT2FIX", value);
}

VALUE UINT2NUM(unsigned int value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "UINT2NUM", value);
}

VALUE LONG2NUM(long value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "LONG2NUM", value);
}

VALUE ULONG2NUM(unsigned long value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "ULONG2NUM", value);
}

VALUE LONG2FIX(long value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "LONG2FIX", value);
}

int rb_fix2int(VALUE value) {
  return truffle_invoke_i(RUBY_CEXT, "rb_fix2int", value);
}

unsigned long rb_fix2uint(VALUE value) {
  return truffle_invoke_l(RUBY_CEXT, "rb_fix2uint", value);
}

int rb_long2int(long value) {
  return truffle_invoke_l(RUBY_CEXT, "rb_long2int", value);
}

ID SYM2ID(VALUE value) {
  return (ID) value;
}

VALUE ID2SYM(ID value) {
  return (VALUE) value;
}

char RB_NUM2CHR(VALUE x) {
  if (RB_TYPE_P(x, RUBY_T_STRING) && RSTRING_LEN(x)>=1) {
    return RSTRING_PTR(x)[0];
  } else {
    int a = truffle_invoke_i(RUBY_CEXT, "rb_num2int", x);
    return (char)(a & 0xff);
  }
}

int rb_cmpint(VALUE val, VALUE a, VALUE b) {
  return truffle_invoke_i(RUBY_CEXT, "rb_cmpint", val, a, b);
}

VALUE rb_int2inum(SIGNED_VALUE n) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "LONG2NUM", n);
}

VALUE rb_ll2inum(LONG_LONG n) {
  /* Long and long long are both 64-bits with clang x86-64. */
  return (VALUE) truffle_invoke(RUBY_CEXT, "LONG2NUM", n);
}

double rb_num2dbl(VALUE val) {
  return truffle_invoke_d(RUBY_CEXT, "rb_num2dbl", val);
}

long rb_num2int(VALUE val) {
  return truffle_invoke_i(RUBY_CEXT, "rb_num2int", val);
}

unsigned long rb_num2uint(VALUE val) {
  return (unsigned long)truffle_invoke_l(RUBY_CEXT, "rb_num2uint", val);
}

long rb_num2long(VALUE val) {
  return truffle_invoke_l(RUBY_CEXT, "rb_num2long", val);
}

VALUE rb_num_coerce_bin(VALUE x, VALUE y, ID func) {
  return truffle_invoke(RUBY_CEXT, "rb_num_coerce_bin", x, y, ID2SYM(func));
}

VALUE rb_num_coerce_cmp(VALUE x, VALUE y, ID func) {
  return truffle_invoke(RUBY_CEXT, "rb_num_coerce_cmp", x, y, ID2SYM(func));
}

VALUE rb_num_coerce_relop(VALUE x, VALUE y, ID func) {
  return truffle_invoke(RUBY_CEXT, "rb_num_coerce_relop", x, y, ID2SYM(func));
}

void rb_num_zerodiv(void) {
  rb_raise(rb_eZeroDivError, "divided by 0");
}

VALUE LL2NUM(LONG_LONG n) {
  return truffle_invoke(RUBY_CEXT, "LL2NUM", n);
}

// Type checks

int RB_NIL_P(VALUE value) {
  return truffle_invoke_b(RUBY_CEXT, "RB_NIL_P", value);
}

int RB_FIXNUM_P(VALUE value) {
  return truffle_invoke_b(RUBY_CEXT, "RB_FIXNUM_P", value);
}

int RTEST(VALUE value) {
  return truffle_invoke_b(RUBY_CEXT, "RTEST", value);
}

// Kernel

VALUE rb_require(const char *feature) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_require", rb_str_new_cstr(feature));
}

VALUE rb_eval_string(const char *str) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_eval_string", rb_str_new_cstr(str));
}

VALUE rb_exec_recursive(VALUE (*func) (VALUE, VALUE, int), VALUE obj, VALUE arg) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_exec_recursive", func, obj, arg);
}

VALUE rb_f_sprintf(int argc, const VALUE *argv) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_f_sprintf", rb_ary_new4(argc, argv));
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
  truffle_invoke(RUBY_CEXT, "rb_iter_break");
}

const char *rb_sourcefile(void) {
  return RSTRING_PTR(truffle_invoke(RUBY_CEXT, "rb_sourcefile"));
}

int rb_sourceline(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_sourceline");
}

int rb_method_boundp(VALUE klass, ID id, int ex) {
  return truffle_invoke_i(RUBY_CEXT, "rb_method_boundp", klass, id, ex);
}

// Object

VALUE rb_obj_dup(VALUE object) {
  return (VALUE) truffle_invoke(object, "dup");
}

VALUE rb_any_to_s(VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_any_to_s", object);
}

VALUE rb_obj_instance_variables(VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_obj_instance_variables", object);
}

VALUE rb_check_convert_type(VALUE val, int type, const char *type_name, const char *method) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_check_convert_type", val, rb_str_new_cstr(type_name), rb_str_new_cstr(method));
}

VALUE rb_check_to_integer(VALUE object, const char *method) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_check_to_integer", object, rb_str_new_cstr(method));
}

VALUE rb_check_string_type(VALUE object) {
  return rb_check_convert_type(object, T_STRING, "String", "to_str");
}

VALUE rb_convert_type(VALUE object, int type, const char *type_name, const char *method) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_convert_type", object, rb_str_new_cstr(type_name), rb_str_new_cstr(method));
}

void rb_extend_object(VALUE object, VALUE module) {
  truffle_invoke(module, "extend_object", object);
}

VALUE rb_inspect(VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_inspect", object);
}

void rb_obj_call_init(VALUE object, int argc, const VALUE *argv) {
  truffle_invoke(RUBY_CEXT, "rb_obj_call_init", object, rb_ary_new4(argc, argv));
}

const char *rb_obj_classname(VALUE object) {
  return RSTRING_PTR((VALUE) truffle_invoke(RUBY_CEXT, "rb_obj_classname", object));
}

VALUE rb_obj_id(VALUE object) {
  return (VALUE) truffle_invoke(object, "object_id");
}

void rb_tr_hidden_variable_set(VALUE object, const char *name, VALUE value) {
  truffle_invoke(RUBY_CEXT, "hidden_variable_set", object, rb_intern(name), value);
}

VALUE rb_tr_hidden_variable_get(VALUE object, const char *name) {
  return truffle_invoke(RUBY_CEXT, "hidden_variable_get", object, rb_intern(name));
}

int rb_obj_method_arity(VALUE object, ID id) {
  return truffle_invoke_i(RUBY_CEXT, "rb_obj_method_arity", object, id);
}

int rb_obj_respond_to(VALUE object, ID id, int priv) {
  return truffle_invoke_i(RUBY_CEXT, "rb_obj_respond_to", object, id, priv);
}

VALUE rb_special_const_p(VALUE object) {
  return truffle_invoke(RUBY_CEXT, "rb_special_const_p", object);
}

VALUE rb_to_int(VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_to_int", object);
}

VALUE rb_obj_instance_eval(int argc, const VALUE *argv, VALUE self) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_obj_instance_eval", self, rb_ary_new4(argc, argv), rb_block_proc());
}

VALUE rb_ivar_defined(VALUE object, ID id) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_ivar_defined", object, id);
}

VALUE rb_equal_opt(VALUE a, VALUE b) {
  rb_tr_error("rb_equal_opt not implemented");
}

VALUE rb_class_inherited_p(VALUE module, VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_class_inherited_p", module, object);
}

VALUE rb_equal(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke(a, "===", b);
}

VALUE rb_obj_taint(VALUE object) {
  return (VALUE) truffle_invoke(object, "taint");
}

bool rb_tr_obj_taintable_p(VALUE object) {
  return truffle_invoke_b(RUBY_CEXT, "RB_OBJ_TAINTABLE", object);
}

bool rb_tr_obj_tainted_p(VALUE object) {
  return truffle_invoke_b((void *)object, "tainted?");
}

void rb_tr_obj_infect(VALUE a, VALUE b) {
  truffle_invoke(RUBY_CEXT, "rb_tr_obj_infect", a, b);
}

VALUE rb_obj_freeze(VALUE object) {
  return (VALUE) truffle_invoke(object, "freeze");
}

VALUE rb_obj_frozen_p(VALUE object) {
  return truffle_invoke(object, "frozen?");
}

// Integer

VALUE rb_Integer(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Integer", value);
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

static void
validate_integer_pack_format(size_t numwords, size_t wordsize, size_t nails, int flags, int supported_flags)
{
    int wordorder_bits = flags & INTEGER_PACK_WORDORDER_MASK;
    int byteorder_bits = flags & INTEGER_PACK_BYTEORDER_MASK;

    if (flags & ~supported_flags) {
        rb_raise(rb_eArgError, "unsupported flags specified");
    }

    if (wordorder_bits == 0) {
        if (1 < numwords)
            rb_raise(rb_eArgError, "word order not specified");
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
  long j;
  VALUE msw_first, twosComp, swap, bytes;
  int sign, size, bytes_needed, words_needed, result;
  uint8_t *buf;
  msw_first = rb_boolean(check_msw_first(flags));
  twosComp = rb_boolean(((flags & INTEGER_PACK_2COMP) != 0));
  swap = rb_boolean(endian_swap(flags));
  // Test for fixnum and do the right things here.
  bytes = truffle_invoke(RUBY_CEXT, "rb_integer_bytes", value,
                         (int)numwords, (int)wordsize, msw_first, twosComp, swap);
  size = (twosComp == Qtrue) ? truffle_invoke_i(RUBY_CEXT, "rb_2scomp_bit_length", value)
    : truffle_invoke_i(RUBY_CEXT, "rb_absint_bit_length", value);
  if (RB_FIXNUM_P(value)) {
    long l = NUM2LONG(value);
    sign = (l > 0) - (l < 0);
  } else {
    sign = truffle_invoke_i(value, "<=>", 0);
  }
  bytes_needed = size / 8 + (size % 8 == 0 ? 0 : 1);
  words_needed = bytes_needed / wordsize + (bytes_needed % wordsize == 0 ? 0 : 1);
  result = (words_needed <= numwords ? 1 : 2) * sign;

  buf = (uint8_t *)words;
  for (i = 0; i < numwords * wordsize; i++) {
    buf[i] = (uint8_t)truffle_read_idx_i(bytes, i);
  }
  return result;
}

VALUE rb_integer_unpack(const void *words, size_t numwords, size_t wordsize, size_t nails, int flags) {
  rb_tr_error("rb_integer_unpack not implemented");
}

size_t rb_absint_size(VALUE value, int *nlz_bits_ret) {
  int size = truffle_invoke_i(RUBY_CEXT, "rb_absint_bit_length", value);
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
  return truffle_invoke(RUBY_CEXT, "rb_cstr_to_inum", rb_str_new_cstr(string), base, raise);
}

double rb_big2dbl(VALUE x) {
  return truffle_invoke_d(RUBY_CEXT, "rb_num2dbl", x);
}

VALUE rb_dbl2big(double d) {
  return truffle_invoke(RUBY_CEXT, "DBL2BIG", d);
}

LONG_LONG rb_big2ll(VALUE x) {
  return truffle_invoke_l(RUBY_CEXT, "rb_num2long", x);
}

long rb_big2long(VALUE x) {
  return truffle_invoke_l(RUBY_CEXT, "rb_num2long", x);
}

VALUE rb_big2str(VALUE x, int base) {
  return (VALUE) truffle_invoke((void *)x, "to_s", base);
}

unsigned long rb_big2ulong(VALUE x) {
  return truffle_invoke_l(RUBY_CEXT, "rb_num2ulong", x);
}

VALUE rb_big_cmp(VALUE x, VALUE y) {
  return (VALUE) truffle_invoke(x, "<=>", y);
}

void rb_big_pack(VALUE val, unsigned long *buf, long num_longs) {
  rb_integer_pack(val, buf, num_longs, 8, 0,
                  INTEGER_PACK_2COMP | INTEGER_PACK_NATIVE_BYTE_ORDER | INTEGER_PACK_LSWORD_FIRST);
}

// Float

VALUE rb_float_new(double value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_float_new", value);
}

VALUE rb_Float(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Float", value);
}

double RFLOAT_VALUE(VALUE value){
  return truffle_invoke_d(RUBY_CEXT, "RFLOAT_VALUE", value);
}

// String

char *RSTRING_PTR(VALUE string) {
  return (char *)truffle_invoke(RUBY_CEXT, "RSTRING_PTR", string);
}

int rb_str_len(VALUE string) {
  return truffle_invoke_i((void *)string, "bytesize");
}

VALUE rb_str_new(const char *string, long length) {
  if (length < 0) {
    rb_raise(rb_eArgError, "negative string size (or size too big)");
  }

  if (string == NULL) {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new_nul", length);
  } else if (truffle_is_truffle_object((VALUE) string)) {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new", string, length);
  } else {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new_cstr", truffle_read_n_string(string, length));
  }
}

VALUE rb_str_new_cstr(const char *string) {
  if (truffle_is_truffle_object((VALUE) string)) {
    VALUE ruby_string = (VALUE) truffle_invoke((VALUE) string, "to_s");
    int len = strlen(string);
    return (VALUE) truffle_invoke(ruby_string, "[]", 0, len);
  } else {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new_cstr", truffle_read_string(string));
  }
}

VALUE rb_str_new_shared(VALUE string) {
  return truffle_invoke((void *)string, "dup");
}

VALUE rb_str_new_with_class(VALUE klass, const char *string, long len) {
  return truffle_invoke(truffle_invoke(klass, "class"), "new", rb_str_new(string, len));
}

VALUE rb_intern_str(VALUE string) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_intern_str", string);
}

VALUE rb_str_cat(VALUE string, const char *to_concat, long length) {
  truffle_invoke(string, "concat", rb_str_new(to_concat, length));
  return string;
}

VALUE rb_str_cat2(VALUE string, const char *to_concat) {
  truffle_invoke(string, "concat", rb_str_new_cstr(to_concat));
  return string;
}

VALUE rb_str_to_str(VALUE string) {
  return rb_convert_type(string, T_STRING, "String", "to_str");
}

VALUE rb_str_buf_new(long capacity) {
  return rb_str_new_cstr("");
}

VALUE rb_vsprintf(const char *format, va_list args) {
  // TODO CS 7-May-17 this needs to use the Ruby sprintf, not C's
  char *buffer;
  if (vasprintf(&buffer, format, args) < 0) {
    rb_tr_error("vasprintf error");
  }
  VALUE string = rb_str_new_cstr(buffer);
  free(buffer);
  return string;
}

VALUE rb_str_append(VALUE string, VALUE to_append) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_append", string, to_append);
}

void rb_str_set_len(VALUE string, long length) {
  rb_str_resize(string, length);
}

VALUE rb_str_new_frozen(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new_frozen", value);
}

VALUE rb_String(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_String", value);
}

VALUE rb_str_resize(VALUE string, long length) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_resize", string, length);
}

VALUE rb_str_split(VALUE string, const char *split) {
  return (VALUE) truffle_invoke(string, "split", rb_str_new_cstr(split));
}

void rb_str_modify(VALUE string) {
  // Does nothing because writing to the string pointer will cause necessary invalidations anyway
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
  return truffle_invoke_i((void *)a, "<=>", b);
}

VALUE rb_str_buf_cat(VALUE string, const char *to_concat, long length) {
  return truffle_invoke(string, "<<", rb_str_new(to_concat, length));
}

rb_encoding *rb_to_encoding(VALUE encoding) {
  return truffle_invoke(RUBY_CEXT, "rb_to_encoding", encoding);
}

VALUE rb_str_conv_enc(VALUE string, rb_encoding *from, rb_encoding *to) {
  return rb_str_conv_enc_opts(string, from, to, 0, Qnil);
}

VALUE rb_str_conv_enc_opts(VALUE str, rb_encoding *from, rb_encoding *to, int ecflags, VALUE ecopts) {
  if (!to) return str;
  if (!from) from = rb_enc_get(str);
  if (from == to) return str;
  return truffle_invoke(RUBY_CEXT, "rb_str_conv_enc_opts", str, rb_enc_from_encoding(from), rb_enc_from_encoding(to), ecflags, ecopts);
}

VALUE
rb_tainted_str_new_with_enc(const char *ptr, long len, rb_encoding *enc)
{
  VALUE str = rb_enc_str_new(ptr, len, enc);
  OBJ_TAINT(str);
  return str;
}

VALUE rb_external_str_new_with_enc(const char *ptr, long len, rb_encoding *eenc) {
  VALUE str;
  str = rb_tainted_str_new_with_enc(ptr, len, eenc);
  return rb_external_str_with_enc(str, eenc);
}

VALUE rb_external_str_with_enc(VALUE str, rb_encoding *eenc) {
  if (truffle_invoke_b(rb_enc_from_encoding(eenc), "==", rb_enc_from_encoding(rb_usascii_encoding())) &&
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
  VALUE result = truffle_invoke(RUBY_CEXT, "rb_default_external_encoding");
  if (result == Qnil) {
    return NULL;
  }
  return rb_to_encoding(result);
}

rb_encoding *rb_default_internal_encoding(void) {
  VALUE result = truffle_invoke(RUBY_CEXT, "rb_default_internal_encoding");
  if (result == Qnil) {
    return NULL;
  }
  return rb_to_encoding(result);
}

rb_encoding *rb_locale_encoding(void) {
  VALUE result = truffle_invoke(RUBY_CEXT, "rb_locale_encoding");
  if (result == Qnil) {
    return NULL;
  }
  return rb_to_encoding(result);
}

int rb_locale_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_locale_encindex");
}

rb_encoding *rb_filesystem_encoding(void) {
  VALUE result = truffle_invoke(RUBY_CEXT, "rb_filesystem_encoding");
  if (result == Qnil) {
    return NULL;
  }
  return rb_to_encoding(result);
}

int rb_filesystem_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_filesystem_encindex");
}

rb_encoding *get_encoding(VALUE string) {
  return rb_to_encoding(truffle_invoke(string, "encoding"));
}

VALUE rb_str_intern(VALUE string) {
  return (VALUE) truffle_invoke(string, "intern");
}

VALUE rb_str_length(VALUE string) {
  return (VALUE) truffle_invoke(string, "length");
}

VALUE rb_str_plus(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke(a, "+", b);
}

VALUE rb_str_subseq(VALUE string, long beg, long len) {
  rb_tr_error("rb_str_subseq not implemented");
}

VALUE rb_str_substr(VALUE string, long beg, long len) {
  return (VALUE) truffle_invoke(string, "[]", beg, len);
}

st_index_t rb_str_hash(VALUE string) {
  return (st_index_t) truffle_invoke_l((void *)string, "hash");
}

void rb_str_update(VALUE string, long beg, long len, VALUE value) {
  truffle_invoke(string, "[]=", beg, len, value);
}

VALUE rb_str_equal(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke(a, "==", b);
}

void rb_str_free(VALUE string) {
//  intentional noop here
}

unsigned int rb_enc_codepoint_len(const char *p, const char *e, int *len_p, rb_encoding *encoding) {
  int len = e - p;
  if (len <= 0) {
    rb_raise(rb_eArgError, "empty string");
  }
  VALUE array = truffle_invoke(RUBY_CEXT, "rb_enc_codepoint_len", rb_str_new(p, len), rb_enc_from_encoding(encoding));
  if (len_p) *len_p = truffle_invoke_i(array, "[]", 0);
  return (unsigned int)truffle_invoke_i(array, "[]", 1);
}

rb_encoding *rb_enc_get(VALUE object) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "rb_enc_get", object));
}

void rb_enc_set_index(VALUE obj, int idx) {
  truffle_invoke(RUBY_CEXT, "rb_enc_set_index", obj, idx);
}

rb_encoding *rb_ascii8bit_encoding(void) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "ascii8bit_encoding"));
}

int rb_ascii8bit_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_ascii8bit_encindex");
}

rb_encoding *rb_usascii_encoding(void) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "usascii_encoding"));
}

int rb_enc_asciicompat(rb_encoding *enc){
  return truffle_invoke_b(rb_enc_from_encoding(enc), "ascii_compatible?");
}

void rb_must_asciicompat(VALUE str) {
  rb_encoding *enc = rb_enc_get(str);
  if (!rb_enc_asciicompat(enc)) {
    rb_raise(rb_eEncCompatError, "ASCII incompatible encoding: %s", rb_enc_name(enc));
  }
}

int rb_usascii_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_usascii_encindex");
}

rb_encoding *rb_utf8_encoding(void) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "utf8_encoding"));
}

int rb_utf8_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_utf8_encindex");
}

enum ruby_coderange_type RB_ENC_CODERANGE(VALUE obj) {
  return truffle_invoke_i(RUBY_CEXT, "RB_ENC_CODERANGE", obj);
}

int rb_encdb_alias(const char *alias, const char *orig) {
  rb_tr_error("rb_encdb_alias not implemented");
}

VALUE rb_enc_associate(VALUE obj, rb_encoding *enc) {
  return rb_enc_associate_index(obj, rb_enc_to_index(enc));
}

VALUE rb_enc_associate_index(VALUE obj, int idx) {
  return truffle_invoke(RUBY_CEXT, "rb_enc_associate_index", obj, idx);
}

rb_encoding* rb_enc_compatible(VALUE str1, VALUE str2) {
  VALUE result = truffle_invoke(rb_cEncoding, "compatible?", str1, str2);
  if (result != Qnil) {
    return rb_to_encoding(result);
  }
  return NULL;
}

void rb_enc_copy(VALUE obj1, VALUE obj2) {
  rb_enc_associate_index(obj1, rb_enc_get_index(obj2));
}

int rb_enc_find_index(const char *name) {
  return truffle_invoke_i(RUBY_CEXT, "rb_enc_find_index", rb_str_new_cstr(name));
}

rb_encoding *rb_enc_find(const char *name) {
  int idx = rb_enc_find_index(name);
  if (idx < 0) idx = 0;
  return rb_enc_from_index(idx);
}

VALUE rb_enc_from_encoding(rb_encoding *encoding) {
  return truffle_invoke(RUBY_CEXT, "rb_enc_from_encoding", encoding);
}

rb_encoding *rb_enc_from_index(int index) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "rb_enc_from_index", index));
}

int rb_enc_str_coderange(VALUE str) {
  return truffle_invoke_i(RUBY_CEXT, "rb_enc_str_coderange", str);
}

VALUE rb_enc_str_new(const char *ptr, long len, rb_encoding *enc) {
  return truffle_invoke(rb_str_new(ptr, len), "force_encoding", rb_enc_from_encoding(enc));
}

int rb_enc_to_index(rb_encoding *enc) {
  return truffle_invoke_i(RUBY_CEXT, "rb_enc_to_index", rb_enc_from_encoding(enc));
}

VALUE rb_obj_encoding(VALUE obj) {
  return truffle_invoke(obj, "encoding");
}

VALUE rb_str_encode(VALUE str, VALUE to, int ecflags, VALUE ecopts) {
  return truffle_invoke(RUBY_CEXT, "rb_str_encode", str, to, ecflags, ecopts);
}

VALUE rb_usascii_str_new(const char *ptr, long len) {
  return truffle_invoke(rb_str_new(ptr, len), "force_encoding", rb_enc_from_encoding(rb_usascii_encoding()));
}

VALUE rb_usascii_str_new_cstr(const char *ptr) {
  return truffle_invoke(rb_str_new_cstr(ptr), "force_encoding", rb_enc_from_encoding(rb_usascii_encoding()));
}

int rb_to_encoding_index(VALUE enc) {
  return truffle_invoke_i(RUBY_CEXT, "rb_to_encoding_index", enc);
}

char* rb_enc_nth(const char *p, const char *e, long nth, rb_encoding *enc) {
  rb_tr_error("rb_enc_nth not implemented");
}

int rb_enc_get_index(VALUE obj) {
  return truffle_invoke_i(RUBY_CEXT, "rb_enc_get_index", obj);
}

VALUE rb_str_times(VALUE string, VALUE times) {
  rb_tr_error("rb_str_times not implemented");
}

// Symbol

ID rb_to_id(VALUE name) {
  return SYM2ID((VALUE) truffle_invoke(name, "to_sym"));
}

ID rb_intern(const char *string) {
  return (ID) truffle_invoke(RUBY_CEXT, "rb_intern", rb_str_new_cstr(string));
}

ID rb_intern2(const char *string, long length) {
  return (ID) SYM2ID(truffle_invoke(RUBY_CEXT, "rb_intern", rb_str_new(string, length)));
}

ID rb_intern3(const char *name, long len, rb_encoding *enc) {
  return (ID) SYM2ID(truffle_invoke(RUBY_CEXT, "rb_intern3", rb_str_new(name, len), rb_enc_from_encoding(enc)));
}

VALUE rb_sym2str(VALUE string) {
  return (VALUE) truffle_invoke(string, "to_s");
}

const char *rb_id2name(ID id) {
    return RSTRING_PTR(rb_id2str(id));
}

VALUE rb_id2str(ID id) {
  return truffle_invoke(RUBY_CEXT, "rb_id2str", ID2SYM(id));
}

int rb_is_class_id(ID id) {
  return truffle_invoke_b(RUBY_CEXT, "rb_is_class_id", ID2SYM(id));
}

int rb_is_const_id(ID id) {
  return truffle_invoke_b(RUBY_CEXT, "rb_is_const_id", ID2SYM(id));
}

int rb_is_instance_id(ID id) {
  return truffle_invoke_b(RUBY_CEXT, "rb_is_instance_id", ID2SYM(id));
}

// Array

int RARRAY_LEN(VALUE array) {
  return truffle_get_size(array);
}

int RARRAY_LENINT(VALUE array) {
  return truffle_get_size(array);
}

VALUE RARRAY_AREF(VALUE array, long index) {
  return truffle_read_idx(array, (int) index);
}

VALUE rb_Array(VALUE array) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Array", array);
}

VALUE rb_ary_new() {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_ary_new");
}

VALUE rb_ary_new_capa(long capacity) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_ary_new_capa", capacity);
}

VALUE rb_ary_new_from_args(long n, ...) {
  VALUE array = rb_ary_new_capa(n);
  for (int i = 0; i < n; i++) {
    rb_ary_store(array, i, (VALUE) truffle_get_arg(1+i));
  }
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
  truffle_invoke(array, "push", value);
  return array;
}

VALUE rb_ary_pop(VALUE array) {
  return (VALUE) truffle_invoke(array, "pop");
}

void rb_ary_store(VALUE array, long index, VALUE value) {
  truffle_write_idx(array, (int) index, value);
}

VALUE rb_ary_entry(VALUE array, long index) {
  return truffle_read_idx(array, (int) index);
}

VALUE rb_ary_each(VALUE array) {
  rb_tr_error("rb_ary_each not implemented");
}

VALUE rb_ary_unshift(VALUE array, VALUE value) {
  return (VALUE) truffle_invoke(array, "unshift", value);
}

VALUE rb_ary_aref(int n, const VALUE* values, VALUE array) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "send_splatted", array, rb_str_new_cstr("[]"), rb_ary_new4(n, values));
}

VALUE rb_ary_clear(VALUE array) {
  return (VALUE) truffle_invoke(array, "clear");
}

VALUE rb_ary_delete(VALUE array, VALUE value) {
  return (VALUE) truffle_invoke(array, "delete", value);
}

VALUE rb_ary_delete_at(VALUE array, long n) {
  return (VALUE) truffle_invoke(array, "delete_at", n);
}

VALUE rb_ary_includes(VALUE array, VALUE value) {
  return (VALUE) truffle_invoke(array, "include?", value);
}

VALUE rb_ary_join(VALUE array, VALUE sep) {
  return (VALUE) truffle_invoke(array, "join", sep);
}

VALUE rb_ary_to_s(VALUE array) {
  return (VALUE) truffle_invoke(array, "to_s");
}

VALUE rb_ary_reverse(VALUE array) {
  return (VALUE) truffle_invoke(array, "reverse!");
}

VALUE rb_ary_shift(VALUE array) {
  return (VALUE) truffle_invoke(array, "shift");
}

VALUE rb_ary_concat(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke(a, "concat", b);
}

VALUE rb_ary_plus(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke(a, "+", b);
}

VALUE rb_iterate(VALUE (*function)(), VALUE arg1, VALUE (*block)(), VALUE arg2) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_iterate", function, arg1, block, arg2, rb_block_proc());
}

VALUE rb_each(VALUE array) {
  if (rb_block_given_p()) {
    return rb_funcall_with_block(array, rb_intern("each"), 0, NULL, rb_block_proc());
  } else {
    return (VALUE) truffle_invoke(array, "each");
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
  return rb_ary_new3(1, array);
}

VALUE rb_ary_subseq(VALUE array, long start, long length) {
  return (VALUE) truffle_invoke(array, "[]", start, length);
}

VALUE rb_check_array_type(VALUE array) {
  return rb_check_convert_type(array, T_ARRAY, "Array", "to_ary");
}

VALUE rb_ary_cat(VALUE array, const VALUE *cat, long n) {
  rb_tr_error("rb_ary_cat not implemented");
}

VALUE rb_ary_rotate(VALUE array, long n) {
  rb_tr_error("rb_ary_rotate not implemented");
}

// Hash

VALUE rb_Hash(VALUE obj) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Hash", obj);
}

VALUE rb_hash(VALUE obj) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_hash", obj);
}

VALUE rb_hash_new() {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_hash_new");
}

VALUE rb_hash_aref(VALUE hash, VALUE key) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_hash_aref", hash, key);
}

VALUE rb_hash_aset(VALUE hash, VALUE key, VALUE value) {
  return (VALUE) truffle_invoke(hash, "[]=", key, value);
  return value;
}

VALUE rb_hash_lookup(VALUE hash, VALUE key) {
  return rb_hash_lookup2(hash, key, Qnil);
}

VALUE rb_hash_lookup2(VALUE hash, VALUE key, VALUE default_value) {
  return (VALUE) truffle_invoke(hash, "fetch", key, default_value);
}

VALUE rb_hash_set_ifnone(VALUE hash, VALUE if_none) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_hash_set_ifnone", hash, if_none);
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
  return (VALUE) truffle_invoke(hash, "clear");
}

VALUE rb_hash_delete(VALUE hash, VALUE key) {
  return (VALUE) truffle_invoke(hash, "delete", key);
}

VALUE rb_hash_delete_if(VALUE hash) {
  if (rb_block_given_p()) {
    return rb_funcall_with_block(hash, rb_intern("delete_if"), 0, NULL, rb_block_proc());
  } else {
    return (VALUE) truffle_invoke(hash, "delete_if");
  }
}

void rb_hash_foreach(VALUE hash, int (*func)(ANYARGS), VALUE farg) {
  rb_tr_error("rb_hash_foreach not implemented");
}

VALUE rb_hash_size(VALUE hash) {
  return (VALUE) truffle_invoke(hash, "size");
}

// Class

const char* rb_class2name(VALUE ruby_class) {
  return RSTRING_PTR(rb_class_name(ruby_class));
}

VALUE rb_class_real(VALUE ruby_class) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_class_real", ruby_class);
}

VALUE rb_class_superclass(VALUE ruby_class) {
  return (VALUE) truffle_invoke(ruby_class, "superclass");
}

VALUE rb_obj_class(VALUE object) {
  return rb_class_real(rb_class_of(object));
}

VALUE CLASS_OF(VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "CLASS_OF", object);
}

VALUE rb_class_of(VALUE object) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_class_of", object);
}

VALUE rb_obj_alloc(VALUE ruby_class) {
  return (VALUE) truffle_invoke(ruby_class, "__allocate__");
}

VALUE rb_class_path(VALUE ruby_class) {
  return (VALUE) truffle_invoke(ruby_class, "name");
}

VALUE rb_path2class(const char *string) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_path_to_class", rb_str_new_cstr(string));
}

VALUE rb_path_to_class(VALUE pathname) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_path_to_class", pathname);
}

VALUE rb_class_name(VALUE ruby_class) {
  VALUE name = truffle_invoke(ruby_class, "name");

  if (NIL_P(name)) {
    return rb_class_name(rb_obj_class(ruby_class));
  } else {
    return name;
  }
}

VALUE rb_class_new(VALUE super) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_class_new", super);
}

VALUE rb_class_new_instance(int argc, const VALUE *argv, VALUE klass) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_class_new_instance", klass, rb_ary_new4(argc, argv));
}

VALUE rb_cvar_defined(VALUE klass, ID id) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_cvar_defined", klass, id);
}

VALUE rb_cvar_get(VALUE klass, ID id) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_cvar_get", klass, id);
}

void rb_cvar_set(VALUE klass, ID id, VALUE val) {
  truffle_invoke(RUBY_CEXT, "rb_cvar_set", klass, id, val);
}

VALUE rb_cv_get(VALUE klass, const char *name) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_cv_get", klass, rb_str_new_cstr(name));
}

void rb_cv_set(VALUE klass, const char *name, VALUE val) {
  truffle_invoke(RUBY_CEXT, "rb_cv_set", klass, rb_str_new_cstr(name), val);
}

void rb_define_attr(VALUE klass, const char *name, int read, int write) {
  truffle_invoke(RUBY_CEXT, "rb_define_attr", klass, ID2SYM(rb_intern(name)), read, write);
}

void rb_define_class_variable(VALUE klass, const char *name, VALUE val) {
  truffle_invoke(RUBY_CEXT, "rb_cv_set", klass, rb_str_new_cstr(name), val);
}

// Proc

VALUE rb_proc_new(void *function, VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_proc_new", truffle_address_to_function(function), value);
}

// Utilities

VALUE rb_enumeratorize(VALUE obj, VALUE meth, int argc, const VALUE *argv) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_enumeratorize", obj, meth, rb_ary_new4(argc, argv));
}

// Calls

int rb_respond_to(VALUE object, ID name) {
  return truffle_invoke_b((void *)object, "respond_to?", name);
}

VALUE rb_funcallv(VALUE object, ID name, int args_count, const VALUE *args) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_funcallv", object, ID2SYM(name), rb_ary_new4(args_count, args));
}

VALUE rb_funcallv_public(VALUE object, ID name, int args_count, const VALUE *args) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_funcallv_public", object, ID2SYM(name), rb_ary_new4(args_count, args));
}

VALUE rb_apply(VALUE object, ID name, VALUE args) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_apply", object, ID2SYM(name), args);
}

VALUE rb_block_call(VALUE object, ID name, int args_count, const VALUE *args, rb_block_call_func_t block_call_func, VALUE data) {
  if (rb_block_given_p()) {
    return rb_funcall_with_block(object, name, args_count, args, rb_block_proc());
  } else if (block_call_func == NULL) {
    return rb_funcallv(object, name, args_count, args);
  } else {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_block_call", object, ID2SYM(name), rb_ary_new4(args_count, args), block_call_func, data);
  }
}

VALUE rb_call_super(int args_count, const VALUE *args) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_call_super", rb_ary_new4(args_count, args));
}

int rb_block_given_p() {
  return rb_block_proc() != Qnil;
}

VALUE rb_block_proc(void) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_block_proc");
}

VALUE rb_yield(VALUE value) {
  if (rb_block_given_p()) {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_yield", value);
  } else {
    return truffle_invoke(RUBY_CEXT, "yield_no_block");
  }
}

VALUE rb_funcall_with_block(VALUE recv, ID mid, int argc, const VALUE *argv, VALUE pass_procval) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_funcall_with_block", recv, ID2SYM(mid), rb_ary_new4(argc, argv), pass_procval);
}

VALUE rb_yield_splat(VALUE values) {
  if (rb_block_given_p()) {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_yield_splat", values);
  } else {
    return truffle_invoke(RUBY_CEXT, "yield_no_block");
  }
}

VALUE rb_yield_values(int n, ...) {
  VALUE values = rb_ary_new_capa(n);
  for (int i = 0; i < n; i++) {
    rb_ary_store(values, i, (VALUE) truffle_get_arg(1+i));
  }
  return rb_yield_splat(values);
}

// Instance variables

VALUE rb_iv_get(VALUE object, const char *name) {
  return truffle_invoke(RUBY_CEXT, "rb_ivar_get", object, rb_str_new_cstr(name));
}

VALUE rb_iv_set(VALUE object, const char *name, VALUE value) {
  truffle_invoke(RUBY_CEXT, "rb_ivar_set", object, rb_str_new_cstr(name), value);
  return value;
}

VALUE rb_ivar_get(VALUE object, ID name) {
  return truffle_invoke(RUBY_CEXT, "rb_ivar_get", object, name);
}

VALUE rb_ivar_set(VALUE object, ID name, VALUE value) {
  truffle_invoke(RUBY_CEXT, "rb_ivar_set", object, name, value);
  return value;
}

VALUE rb_ivar_lookup(VALUE object, const char *name, VALUE default_value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_ivar_lookup", object, name, default_value);
}

VALUE rb_attr_get(VALUE object, const char *name) {
  return rb_ivar_lookup((void *)object, name, Qnil);
}

// Accessing constants

int rb_const_defined(VALUE module, ID name) {
  return truffle_invoke_b((void *)module, "const_defined?", name);
}

int rb_const_defined_at(VALUE module, ID name) {
  return truffle_invoke_b((void *)module, "const_defined?", name, Qfalse);
}

VALUE rb_const_get(VALUE module, ID name) {
  return (VALUE) truffle_invoke(module, "const_get", name);
}

VALUE rb_const_get_at(VALUE module, ID name) {
  return (VALUE) truffle_invoke(module, "const_get", name, Qfalse);
}

VALUE rb_const_get_from(VALUE module, ID name) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_const_get_from", module, name);
}

VALUE rb_const_set(VALUE module, ID name, VALUE value) {
  return (VALUE) truffle_invoke(module, "const_set", name, value);
}

VALUE rb_define_const(VALUE module, const char *name, VALUE value) {
  return rb_const_set(module, rb_str_new_cstr(name), value);
}

void rb_define_global_const(const char *name, VALUE value) {
  rb_define_const(rb_cObject, name, value);
}

// Global variables

VALUE rb_gvar_var_getter(ID id, VALUE *var, void *gvar) {
  return *var;
}

void rb_gvar_var_setter(VALUE val, ID id, VALUE *var, void *g) {
  *var = val;
}

void rb_define_hooked_variable(const char *name, VALUE *var, VALUE (*getter)(ANYARGS), void (*setter)(ANYARGS)) {
  if (!getter) {
    getter = rb_gvar_var_getter;
  }

  if (!setter) {
    setter = rb_gvar_var_setter;
  }

  truffle_invoke(RUBY_CEXT, "rb_define_hooked_variable", rb_str_new_cstr(name), var, getter, setter);
}

void rb_gvar_readonly_setter(VALUE v, ID id, void *d, void *g) {
  rb_raise(rb_eNameError, "read-only variable");
}

void rb_define_readonly_variable(const char *name, const VALUE *var) {
  rb_define_hooked_variable(name, (VALUE *)var, NULL, rb_gvar_readonly_setter);
}

void rb_define_variable(const char *name, VALUE *var) {
  rb_define_hooked_variable(name, var, 0, 0);
}

VALUE rb_f_global_variables(void) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_f_global_variables");
}

VALUE rb_gv_set(const char *name, VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_gv_set", rb_str_new_cstr(name), value);
}

VALUE rb_gv_get(const char *name) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_gv_get", rb_str_new_cstr(name));
}

VALUE rb_lastline_get(void) {
  rb_tr_error("rb_lastline_get not implemented");
}

void rb_lastline_set(VALUE val) {
  rb_tr_error("rb_lastline_set not implemented");
}

// Raising exceptions

VALUE rb_exc_new(VALUE etype, const char *ptr, long len) {
  return (VALUE) truffle_invoke(etype, "new", rb_str_new(ptr, len));
}

VALUE rb_exc_new_cstr(VALUE exception_class, const char *message) {
  return (VALUE) truffle_invoke(exception_class, "new", rb_str_new_cstr(message));
}

VALUE rb_exc_new_str(VALUE exception_class, VALUE message) {
  return (VALUE) truffle_invoke(exception_class, "new", message);
}

void rb_exc_raise(VALUE exception) {
  truffle_invoke(RUBY_CEXT, "rb_exc_raise", exception);
  abort();
}

VALUE rb_protect(VALUE (*function)(VALUE), VALUE data, int *status) {
  // TODO CS 23-Jul-16
  return function(data);
}

void rb_jump_tag(int status) {
  if (status) {
    rb_tr_error("rb_jump_tag not implemented");
  }
}

void rb_set_errinfo(VALUE error) {
  truffle_invoke(RUBY_CEXT, "rb_set_errinfo", error);
}

void rb_syserr_fail(int eno, const char *message) {
  truffle_invoke(RUBY_CEXT, "rb_syserr_fail", eno, message == NULL ? Qnil : rb_str_new_cstr(message));
}

void rb_sys_fail(const char *message) {
  truffle_invoke(RUBY_CEXT, "rb_sys_fail", message == NULL ? Qnil : rb_str_new_cstr(message));
}

VALUE rb_ensure(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*e_proc)(ANYARGS), VALUE data2) {
  return truffle_invoke(RUBY_CEXT, "rb_ensure", b_proc, data1, e_proc, data2);
}

VALUE rb_rescue(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2) {
  return truffle_invoke(RUBY_CEXT, "rb_rescue", b_proc, data1, r_proc, data2);
}

VALUE rb_rescue2(VALUE (*b_proc)(ANYARGS), VALUE data1, VALUE (*r_proc)(ANYARGS), VALUE data2, ...) {
  VALUE rescued = rb_ary_new();
  int n = 4;
  while (true) {
    VALUE arg = truffle_get_arg(n);
    if (arg == NULL) {
      break;
    }
    rb_ary_push(rescued, arg);
    n++;
  }
  return truffle_invoke(RUBY_CEXT, "rb_rescue2", b_proc, data1, r_proc, data2, rescued);
}

VALUE rb_make_backtrace(void) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_make_backtrace");
}

void rb_throw(const char *tag, VALUE val) {
  return rb_throw_obj(rb_intern(tag), val);
}

void rb_throw_obj(VALUE tag, VALUE value) {
  truffle_invoke(rb_mKernel, "throw", tag, value == NULL ? Qnil : value);
}

VALUE rb_catch(const char *tag, VALUE (*func)(), VALUE data) {
  return rb_catch_obj(rb_intern(tag), func, data);
}

VALUE rb_catch_obj(VALUE t, VALUE (*func)(), VALUE data) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_catch_obj", t, func, data);
}

// Defining classes, modules and methods

VALUE rb_define_class(const char *name, VALUE superclass) {
  return rb_define_class_under(rb_cObject, name, superclass);
}

VALUE rb_define_class_under(VALUE module, const char *name, VALUE superclass) {
  return rb_define_class_id_under(module, rb_str_new_cstr(name), superclass);
}

VALUE rb_define_class_id_under(VALUE module, ID name, VALUE superclass) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_define_class_under", module, name, superclass);
}

VALUE rb_define_module(const char *name) {
  return rb_define_module_under(rb_cObject, name);
}

VALUE rb_define_module_under(VALUE module, const char *name) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_define_module_under", module, rb_str_new_cstr(name));
}

void rb_include_module(VALUE module, VALUE to_include) {
  truffle_invoke(module, "include", to_include);
}

void rb_define_method(VALUE module, const char *name, void *function, int argc) {
  if (function == rb_f_notimplement) {
    truffle_invoke(RUBY_CEXT, "rb_define_method_undefined", module, rb_str_new_cstr(name));
  } else {
    truffle_invoke(RUBY_CEXT, "rb_define_method", module, rb_str_new_cstr(name), truffle_address_to_function(function), argc);
  }
}

void rb_define_private_method(VALUE module, const char *name, void *function, int argc) {
  rb_define_method(module, name, function, argc);
  truffle_invoke(module, "private", rb_str_new_cstr(name));
}

void rb_define_protected_method(VALUE module, const char *name, void *function, int argc) {
  rb_define_method(module, name, function, argc);
  truffle_invoke(module, "protected", rb_str_new_cstr(name));
}

void rb_define_module_function(VALUE module, const char *name, void *function, int argc) {
  rb_define_method(module, name, function, argc);
  truffle_invoke(RUBY_CEXT, "cext_module_function", module, rb_intern(name));
}

void rb_define_global_function(const char *name, void *function, int argc) {
  rb_define_module_function(rb_mKernel, name, function, argc);
}

void rb_define_singleton_method(VALUE object, const char *name, void *function, int argc) {
  rb_define_method(truffle_invoke(object, "singleton_class"), name, function, argc);
}

void rb_define_alias(VALUE module, const char *new_name, const char *old_name) {
  rb_alias(module, rb_str_new_cstr(new_name), rb_str_new_cstr(old_name));
}

void rb_alias(VALUE module, ID new_name, ID old_name) {
  truffle_invoke(RUBY_CEXT, "rb_alias", module, new_name, old_name);
}

void rb_undef_method(VALUE module, const char *name) {
  rb_undef(module, rb_str_new_cstr(name));
}

void rb_undef(VALUE module, ID name) {
  truffle_invoke(RUBY_CEXT, "rb_undef", module, name);
}

void rb_attr(VALUE ruby_class, ID name, int read, int write, int ex) {
  truffle_invoke(RUBY_CEXT, "rb_attr", ruby_class, name, read, write, ex);
}

void rb_define_alloc_func(VALUE ruby_class, rb_alloc_func_t alloc_function) {
  truffle_invoke(RUBY_CEXT, "rb_define_alloc_func", ruby_class, truffle_address_to_function(alloc_function));
}

VALUE rb_obj_method(VALUE obj, VALUE vid) {
  return (VALUE) truffle_invoke(obj, "method", rb_intern_str(vid));
}

// Rational

VALUE rb_Rational(VALUE num, VALUE den) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Rational", num, den);
}

VALUE rb_rational_raw(VALUE num, VALUE den) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_rational_raw", num, den);
}

VALUE rb_rational_new(VALUE num, VALUE den) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_rational_new", num, den);
}

VALUE rb_rational_num(VALUE rat) {
  return (VALUE) truffle_invoke(rat, "numerator");
}

VALUE rb_rational_den(VALUE rat) {
  return (VALUE) truffle_invoke(rat, "denominator");
}

VALUE rb_flt_rationalize_with_prec(VALUE value, VALUE precision) {
  return (VALUE) truffle_invoke(value, "rationalize", precision);
}

VALUE rb_flt_rationalize(VALUE value) {
  return (VALUE) truffle_invoke(value, "rationalize");
}

// Complex

VALUE rb_Complex(VALUE real, VALUE imag) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Complex", real, imag);
}

VALUE rb_complex_new(VALUE real, VALUE imag) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_complex_new", real, imag);
}

VALUE rb_complex_raw(VALUE real, VALUE imag) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_complex_raw", real, imag);
}

VALUE rb_complex_polar(VALUE r, VALUE theta) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_complex_polar", r, theta);
}

VALUE rb_complex_set_real(VALUE complex, VALUE real) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_complex_set_real", complex, real);
}

VALUE rb_complex_set_imag(VALUE complex, VALUE imag) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_complex_set_imag", complex, imag);
}

// Range

VALUE rb_range_new(VALUE beg, VALUE end, int exclude_end) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_range_new", beg, end, exclude_end);
}

// Time

VALUE rb_time_new(time_t sec, long usec) {
  return (VALUE) truffle_invoke(rb_cTime, "at", sec, usec);
}

VALUE rb_time_nano_new(time_t sec, long nsec) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_time_nano_new", sec, nsec);
}

VALUE rb_time_num_new(VALUE timev, VALUE off) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_time_num_new", timev, off);
}

struct timeval rb_time_interval(VALUE time_val) {
  truffle_invoke(RUBY_CEXT, "rb_time_interval_acceptable", time_val);

  struct timeval result;

  VALUE time = rb_time_num_new(time_val, Qnil);
  result.tv_sec = truffle_invoke_l((void *)time, "tv_sec");
  result.tv_usec = truffle_invoke_l((void *)time, "tv_usec");

  return result;
}

struct timeval rb_time_timeval(VALUE time_val) {
  struct timeval result;

  VALUE time = rb_time_num_new(time_val, Qnil);
  result.tv_sec = truffle_invoke_l((void *)time, "tv_sec");
  result.tv_usec = truffle_invoke_l((void *)time, "tv_usec");

  return result;
}

struct timespec rb_time_timespec(VALUE time_val) {
  struct timespec result;

  VALUE time = rb_time_num_new(time_val, Qnil);
  result.tv_sec = truffle_invoke_l((void *)time, "tv_sec");
  result.tv_nsec = truffle_invoke_l((void *)time, "tv_nsec");

  return result;
}

VALUE rb_time_timespec_new(const struct timespec *ts, int offset) {
  VALUE is_utc = rb_boolean(offset == INT_MAX-1);
  VALUE is_local = rb_boolean(offset == INT_MAX);
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_time_timespec_new", ts->tv_sec, ts->tv_nsec, offset, is_utc, is_local);
}

void rb_timespec_now(struct timespec *ts) {
  struct timeval tv = rb_time_timeval((VALUE) truffle_invoke(rb_cTime, "now"));
  ts->tv_sec = tv.tv_sec;
  ts->tv_nsec = tv.tv_usec * 1000;
}

// Regexp

VALUE rb_backref_get(void) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_backref_get");
}

VALUE rb_reg_match_pre(VALUE match) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_match_pre", match);
}

VALUE rb_reg_new(const char *s, long len, int options) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_new", truffle_read_n_string(s, len), options);
}

VALUE rb_reg_new_str(VALUE s, int options) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_new_str", s, options);
}

VALUE rb_reg_nth_match(int nth, VALUE match) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_nth_match", nth, match);
}

VALUE rb_reg_options(VALUE re) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_options", re);
}

VALUE rb_reg_regcomp(VALUE str) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_regcomp", str);
}

VALUE rb_reg_match(VALUE re, VALUE str) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_reg_match", re, str);
}

// Marshal

VALUE rb_marshal_dump(VALUE obj, VALUE port) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_marshal_dump", obj, port);
}

VALUE rb_marshal_load(VALUE port) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_marshal_load", port);
}

// Mutexes

VALUE rb_mutex_new(void) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_new");
}

VALUE rb_mutex_locked_p(VALUE mutex) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_locked_p", mutex);
}

VALUE rb_mutex_trylock(VALUE mutex) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_trylock", mutex);
}

VALUE rb_mutex_lock(VALUE mutex) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_lock", mutex);
}

VALUE rb_mutex_unlock(VALUE mutex) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_unlock", mutex);
}

VALUE rb_mutex_sleep(VALUE mutex, VALUE timeout) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_sleep", mutex, timeout);
}

VALUE rb_mutex_synchronize(VALUE mutex, VALUE (*func)(VALUE arg), VALUE arg) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_mutex_synchronize", mutex, func, arg);
}

// GC

void rb_gc_register_address(VALUE *address) {
}

VALUE rb_gc_enable() {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_gc_enable");
}

VALUE rb_gc_disable() {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_gc_disable");
}

// Threads

void *rb_thread_call_with_gvl(gvl_call function, void *data1) {
  return function(data1);
}

void *rb_thread_call_without_gvl(gvl_call function, void *data1, rb_unblock_function_t *unblock_function, void *data2) {
  // TODO CS 9-Mar-17 truffle_invoke escapes LLVMAddress into Ruby, which goes wrong when Ruby tries to call the callbacks
  return truffle_invoke(RUBY_CEXT, "rb_thread_call_without_gvl", function, data1, unblock_function, data2);
}

int rb_thread_alone(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_thread_alone");
}

VALUE rb_thread_current(void) {
  return (VALUE) truffle_invoke(rb_tr_get_Thread(), "current");
}

VALUE rb_thread_local_aref(VALUE thread, ID id) {
  return (VALUE) truffle_invoke(thread, "[]", ID2SYM(id));
}

VALUE rb_thread_local_aset(VALUE thread, ID id, VALUE val) {
  return (VALUE) truffle_invoke(thread, "[]=", ID2SYM(id), val);
}

void rb_thread_wait_for(struct timeval time) {
  double seconds = (double)time.tv_sec + (double)time.tv_usec/1000000;
  truffle_invoke(rb_mKernel, "sleep", seconds);
}

VALUE rb_thread_wakeup(VALUE thread) {
  return (VALUE) truffle_invoke(thread, "wakeup");
}

VALUE rb_thread_create(VALUE (*fn)(ANYARGS), void *arg) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_thread_create", fn, arg);
}

rb_nativethread_id_t rb_nativethread_self() {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_nativethread_self");
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
      rb_tr_error("rb_io_wait_writable wait case not implemented");
      return true;

    default:
      return false;
  }
}

void rb_thread_wait_fd(int fd) {
  truffle_invoke(RUBY_CEXT, "rb_thread_wait_fd", fd);
}

NORETURN(void rb_eof_error(void)) {
  rb_tr_error("rb_eof_error not implemented");
}

VALUE rb_io_addstr(VALUE io, VALUE str) {
  return (VALUE) truffle_invoke(io, "<<", str);
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
  return (VALUE) truffle_invoke(io, "close");
}

VALUE rb_io_print(int argc, const VALUE *argv, VALUE out) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_io_print", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_printf(int argc, const VALUE *argv, VALUE out) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_io_printf", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_puts(int argc, const VALUE *argv, VALUE out) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_io_puts", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_write(VALUE io, VALUE str) {
  return (VALUE) truffle_invoke(io, "write", str);
}

VALUE rb_io_binmode(VALUE io) {
  return (VALUE) truffle_invoke(io, "binmode");
}

int rb_thread_fd_writable(int fd) {
  return truffle_invoke_i(RUBY_CEXT, "rb_thread_fd_writable", fd);
}

int rb_cloexec_open(const char *pathname, int flags, mode_t mode) {
  int fd = open(pathname, flags, mode);
  if (fd >= 0) {
    rb_fd_fix_cloexec(fd);
  }
  return fd;
}

VALUE rb_file_open(const char *fname, const char *modestr) {
  return (VALUE) truffle_invoke(rb_cFile, "open", rb_str_new_cstr(fname), rb_str_new_cstr(modestr));
}

VALUE rb_file_open_str(VALUE fname, const char *modestr) {
  return (VALUE) truffle_invoke(rb_cFile, "open", fname, rb_str_new_cstr(modestr));
}

VALUE rb_get_path(VALUE object) {
  return (VALUE) truffle_invoke(rb_cFile, "path", object);
}

int rb_tr_readable(int mode) {
  return truffle_invoke_i(RUBY_CEXT, "rb_tr_readable", mode);
}

int rb_tr_writable(int mode) {
  return truffle_invoke_i(RUBY_CEXT, "rb_tr_writable", mode);
}

// Structs

VALUE rb_struct_aref(VALUE s, VALUE idx) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_struct_aref", s, idx);
}

VALUE rb_struct_aset(VALUE s, VALUE idx, VALUE val) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_struct_aset", s, idx, val);
}

VALUE rb_struct_define(const char *name, ...) {
  VALUE *rb_name = name == NULL ? truffle_read(RUBY_CEXT, "Qnil") : rb_str_new_cstr(name);
  VALUE *ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  while ((arg = (char *)truffle_get_arg(i + 1)) != NULL) {
    rb_ary_store(ary, i++, rb_str_new_cstr(arg));
  }
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_struct_define_no_splat", rb_name, ary);
}

VALUE rb_struct_new(VALUE klass, ...) {
  int members = truffle_invoke_i(RUBY_CEXT, "rb_struct_size", klass);
  VALUE *ary = rb_ary_new();
  int i = 0;
  char *arg = NULL;
  while (i < members) {
    VALUE arg = truffle_get_arg(i + 1);
    rb_ary_store(ary, i++, arg);
  }
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_struct_new_no_splat", klass, ary);
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

VALUE rb_struct_define_under(VALUE outer, const char *name, ...) {
  rb_tr_error("rb_struct_define_under not implemented");
}

// Data

VALUE rb_data_object_wrap(VALUE klass, void *datap, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_data_object_wrap", klass == NULL ? rb_cObject : klass, datap, dmark, dfree);
}

struct RData *RDATA(VALUE value) {
  return (struct RData *)truffle_invoke(RUBY_CEXT, "RDATA", value);
}

// Typed data

VALUE rb_data_typed_object_wrap(VALUE ruby_class, void *data, const rb_data_type_t *data_type) {
  // TODO CS 6-Mar-17 work around the issue with LLVM addresses escaping into Ruby by making data_type a uintptr_t
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_data_typed_object_wrap", ruby_class == NULL ? rb_cObject : ruby_class, data, (uintptr_t) data_type);
}

VALUE rb_data_typed_object_zalloc(VALUE ruby_class, size_t size, const rb_data_type_t *data_type) {
  VALUE obj = rb_data_typed_object_wrap(ruby_class, 0, data_type);
  DATA_PTR(obj) = calloc(1, size);
  return obj;
}

VALUE rb_data_typed_object_make(VALUE ruby_class, const rb_data_type_t *type, void **data_pointer, size_t size) {
  TypedData_Make_Struct0(result, ruby_class, void, size, type, *data_pointer);
  return result;
}

void *rb_check_typeddata(VALUE value, const rb_data_type_t *data_type) {
  // TODO CS 6-Mar-17 work around the issue with LLVM addresses escaping into Ruby by making data_type a uintptr_t
  if ((uintptr_t) rb_tr_hidden_variable_get(value, "data_type") != (uintptr_t) data_type) {
    rb_raise(rb_eTypeError, "wrong argument type");
  }
  return RTYPEDDATA_DATA(value);
}

// VM

VALUE rb_tr_ruby_verbose_ptr;

VALUE *rb_ruby_verbose_ptr(void) {
  rb_tr_ruby_verbose_ptr = truffle_invoke(RUBY_CEXT, "rb_ruby_verbose_ptr");
  return &rb_tr_ruby_verbose_ptr;
}

VALUE rb_tr_ruby_debug_ptr;

VALUE *rb_ruby_debug_ptr(void) {
  rb_tr_ruby_debug_ptr = truffle_invoke(RUBY_CEXT, "rb_ruby_debug_ptr");
  return &rb_tr_ruby_debug_ptr;
}

// Non-standard

void rb_tr_error(const char *message) {
  truffle_invoke(RUBY_CEXT, "rb_tr_error", rb_str_new_cstr(message));
  abort();
}

void rb_tr_log_warning(const char *message) {
  truffle_invoke(RUBY_CEXT, "rb_tr_log_warning", rb_str_new_cstr(message));
}

long rb_tr_obj_id(VALUE object) {
  return truffle_invoke_l(RUBY_CEXT, "rb_tr_obj_id", object);
}

void rb_p(VALUE obj) {
  truffle_invoke(rb_mKernel, "puts", truffle_invoke(obj, "inspect"));
}

VALUE rb_java_class_of(VALUE obj) {
  return truffle_invoke(RUBY_CEXT, "rb_java_class_of", obj);
}

VALUE rb_java_to_string(VALUE obj) {
  return truffle_invoke(RUBY_CEXT, "rb_java_to_string", obj);
}

void *rb_tr_handle_for_managed(void *managed) {
  return truffle_handle_for_managed(managed);
}

void *rb_tr_handle_for_managed_leaking(void *managed) {
  rb_tr_log_warning("rb_tr_handle_for_managed without matching rb_tr_release_handle; handles will be leaking");
  return rb_tr_handle_for_managed(managed);
}

VALUE rb_tr_managed_from_handle_or_null(void *handle) {
  if (handle == NULL) {
    return NULL;
  } else {
    return rb_tr_managed_from_handle(handle);
  }
}

void *rb_tr_managed_from_handle(void *handle) {
  return truffle_managed_from_handle(handle);
}

void rb_tr_release_handle(void *handle) {
  truffle_release_handle(handle);
}
