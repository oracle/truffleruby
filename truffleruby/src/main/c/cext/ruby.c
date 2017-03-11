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

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>
#include <limits.h>

#include <truffle.h>

#include <ruby.h>

// Private helper macros just for ruby.c

 #define RBOOL(c) ((c) ? Qtrue : Qfalse)

// Helpers

VALUE rb_f_notimplement(int args_count, const VALUE *args, VALUE object) {
  rb_tr_error("rb_f_notimplement");
}

// Memory

void *rb_alloc_tmp_buffer(VALUE *buffer_pointer, long length) {
  rb_tr_error("rb_alloc_tmp_buffer not implemented");
}

void *rb_alloc_tmp_buffer2(VALUE *buffer_pointer, long count, size_t size) {
  rb_tr_error("rb_alloc_tmp_buffer2 not implemented");
}

void rb_free_tmp_buffer(VALUE *buffer_pointer) {
  rb_tr_error("rb_free_tmp_buffer not implemented");
}

// Types

int rb_type(VALUE value) {
  return truffle_invoke_i(RUBY_CEXT, "rb_type", value);
}

bool RB_TYPE_P(VALUE value, int type) {
  return truffle_invoke_b(RUBY_CEXT, "RB_TYPE_P", value, type);
}

void rb_check_type(VALUE value, int type) {
  truffle_invoke(RUBY_CEXT, "rb_check_type", value);
}

VALUE rb_obj_is_instance_of(VALUE object, VALUE ruby_class) {
  return truffle_invoke(RUBY_CEXT, "rb_obj_is_instance_of", object, ruby_class);
}

VALUE rb_obj_is_kind_of(VALUE object, VALUE ruby_class) {
  return truffle_invoke((void *)object, "kind_of?", ruby_class);
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
  rb_tr_error("LL2NUM not implemented");
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
  return (VALUE) truffle_invoke((void *)object, "dup");
}

VALUE rb_any_to_s(VALUE object) {
  return (VALUE) truffle_invoke((void *)object, "to_s");
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
  truffle_invoke((void *)module, "extend_object", object);
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
  return (VALUE) truffle_invoke((void *)object, "object_id");
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
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_obj_instance_eval", self, rb_ary_new4(argc, argv));
}

VALUE rb_ivar_defined(VALUE object, ID id) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_ivar_defined", object, id);
}

VALUE rb_equal_opt(VALUE a, VALUE b) {
  rb_tr_error("rb_equal_opt not implemented");
}

VALUE rb_class_inherited_p(VALUE module, VALUE object) {
  return (VALUE) truffle_invoke((void *)module, "<=", object);
}

VALUE rb_equal(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke((void *)a, "===", b);
}

int RB_BUILTIN_TYPE(VALUE object) {
  rb_tr_error("RB_BUILTIN_TYPE not implemented");
}

VALUE rb_obj_taint(VALUE object) {
  return (VALUE) truffle_invoke((void *)object, "taint");
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
  return (VALUE) truffle_invoke((void *)object, "freeze");
}

VALUE rb_obj_frozen_p(VALUE object) {
  return truffle_invoke((void *)object, "frozen?");
}

// Integer

VALUE rb_Integer(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_Integer", value);
}

int rb_integer_pack(VALUE value, void *words, size_t numwords, size_t wordsize, size_t nails, int flags) {
  rb_tr_error("rb_integer_pack not implemented");
}

VALUE rb_integer_unpack(const void *words, size_t numwords, size_t wordsize, size_t nails, int flags) {
  rb_tr_error("rb_integer_unpack not implemented");
}

size_t rb_absint_size(VALUE value, int *nlz_bits_ret) {
  rb_tr_error("rb_absint_size not implemented");
}

VALUE rb_cstr_to_inum(const char* string, int base, int raise) {
  rb_tr_error("rb_cstr_to_inum not implemented");
}

double rb_big2dbl(VALUE x) {
  rb_tr_error("rb_big2dbl not implemented");
}

VALUE rb_dbl2big(double d) {
  rb_tr_error("rb_dbl2big not implemented");
}

