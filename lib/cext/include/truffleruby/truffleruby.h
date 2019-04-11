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
bool rb_tr_hidden_p(VALUE value);


// Managed Structs

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

// Utilities

#define rb_warn(FORMAT, ...) do { \
if (polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "warn?"))) { \
  RUBY_INVOKE(rb_mKernel, "warn", rb_sprintf(FORMAT, ##__VA_ARGS__));   \
} \
} while (0);

#define rb_warning(FORMAT, ...) do { \
if (polyglot_as_boolean(polyglot_invoke(RUBY_CEXT, "warning?"))) { \
  RUBY_INVOKE(rb_mKernel, "warn", rb_sprintf(FORMAT, ##__VA_ARGS__)); \
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

// Invoking ruby methods.

// These macros implement ways to call the methods on Truffle::CExt
// (RUBY_CEXT_INVOKE) and other ruby objects (RUBY_INVOKE) and handle
// all the unwrapping of arguments. They also come in _NO_WRAP
// variants which will not attempt to wrap the result. This is
// important if it is not an actual ruby object being returned as an
// error will be raised when attempting to wrap such objects.

// Internal macros for the implementation
#define RUBY_INVOKE_IMPL_0(recv, name) polyglot_invoke(recv, name)
#define RUBY_INVOKE_IMPL_1(recv, name, V1) polyglot_invoke(recv, name, rb_tr_unwrap(V1))
#define RUBY_INVOKE_IMPL_2(recv, name, V1, V2) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2))
#define RUBY_INVOKE_IMPL_3(recv, name, V1, V2, V3) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3))
#define RUBY_INVOKE_IMPL_4(recv, name, V1, V2, V3, V4) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4))
#define RUBY_INVOKE_IMPL_5(recv, name, V1, V2, V3, V4, V5) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4), rb_tr_unwrap(V5))
#define RUBY_INVOKE_IMPL_6(recv, name, V1, V2, V3, V4, V5, V6) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4), rb_tr_unwrap(V5), rb_tr_unwrap(V6))
#define RUBY_INVOKE_IMPL_7(recv, name, V1, V2, V3, V4, V5, V6, V7) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4), rb_tr_unwrap(V5), rb_tr_unwrap(V6), rb_tr_unwrap(V7))
#define RUBY_INVOKE_IMPL_8(recv, name, V1, V2, V3, V4, V5, V6, V7, V8) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4), rb_tr_unwrap(V5), rb_tr_unwrap(V6), rb_tr_unwrap(V7), rb_tr_unwrap(V8))
#define RUBY_INVOKE_IMPL_9(recv, name, V1, V2, V3, V4, V5, V6, V7, V8, V9) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4), rb_tr_unwrap(V5), rb_tr_unwrap(V6), rb_tr_unwrap(V7), rb_tr_unwrap(V8), rb_tr_unwrap(V9))
#define RUBY_INVOKE_IMPL_10(recv, name, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10) polyglot_invoke(recv, name, rb_tr_unwrap(V1), rb_tr_unwrap(V2), rb_tr_unwrap(V3), rb_tr_unwrap(V4), rb_tr_unwrap(V5), rb_tr_unwrap(V6), rb_tr_unwrap(V7), rb_tr_unwrap(V8), rb_tr_unwrap(V9), rb_tr_unwrap(V10))
#define INVOKE_IMPL(RECV, MESG, _1, _2, _3, _4, _5, _6, _7, _8, _9, _10, NAME, ...) NAME
#define RUBY_INVOKE_IMPL_NO_WRAP(RECV, NAME, ...) INVOKE_IMPL(RECV, NAME, ##__VA_ARGS__, RUBY_INVOKE_IMPL_10, RUBY_INVOKE_IMPL_9, RUBY_INVOKE_IMPL_8, RUBY_INVOKE_IMPL_7, RUBY_INVOKE_IMPL_6, RUBY_INVOKE_IMPL_5, RUBY_INVOKE_IMPL_4, RUBY_INVOKE_IMPL_3, RUBY_INVOKE_IMPL_2, RUBY_INVOKE_IMPL_1, RUBY_INVOKE_IMPL_0)(RECV, NAME, ##__VA_ARGS__)
#define RUBY_INVOKE_IMPL(RECV, NAME, ...) rb_tr_wrap(RUBY_INVOKE_IMPL_NO_WRAP(RECV, NAME, ##__VA_ARGS__))

// Public macros used in this header and ruby.c
#define RUBY_INVOKE(RECV, NAME, ...) RUBY_INVOKE_IMPL(rb_tr_unwrap(RECV), NAME, ##__VA_ARGS__)
#define RUBY_INVOKE_NO_WRAP(RECV, NAME, ...) RUBY_INVOKE_IMPL_NO_WRAP(rb_tr_unwrap(RECV), NAME, ##__VA_ARGS__)

#define RUBY_CEXT_INVOKE(NAME, ...) RUBY_INVOKE_IMPL(RUBY_CEXT, NAME, ##__VA_ARGS__)
#define RUBY_CEXT_INVOKE_NO_WRAP(NAME, ...) RUBY_INVOKE_IMPL_NO_WRAP(RUBY_CEXT, NAME, ##__VA_ARGS__)

// Calls

// We use this pair of macros because ##__VA_ARGS__ args will not
// have macro substitution done on them at the right point in
// preprocessing and will prevent rb_funcall(..., rb_funcall(...))
// from being expanded correctly.
#define rb_tr_funcall(object, method, n,...) RUBY_CEXT_INVOKE("rb_funcall", object, method, INT2FIX(n), ##__VA_ARGS__)
#define rb_funcall(object, method, ...) rb_tr_funcall(object, method, __VA_ARGS__)

// Exceptions

#define rb_raise(EXCEPTION, FORMAT, ...) \
  rb_exc_raise(rb_exc_new_str(EXCEPTION, rb_sprintf(FORMAT, ##__VA_ARGS__)))

// Additional non-standard
VALUE rb_java_class_of(VALUE val);
VALUE rb_java_to_string(VALUE val);
VALUE rb_equal_opt(VALUE a, VALUE b);
int rb_encdb_alias(const char *alias, const char *orig);
VALUE rb_ivar_lookup(VALUE object, const char *name, VALUE default_value);

// Inline implementations

MUST_INLINE int rb_nativethread_lock_initialize(rb_nativethread_lock_t *lock) {
  *lock = RUBY_CEXT_INVOKE("rb_nativethread_lock_initialize");
  return 0;
}

MUST_INLINE int rb_nativethread_lock_destroy(rb_nativethread_lock_t *lock) {
  *lock = RUBY_CEXT_INVOKE("rb_nativethread_lock_destroy", *lock);
  return 0;
}

MUST_INLINE int rb_nativethread_lock_lock(rb_nativethread_lock_t *lock) {
  RUBY_INVOKE_NO_WRAP(*lock, "lock");
  return 0;
}

MUST_INLINE int rb_nativethread_lock_unlock(rb_nativethread_lock_t *lock) {
  RUBY_INVOKE_NO_WRAP(*lock, "unlock");
  return 0;
}

MUST_INLINE int rb_range_values(VALUE range, VALUE *begp, VALUE *endp, int *exclp) {
  if (!rb_obj_is_kind_of(range, rb_cRange)) {
    if (!RTEST(RUBY_INVOKE(range, "respond_to?", rb_intern("begin")))) return Qfalse_int_const;
    if (!RTEST(RUBY_INVOKE(range, "respond_to?", rb_intern("end")))) return Qfalse_int_const;
  }

  *begp = RUBY_INVOKE(range, "begin");
  *endp = RUBY_INVOKE(range, "end");
  *exclp = (int) RTEST(RUBY_INVOKE(range, "exclude_end?"));
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

  RUBY_CEXT_INVOKE("rb_string_value_cstr_check", string);

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

  const int n_mand = pre + post;
  const int n_opt = optional;

  // Read arguments

  int argn = 0;
  int valuen = 1; // We've numbered the v parameters from 1
  bool taken_rest = false;
  bool taken_block = false;
  bool taken_kwargs = false;
  bool erased_kwargs = false;
  bool found_kwargs = false;

  if (kwargs && (n_mand < argc)) {
    VALUE last = argv[argc - 1];

    if (NIL_P(last)) {
      /* nil is taken as an empty option hash only if it is not
         ambiguous; i.e. '*' is not specified and arguments are
         given more than sufficient */
      if (rest || argc <= n_mand + n_opt) {
        kwargs = false;
        erased_kwargs = true;
      }
    } else {
      if (!polyglot_as_boolean(RUBY_CEXT_INVOKE_NO_WRAP("test_kwargs", argv[argc - 1], Qfalse))) {
        kwargs = false;
        erased_kwargs = true;
      }
    }
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
        RUBY_CEXT_INVOKE_NO_WRAP("test_kwargs", arg, Qtrue);
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
    case 1: if (v1 != NULL) { *v1 = arg; } break;
    case 2: if (v2 != NULL) { *v2 = arg; } break;
    case 3: if (v3 != NULL) { *v3 = arg; } break;
    case 4: if (v4 != NULL) { *v4 = arg; } break;
    case 5: if (v5 != NULL) { *v5 = arg; } break;
    case 6: if (v6 != NULL) { *v6 = arg; } break;
    case 7: if (v7 != NULL) { *v7 = arg; } break;
    case 8: if (v8 != NULL) { *v8 = arg; } break;
    case 9: if (v9 != NULL) { *v9 = arg; } break;
    case 10: if (v10 != NULL) { *v10 = arg; } break;
    }

    valuen++;
  }

  if (found_kwargs) {
    argc = argc - 1;
  }

  if (argn < argc) {
	rb_error_arity(argc, n_mand, rest ? UNLIMITED_ARGUMENTS : n_mand + n_opt);
  }

  return argc;
}

#if defined(__cplusplus)
}
#endif

#endif
