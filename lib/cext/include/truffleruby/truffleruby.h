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

#define rb_sprintf(format, ...) \
(VALUE) polyglot_invoke(RUBY_CEXT, "rb_sprintf", rb_str_new_cstr(format), ##__VA_ARGS__)

NORETURN(VALUE rb_f_notimplement(int args_count, const VALUE *args, VALUE object));

// Non-standard

NORETURN(void rb_tr_error(const char *message));
void rb_tr_log_warning(const char *message);
#define rb_tr_debug(args...) polyglot_invoke(RUBY_CEXT, "rb_tr_debug", args)
long rb_tr_obj_id(VALUE object);
void rb_tr_hidden_variable_set(VALUE object, const char *name, VALUE value);
VALUE rb_tr_hidden_variable_get(VALUE object, const char *name);
int rb_tr_obj_equal(VALUE first, VALUE second);
int rb_tr_flags(VALUE value);
void rb_tr_add_flags(VALUE value, int flags);

// Handles

void *rb_tr_handle_for_managed(VALUE managed);
void *rb_tr_handle_for_managed_leaking(VALUE managed);
void *rb_tr_handle_if_managed(VALUE managed);
void *rb_tr_handle_if_managed_leaking(VALUE managed);
VALUE rb_tr_managed_from_handle_or_null(void *handle);
VALUE rb_tr_managed_from_handle(void *handle);
VALUE rb_tr_managed_from_handle_release(void *handle);
VALUE rb_tr_managed_if_handle(void *handle);
void rb_tr_release_handle(void *handle);
void rb_tr_release_if_handle(void *handle);

// Managed Strucs

void* rb_tr_new_managed_struct_internal(void *type);
#define rb_tr_new_managed_struct(type) rb_tr_new_managed_struct_internal(polyglot_##type##_typeid())
VALUE rb_data_object_alloc_managed(VALUE klass, size_t size, RUBY_DATA_FUNC dmark, RUBY_DATA_FUNC dfree, void *interoptypeid);
VALUE rb_data_typed_object_alloc_managed(VALUE ruby_class, size_t size, const rb_data_type_t *data_type, void *interoptypeid);

#define Data_Make_Managed_Struct0(result, klass, type, size, mark, free, sval) \
    VALUE result = rb_data_object_alloc_managed((klass), (size), \
                     (RUBY_DATA_FUNC)(mark), \
                     (RUBY_DATA_FUNC)(free), \
                     polyglot_##type##_typeid()); \
    (void)((sval) = (type *)DATA_PTR(result));

#define Data_Make_Managed_Struct(klass,type,mark,free,sval) ({\
    Data_Make_Managed_Struct0(data_struct_obj, klass, type, sizeof(type), mark, free, sval); \
    data_struct_obj; \
})

#define TypedData_Make_Managed_Struct0(result, klass, type, size, data_type, sval, interoptype) \
    VALUE result = rb_data_typed_object_alloc_managed(klass, size, data_type, polyglot_##interoptype##_typeid()); \
    (void)((sval) = (type *)DATA_PTR(result));

#define TypedData_Make_Managed_Struct(klass, type, data_type, sval, interoptype) ({\
    TypedData_Make_Managed_Struct0(data_struct_obj, klass, type, sizeof(type), data_type, sval, interoptype); \
    data_struct_obj; \
})

bool rb_tr_obj_taintable_p(VALUE object);
bool rb_tr_obj_tainted_p(VALUE object);
void rb_tr_obj_infect(VALUE a, VALUE b);

#define Qfalse_int_const 0
#define Qtrue_int_const 2
#define Qnil_int_const 4
int rb_tr_to_int_const(VALUE value);

int rb_tr_readable(int mode);
int rb_tr_writable(int mode);

typedef void *(*gvl_call)(void *);

// Exceptions