LONG_LONG rb_big2ll(VALUE x) {
  rb_tr_error("rb_big2ll not implemented");
}

long rb_big2long(VALUE x) {
  rb_tr_error("rb_big2long not implemented");
}

VALUE rb_big2str(VALUE x, int base) {
  rb_tr_error("rb_big2str not implemented");
}

unsigned long rb_big2ulong(VALUE x) {
  rb_tr_error("rb_big2ulong not implemented");
}

VALUE rb_big_cmp(VALUE x, VALUE y) {
  rb_tr_error("rb_big_cmp not implemented");
}

void rb_big_pack(VALUE val, unsigned long *buf, long num_longs) {
  rb_tr_error("rb_big_pack not implemented");
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
    return (VALUE) truffle_invoke(RUBY_CEXT, "to_ruby_string", string);
  } else {
    return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new_cstr", truffle_read_string(string));
  }
}

VALUE rb_str_new_shared(VALUE string) {
  rb_tr_error("rb_str_new_shared not implemented");
}

VALUE rb_str_new_with_class(VALUE klass, const char *string, long len) {
  rb_tr_error("rb_str_new_with_class not implemented");
}

VALUE rb_intern_str(VALUE string) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_intern_str", string);
}

VALUE rb_str_cat(VALUE string, const char *to_concat, long length) {
  truffle_invoke((void *)string, "concat", rb_str_new(to_concat, length));
  return string;
}

VALUE rb_str_cat2(VALUE string, const char *to_concat) {
  truffle_invoke((void *)string, "concat", rb_str_new_cstr(to_concat));
  return string;
}

VALUE rb_str_to_str(VALUE string) {
  return (VALUE) truffle_invoke((void *)string, "to_str");
}

VALUE rb_str_buf_new(long capacity) {
  return rb_str_new_cstr("");
}

VALUE rb_vsprintf(const char *format, va_list args) {
  // TODO CS 7-May-17 this needs to use the Ruby sprintf, not C's

  va_list args_copy;
  va_copy(args_copy, args);
  int length = vsnprintf(NULL, 0, format, args_copy);
  //va_end(args_copy); CS 4-May-17 why does this crash?
  if (length < 0) {
    rb_tr_error("vsnprintf error");
  }
  // TODO CS 4-May-17 allocate a native Ruby string in the first place?
  char *buffer = malloc(length + 1);
  vsnprintf(buffer, length + 1, format, args);
  VALUE string = rb_str_new_cstr(buffer);
  free(buffer);
  return string;
}

VALUE rb_str_append(VALUE string, VALUE to_append) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_append", string, to_append);
}

void rb_str_set_len(VALUE string, long length) {
  rb_tr_error("rb_str_set_len not implemented");
}

VALUE rb_str_new_frozen(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_str_new_frozen", value);
}

VALUE rb_String(VALUE value) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_String", value);
}

VALUE rb_str_resize(VALUE string, long length) {
  rb_tr_error("rb_str_resize not implemented");
}

VALUE rb_str_split(VALUE string, const char *split) {
  return (VALUE) truffle_invoke(string, "split", rb_str_new_cstr(split));
}

void rb_str_modify(VALUE string) {
  // Does nothing because writing to the string pointer will cause necessary invalidations anyway
}

VALUE rb_cstr2inum(const char *string, int base) {
  rb_tr_error("rb_cstr2inum not implemented");
}

VALUE rb_str2inum(VALUE string, int base) {
  rb_tr_error("rb_str2inum not implemented");
}

VALUE rb_str_buf_new_cstr(const char *string) {
  rb_tr_error("rb_str_buf_new_cstr not implemented");
}

int rb_str_cmp(VALUE a, VALUE b) {
  return truffle_invoke_i((void *)a, "<=>", b);
}

VALUE rb_str_buf_cat(VALUE string, const char *to_concat, long length) {
  rb_tr_error("rb_str_buf_cat not implemented");
}

rb_encoding *rb_to_encoding(VALUE encoding) {
  return truffle_invoke(RUBY_CEXT, "rb_to_encoding", encoding);
}

VALUE rb_str_conv_enc(VALUE string, rb_encoding *from, rb_encoding *to) {
  return rb_str_conv_enc_opts(string, from, to, 0, Qnil);
}

VALUE rb_str_conv_enc_opts(VALUE string, rb_encoding *from, rb_encoding *to, int ecflags, VALUE ecopts) {
  rb_tr_error("rb_str_conv_enc_opts not implemented");
}

VALUE rb_external_str_new_with_enc(const char *string, long len, rb_encoding *eenc) {
  rb_tr_error("rb_external_str_with_enc not implemented");
}

VALUE rb_external_str_with_enc(VALUE string, rb_encoding *eenc) {
  rb_tr_error("rb_external_str_with_enc not implemented");
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
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "rb_default_external_encoding"));
}

rb_encoding *rb_default_internal_encoding(void) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "rb_default_internal_encoding"));
}

rb_encoding *rb_locale_encoding(void) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "rb_locale_encoding"));
}

int rb_locale_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_locale_encindex");
}

rb_encoding *rb_filesystem_encoding(void) {
  return rb_to_encoding(truffle_invoke(RUBY_CEXT, "rb_filesystem_encoding"));
}

int rb_filesystem_encindex(void) {
  return truffle_invoke_i(RUBY_CEXT, "rb_filesystem_encindex");
}

rb_encoding *get_encoding(VALUE string) {
  return rb_to_encoding(truffle_invoke((void *)string, "encoding"));
}

VALUE rb_str_intern(VALUE string) {
  return (VALUE) truffle_invoke((void *)string, "intern");
}

VALUE rb_str_length(VALUE string) {
  return (VALUE) truffle_invoke((void *)string, "length");
}

VALUE rb_str_plus(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke((void *)a, "+", b);
}

VALUE rb_str_subseq(VALUE string, long beg, long len) {
  rb_tr_error("rb_str_subseq not implemented");
}

VALUE rb_str_substr(VALUE string, long beg, long len) {
  return (VALUE) truffle_invoke((void *)string, "[]", beg, len);
}

st_index_t rb_str_hash(VALUE string) {
  return (st_index_t) truffle_invoke_l((void *)string, "hash");
}

void rb_str_update(VALUE string, long beg, long len, VALUE value) {
  truffle_invoke((void *)string, "[]=", beg, len, value);
}

VALUE rb_str_equal(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke((void *)a, "==", b);
}

void rb_str_free(VALUE string) {
  rb_tr_error("rb_str_free not implemented");
}