#define rb_raise(EXCEPTION, FORMAT, ...) \
rb_exc_raise(rb_exc_new_str(EXCEPTION, (VALUE) polyglot_invoke(RUBY_CEXT, "rb_sprintf", rb_str_new_cstr(FORMAT), ##__VA_ARGS__)))

// Utilities

#define rb_warn(FORMAT, ...) do { \
if (polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "warn?"))) { \
  polyglot_invoke(rb_mKernel, "warn", (VALUE) polyglot_invoke(rb_mKernel, "sprintf", rb_str_new_cstr(FORMAT), ##__VA_ARGS__)); \
} \
} while (0);

#define rb_warning(FORMAT, ...) do { \
if (polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "warning?"))) { \
  polyglot_invoke(rb_mKernel, "warn", (VALUE) polyglot_invoke(rb_mKernel, "sprintf", rb_str_new_cstr(FORMAT), ##__VA_ARGS__)); \
} \
} while (0);

MUST_INLINE int rb_tr_scan_args(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2, VALUE *v3, VALUE *v4, VALUE *v5, VALUE *v6, VALUE *v7, VALUE *v8, VALUE *v9, VALUE *v10);

#define rb_tr_scan_args_1(ARGC, ARGV, FORMAT, V1) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_tr_scan_args_2(ARGC, ARGV, FORMAT, V1, V2) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_tr_scan_args_3(ARGC, ARGV, FORMAT, V1, V2, V3) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, NULL, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_tr_scan_args_4(ARGC, ARGV, FORMAT, V1, V2, V3, V4) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, NULL, NULL, NULL, NULL, NULL, NULL)
#define rb_tr_scan_args_5(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, NULL, NULL, NULL, NULL, NULL)
#define rb_tr_scan_args_6(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, NULL, NULL, NULL, NULL)
#define rb_tr_scan_args_7(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, NULL, NULL, NULL)
#define rb_tr_scan_args_8(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, NULL, NULL)
#define rb_tr_scan_args_9(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9, NULL)
#define rb_tr_scan_args_10(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10) rb_tr_scan_args(ARGC, ARGV, FORMAT, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10)

#define SCAN_ARGS_IMPL(_1, _2, _3, _4, _5, _6, _7, _8, _9, _10, NAME, ...) NAME
#define rb_scan_args(ARGC, ARGV, FORMAT, ...) SCAN_ARGS_IMPL(__VA_ARGS__, rb_tr_scan_args_10, rb_tr_scan_args_9, rb_tr_scan_args_8, rb_tr_scan_args_7, rb_tr_scan_args_6, rb_tr_scan_args_5, rb_tr_scan_args_4, rb_tr_scan_args_3, rb_tr_scan_args_2, rb_tr_scan_args_1)(ARGC, ARGV, FORMAT, __VA_ARGS__)

// Calls

#define rb_funcall(object, ...) polyglot_invoke(RUBY_CEXT, "rb_funcall", (void *) object, __VA_ARGS__)

// Additional non-standard
VALUE rb_java_class_of(VALUE val);
VALUE rb_java_to_string(VALUE val);
VALUE rb_equal_opt(VALUE a, VALUE b);
int rb_encdb_alias(const char *alias, const char *orig);
VALUE rb_ivar_lookup(VALUE object, const char *name, VALUE default_value);

// Inline implementations

MUST_INLINE int rb_nativethread_lock_initialize(rb_nativethread_lock_t *lock) {
  *lock = polyglot_invoke(RUBY_CEXT, "rb_nativethread_lock_initialize");
  return 0;
}

MUST_INLINE int rb_nativethread_lock_destroy(rb_nativethread_lock_t *lock) {
  *lock = NULL;
  return 0;
}

MUST_INLINE int rb_nativethread_lock_lock(rb_nativethread_lock_t *lock) {
  polyglot_invoke(*lock, "lock");
  return 0;
}

MUST_INLINE int rb_nativethread_lock_unlock(rb_nativethread_lock_t *lock) {
  polyglot_invoke(*lock, "unlock");
  return 0;
}

MUST_INLINE int rb_range_values(VALUE range, VALUE *begp, VALUE *endp, int *exclp) {
  if (rb_obj_is_kind_of(range, rb_cRange)) {
    *begp = (VALUE) polyglot_invoke(range, "begin");
    *endp = (VALUE) polyglot_invoke(range, "end");
    *exclp = (int) polyglot_as_boolean(polyglot_invoke(range, "exclude_end?"));
  }
  else {
    if (!polyglot_as_boolean(polyglot_invoke(range, "respond_to?", rb_intern("begin")))) return Qfalse_int_const;
    if (!polyglot_as_boolean(polyglot_invoke(range, "respond_to?", rb_intern("end")))) return Qfalse_int_const;

    *begp = polyglot_invoke(range, "begin");
    *endp = polyglot_invoke(range, "end");
    *exclp = (int) RTEST(polyglot_invoke(range, "exclude_end?"));
  }
  return Qtrue_int_const;
}

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

  polyglot_invoke(RUBY_CEXT, "rb_string_value_cstr_check", string);

  return RSTRING_PTR(string);
}

MUST_INLINE int rb_tr_scan_args(int argc, VALUE *argv, const char *format, VALUE *v1, VALUE *v2, VALUE *v3, VALUE *v4, VALUE *v5, VALUE *v6, VALUE *v7, VALUE *v8, VALUE *v9, VALUE *v10) {
  // Parse the format string

  // TODO CS 7-Feb-17 maybe we could inline cache this part?

  const char *formatp = format;
  int pre = 0;
  int optional = 0;
  bool rest;
  int post = 0;
  bool kwargs;
  bool block;

  // TODO CS 27-Feb-17 can LLVM constant-fold through isdigit?

  if (isdigit(*formatp)) {
    pre = *formatp - '0';
    formatp++;

    if (isdigit(*formatp)) {
      optional = *formatp - '0';
      formatp++;
    }
  }

  if (*formatp == '*') {
    rest = true;
    formatp++;
  } else {
    rest = false;
  }

  if (isdigit(*formatp)) {
    post = *formatp - '0';
    formatp++;
  }

  if (*formatp == ':') {
    kwargs = true;
    formatp++;
  } else {
    kwargs = false;
  }

  if (*formatp == '&') {
    block = true;
    formatp++;
  } else {
    block = false;
  }

  if (*formatp != '\0') {
    rb_raise(rb_eArgError, "bad rb_scan_args format");
  }

  // Check we have enough arguments

  if (pre + post > argc) {
    rb_raise(rb_eArgError, "not enough arguments for required");
  }

  // Read arguments

  int argn = 0;
  int valuen = 1; // We've numbered the v parameters from 1
  bool taken_rest = false;
  bool taken_block = false;
  bool taken_kwargs = false;
  bool erased_kwargs = false;
  bool found_kwargs = false;

  if (rest && kwargs && !polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "test_kwargs", argv[argc - 1], Qfalse))) {
    kwargs = false;
    erased_kwargs = true;
  }

  int trailing = post;

  if (kwargs) {
    trailing++;
  }

  while (true) {
    // Get the next argument

    VALUE arg;

    if (pre > 0 || optional > 0) {
      if (argn - pre < argc - trailing) {
        arg = argv[argn];
        argn++;
      } else {
        arg = Qnil;
      }

      if (pre > 0) {
        pre--;
      } else {
        optional--;
      }
    } else if (rest && !taken_rest) {
      arg = rb_ary_new();
      while (argn < argc - trailing) {
        rb_ary_push(arg, argv[argn]);
        argn++;
      }
      taken_rest = true;
    } else if (post > 0) {
      arg = argv[argn];
      argn++;
      post--;
    } else if (kwargs && !taken_kwargs) {
       if (argn < argc) {
        arg = argv[argn];
        polyglot_invoke(RUBY_CEXT, "test_kwargs", arg, Qtrue);
        argn++;
        found_kwargs = true;
      } else {
        arg = Qnil;
      }
      taken_kwargs = true;
    } else if (erased_kwargs && !taken_kwargs) {
      arg = Qnil;
      taken_kwargs = true;
    } else if (block && !taken_block) {
      if (rb_block_given_p()) {
        arg = rb_block_proc();
      } else {
        arg = Qnil;
      }
      taken_block = true;
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

  if (found_kwargs) {
    return argc - 1;
  } else {
    return argc;
  }
}

#if defined(__cplusplus)
}
#endif

#endif