unsigned int rb_enc_codepoint_len(const char *p, const char *e, int *len_p, rb_encoding *encoding) {
  rb_tr_error("rb_enc_codepoint_len not implemented");
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
  return rb_to_encoding(truffle_invoke(rb_cEncoding, "find", rb_str_new_cstr(name)));
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
  return truffle_invoke((void *)obj, "encoding");
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

// Symbol

ID rb_to_id(VALUE name) {
  return SYM2ID((VALUE) truffle_invoke((void *)name, "to_sym"));
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
  return (VALUE) truffle_invoke((void *)string, "to_s");
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

VALUE *RARRAY_PTR(VALUE array) {
  return (VALUE*) truffle_invoke(RUBY_CEXT, "RARRAY_PTR", array);
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

VALUE rb_ary_new4(long n, const VALUE *values) {
  VALUE array = rb_ary_new_capa(n);
  for (int i = 0; i < n; i++) {
    rb_ary_store(array, i, values[i]);
  }
  return array;
}

VALUE rb_ary_push(VALUE array, VALUE value) {
  truffle_invoke((void *)array, "push", value);
  return array;
}

VALUE rb_ary_pop(VALUE array) {
  return (VALUE) truffle_invoke((void *)array, "pop");
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
  return (VALUE) truffle_invoke((void *)array, "unshift", value);
}

VALUE rb_ary_aref(int n, const VALUE* values, VALUE array) {
  // TODO CS 21-Feb-17 if values is an RARRAY_PTR, go back to the array directly
  return (VALUE) truffle_invoke(RUBY_CEXT, "send_splatted", array, rb_str_new_cstr("[]"), rb_ary_new4(n, values));
}

VALUE rb_ary_clear(VALUE array) {
  return (VALUE) truffle_invoke((void *)array, "clear");
}

VALUE rb_ary_delete(VALUE array, VALUE value) {
  return (VALUE) truffle_invoke((void *)array, "delete", value);
}

VALUE rb_ary_delete_at(VALUE array, long n) {
  return (VALUE) truffle_invoke((void *)array, "delete_at", n);
}

VALUE rb_ary_includes(VALUE array, VALUE value) {
  return (VALUE) truffle_invoke((void *)array, "include?", value);
}

VALUE rb_ary_join(VALUE array, VALUE sep) {
  return (VALUE) truffle_invoke((void *)array, "join", sep);
}

VALUE rb_ary_to_s(VALUE array) {
  return (VALUE) truffle_invoke((void *)array, "to_s");
}

VALUE rb_ary_reverse(VALUE array) {
  return (VALUE) truffle_invoke((void *)array, "reverse!");
}

VALUE rb_ary_shift(VALUE array) {
  return (VALUE) truffle_invoke((void *)array, "shift");
}

VALUE rb_ary_concat(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke((void *)a, "concat", b);
}

VALUE rb_ary_plus(VALUE a, VALUE b) {
  return (VALUE) truffle_invoke((void *)a, "+", b);
}

VALUE rb_iterate(VALUE (*function)(), VALUE arg1, VALUE (*block)(), VALUE arg2) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_iterate", function, arg1, block, arg2);
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
  return (VALUE) truffle_invoke((void *)array, "[]", start, length);
}

VALUE rb_check_array_type(VALUE array) {
  return rb_check_convert_type(array, T_ARRAY, "Array", "to_ary");
}

// Hash

VALUE rb_hash(VALUE obj) {
  return (VALUE) truffle_invoke((void *)obj, "hash");
}

VALUE rb_hash_new() {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_hash_new");
}

VALUE rb_hash_aref(VALUE hash, VALUE key) {
  return truffle_read(hash, key);
}

VALUE rb_hash_aset(VALUE hash, VALUE key, VALUE value) {
  return (VALUE) truffle_invoke((void *)hash, "[]=", key, value);
  return value;
}

VALUE rb_hash_lookup(VALUE hash, VALUE key) {
  return rb_hash_lookup2(hash, key, Qnil);
}

VALUE rb_hash_lookup2(VALUE hash, VALUE key, VALUE default_value) {
  return (VALUE) truffle_invoke((void *)hash, "fetch", key, default_value);
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
  return (VALUE) truffle_invoke((void *)hash, "clear");
}

VALUE rb_hash_delete(VALUE hash, VALUE key) {
  return (VALUE) truffle_invoke((void *)hash, "delete", key);
}

VALUE rb_hash_delete_if(VALUE hash) {
  rb_tr_error("rb_hash_delete_if not implemented");
}

void rb_hash_foreach(VALUE hash, int (*func)(ANYARGS), VALUE farg) {
  rb_tr_error("rb_hash_foreach not implemented");
}

VALUE rb_hash_size(VALUE hash) {
  return (VALUE) truffle_invoke((void *)hash, "size");
}

// Class

const char* rb_class2name(VALUE module) {
  return RSTRING_PTR(truffle_invoke((void *)module, "name"));
}

VALUE rb_class_real(VALUE ruby_class) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_class_real", ruby_class);
}

VALUE rb_class_superclass(VALUE ruby_class) {
  return (VALUE) truffle_invoke((void *)ruby_class, "superclass");
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
  return (VALUE) truffle_invoke((void *)ruby_class, "allocate");
}

VALUE rb_class_path(VALUE ruby_class) {
  rb_tr_error("rb_class_path not implemented");
}

VALUE rb_path2class(const char *string) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_path_to_class", rb_str_new_cstr(string));
}

VALUE rb_path_to_class(VALUE pathname) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_path_to_class", pathname);
}

VALUE rb_class_name(VALUE klass) {
  return (VALUE) truffle_invoke((void *)klass, "name");
}

VALUE rb_class_new(VALUE super) {
  return truffle_invoke(rb_cClass, "new", super, Qfalse);
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
  rb_tr_error("rb_call_super not implemented");
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
  return (VALUE) truffle_invoke((void *)module, "const_get", name);
}

VALUE rb_const_get_at(VALUE module, ID name) {
  return (VALUE) truffle_invoke((void *)module, "const_get", name, Qfalse);
}

VALUE rb_const_get_from(VALUE module, ID name) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_const_get_from", module, name);
}

VALUE rb_const_set(VALUE module, ID name, VALUE value) {
  return (VALUE) truffle_invoke((void *)module, "const_set", name, value);
}

VALUE rb_define_const(VALUE module, const char *name, VALUE value) {
  return rb_const_set(module, rb_str_new_cstr(name), value);
}

void rb_define_global_const(const char *name, VALUE value) {
  rb_define_const(rb_cObject, name, value);
}

// Global variables

void rb_define_hooked_variable(
    const char *name,
    VALUE *var,
    VALUE (*getter)(ANYARGS),
    void  (*setter)(ANYARGS)) {
  rb_tr_error("rb_define_hooked_variable not implemented");
}

void rb_define_readonly_variable(const char *name, const VALUE *var) {
  rb_tr_error("rb_define_readonly_variable not implemented");
}

void rb_define_variable(const char *name, VALUE *var) {
  rb_tr_error("rb_define_variable not implemented");
}

VALUE rb_f_global_variables(void) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_f_global_variables");
}

VALUE rb_gv_set(const char *name, VALUE val) {
  rb_tr_error("rb_gv_set not implemented");
}

VALUE rb_gv_get(const char *name) {
  rb_tr_error("rb_gv_get not implemented");
}

VALUE rb_lastline_get(void) {
  rb_tr_error("rb_lastline_get not implemented");
}

void rb_lastline_set(VALUE val) {
  rb_tr_error("rb_lastline_set not implemented");
}

// Raising exceptions

VALUE rb_exc_new(VALUE etype, const char *ptr, long len) {
  return (VALUE) truffle_invoke((void *)etype, "new", rb_str_new(ptr, len));
}

VALUE rb_exc_new_cstr(VALUE exception_class, const char *message) {
  return (VALUE) truffle_invoke((void *)exception_class, "new", rb_str_new_cstr(message));
}

VALUE rb_exc_new_str(VALUE exception_class, VALUE message) {
  return (VALUE) truffle_invoke((void *)exception_class, "new", message);
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

void rb_syserr_fail(int errno, const char *message) {
  truffle_invoke(RUBY_CEXT, "rb_syserr_fail", errno, message == NULL ? Qnil : rb_str_new_cstr(message));
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
  truffle_invoke((void *)module, "include", to_include);
}

void rb_define_method(VALUE module, const char *name, void *function, int argc) {
  truffle_invoke(RUBY_CEXT, "rb_define_method", module, rb_str_new_cstr(name), truffle_address_to_function(function), argc);
}

void rb_define_private_method(VALUE module, const char *name, void *function, int argc) {
  truffle_invoke(RUBY_CEXT, "rb_define_private_method", module, rb_str_new_cstr(name), truffle_address_to_function(function), argc);
}

void rb_define_protected_method(VALUE module, const char *name, void *function, int argc) {
  truffle_invoke(RUBY_CEXT, "rb_define_protected_method", module, rb_str_new_cstr(name), truffle_address_to_function(function), argc);
}

void rb_define_module_function(VALUE module, const char *name, void *function, int argc) {
  truffle_invoke(RUBY_CEXT, "rb_define_module_function", module, rb_str_new_cstr(name), truffle_address_to_function(function), argc);
}

void rb_define_global_function(const char *name, void *function, int argc) {
  rb_define_module_function(rb_mKernel, name, function, argc);
}

void rb_define_singleton_method(VALUE object, const char *name, void *function, int argc) {
  truffle_invoke(RUBY_CEXT, "rb_define_singleton_method", object, rb_str_new_cstr(name), truffle_address_to_function(function), argc);
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
  return (VALUE) truffle_invoke((void *)obj, "method", rb_intern_str(vid));
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
  return (VALUE) truffle_invoke((void *)rat, "numerator");
}

VALUE rb_rational_den(VALUE rat) {
  return (VALUE) truffle_invoke((void *)rat, "denominator");
}

VALUE rb_flt_rationalize_with_prec(VALUE value, VALUE precision) {
  return (VALUE) truffle_invoke((void *)value, "rationalize", precision);
}

VALUE rb_flt_rationalize(VALUE value) {
  return (VALUE) truffle_invoke((void *)value, "rationalize");
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
  VALUE utc = RBOOL(offset == INT_MAX-1);
  VALUE local = RBOOL(offset == INT_MAX);
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_time_timespec_new", ts->tv_sec, ts->tv_nsec, offset, utc, local);
}

void rb_timespec_now(struct timespec *ts) {
  struct timeval tv = rb_time_timeval((VALUE) truffle_invoke(rb_cTime, "now"));
  ts->tv_sec = tv.tv_sec;
  ts->tv_nsec = tv.tv_usec * 1000;
}

// Regexp

VALUE rb_backref_get(void) {
  rb_tr_error("rb_backref_get not implemented");
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
  rb_tr_error("rb_reg_match not implemented");
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

int rb_nativethread_lock_initialize(rb_nativethread_lock_t *lock) {
  *lock = truffle_invoke(RUBY_CEXT, "rb_nativethread_lock_initialize");
  return 0;
}

int rb_nativethread_lock_destroy(rb_nativethread_lock_t *lock) {
  *lock = NULL;
  return 0;
}

int rb_nativethread_lock_lock(rb_nativethread_lock_t *lock) {
  truffle_invoke((void *)lock, "lock");
  return 0;
}

int rb_nativethread_lock_unlock(rb_nativethread_lock_t *lock) {
  truffle_invoke((void *)lock, "unlock");
  return 0;
}

// IO

void rb_io_check_writable(rb_io_t *io) {
  // TODO
}

void rb_io_check_readable(rb_io_t *io) {
  // TODO
}

int rb_cloexec_dup(int oldfd) {
  rb_tr_error("rb_cloexec_dup not implemented");
}

void rb_fd_fix_cloexec(int fd) {
  rb_tr_error("rb_fd_fix_cloexec not implemented");
}

int rb_tr_io_handle(VALUE io) {
  return truffle_invoke_i(RUBY_CEXT, "rb_tr_io_handle", io);
}

int rb_io_wait_readable(int fd) {
  rb_tr_error("rb_io_wait_readable not implemented");
}

int rb_io_wait_writable(int fd) {
  rb_tr_error("rb_io_wait_writable not implemented");
}

void rb_thread_wait_fd(int fd) {
  rb_tr_error("rb_thread_wait_fd not implemented");
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
  rb_tr_error("rb_io_check_closed not implemented");
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
  rb_tr_error("rb_io_binmode not implemented");
}

int rb_thread_fd_writable(int fd) {
  rb_tr_error("rb_thread_fd_writable not implemented");
}

int rb_cloexec_open(const char *pathname, int flags, mode_t mode) {
  rb_tr_error("rb_cloexec_open not implemented");
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

// Data

VALUE rb_data_object_wrap(VALUE klass, void *datap, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_data_object_wrap", klass == NULL ? rb_cObject : klass, datap, dmark, dfree);
}

struct RData *rb_tr_adapt_rdata(VALUE value) {
  return (struct RData *)truffle_invoke(RUBY_CEXT, "rb_tr_adapt_rdata", value);
}

// Typed data

struct RTypedData *rb_tr_adapt_rtypeddata(VALUE value) {
  return (struct RTypedData *)truffle_invoke(RUBY_CEXT, "rb_tr_adapt_rtypeddata", value);
}

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
  if ((uintptr_t) rb_iv_get(value, "@data_type") != (uintptr_t) data_type) {
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

void *rb_tr_to_native_handle(VALUE managed) {
  return (void *)truffle_invoke_l(RUBY_CEXT, "rb_tr_to_native_handle", managed);
}

VALUE rb_tr_from_native_handle(void *native) {
  return (VALUE) truffle_invoke(RUBY_CEXT, "rb_tr_from_native_handle", (long) native);
}
