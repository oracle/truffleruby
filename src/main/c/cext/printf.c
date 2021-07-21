/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>
#include <ruby/encoding.h>

// *printf* functions
char* rb_value_to_str(const VALUE *arg, int showsign) {
  char *cstr = NULL;
  VALUE v = *arg;

  if (showsign) {
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
      cstr = RSTRING_PTR(str);
    }
  } else {
    VALUE str = rb_obj_as_string(v);
    cstr = RSTRING_PTR(str);
  }
  return cstr;
}

VALUE rb_tr_get_sprintf_args(va_list args, VALUE types);

static VALUE rb_tr_vsprintf_new_cstr(char *cstr) {
  if (cstr == NULL) {
    return rb_str_new_cstr("");
  } else {
    return rb_str_new_cstr(cstr);
  }
}

VALUE rb_enc_vsprintf(rb_encoding *enc, const char *format, va_list args) {
  VALUE rubyFormat = rb_str_new_cstr(format);
  VALUE types = RUBY_CEXT_INVOKE("rb_tr_sprintf_types", rubyFormat);
  VALUE rubyArgs = rb_tr_get_sprintf_args(args, types);

  return rb_str_conv_enc(rb_tr_wrap(
                    polyglot_invoke(
                                    RUBY_CEXT,
                                    "rb_tr_sprintf",
                                    rb_tr_unwrap(rubyFormat),
                                    rb_tr_vsprintf_new_cstr,
                                    rb_tr_unwrap(rubyArgs))), NULL, enc);
}

VALUE rb_enc_sprintf(rb_encoding *enc, const char *format, ...) {
  VALUE result;
  va_list ap;

  va_start(ap, format);
  result = rb_enc_vsprintf(enc, format, ap);
  va_end(ap);

  return result;
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

VALUE rb_f_sprintf(int argc, const VALUE *argv) {
  return RUBY_CEXT_INVOKE("rb_f_sprintf", rb_ary_new4(argc, argv));
}

#undef vsnprintf
int ruby_vsnprintf(char *str, size_t n, char const *fmt, va_list ap) {
  return vsnprintf(str, n, fmt, ap);
}

enum printf_arg_types {
  TYPE_UNKNOWN,
  TYPE_CHAR,
  TYPE_SHORT,
  TYPE_INT,
  TYPE_LONG,
  TYPE_LONGLONG,
  TYPE_DOUBLE,
  TYPE_LONGDOUBLE,
  TYPE_SIZE_T,
  TYPE_INTMAX_T,
  TYPE_PTRDIFF_T,
  TYPE_STRING,
  TYPE_POINTER,
  TYPE_SCHAR = 0x11,
  TYPE_SSHORT,
  TYPE_SINT,
  TYPE_SLONG,
  TYPE_SLONGLONG,
};

VALUE rb_tr_get_sprintf_args(va_list args, VALUE types) {
  VALUE ary = rb_ary_new();

  long len = RARRAY_LEN(types);
  int pos = 0;
  while (pos < len) {
    enum printf_arg_types type = FIX2INT(RARRAY_AREF(types, pos++));
    VALUE val;
    switch(type) {
    case TYPE_CHAR:
    case TYPE_SHORT:
    case TYPE_INT:
      val = UINT2NUM(va_arg(args, unsigned int));
      break;
    case TYPE_LONG:
      val = ULONG2NUM(va_arg(args, unsigned long));
      break;
    case TYPE_LONGLONG:
      val = ULL2NUM(va_arg(args, unsigned long long));
      break;
    case TYPE_DOUBLE:
      val = DBL2NUM(va_arg(args, double));
      break;
    case TYPE_LONGDOUBLE:
      val = DBL2NUM(va_arg(args, long double));
      break;
    case TYPE_SIZE_T:
      val = ULONG2NUM(va_arg(args, size_t));
      break;
    case TYPE_INTMAX_T:
      val = ULONG2NUM(va_arg(args, intmax_t));
      break;
    case TYPE_PTRDIFF_T:
      val = LONG2NUM(va_arg(args, ptrdiff_t));
      break;
    case TYPE_STRING:
      val = rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_tr_pointer", va_arg(args, char *)));
      break;
    case TYPE_POINTER:
      val = rb_tr_wrap(polyglot_invoke(RUBY_CEXT, "rb_tr_pointer", va_arg(args, void *)));
      break;
    case TYPE_SCHAR:
    case TYPE_SSHORT:
    case TYPE_SINT:
      val = INT2NUM(va_arg(args, int));
      break;
    case TYPE_SLONG:
      {
        long arg = va_arg(args, long);
        if (polyglot_is_value(arg)) {
          arg = rb_tr_force_native(arg);
        }
        val = LONG2NUM(arg);
        break;
      }
    case TYPE_SLONGLONG:
      val = LL2NUM(va_arg(args, long long));
      break;
    default:
      {
        char *err_str;
        if (asprintf(&err_str, "unknown rb_sprintf arg type %d", type) > 0 ) {
          rb_tr_error(err_str);
          free(err_str);
        }
      }
    }
    rb_ary_push(ary, val);
  }
  return ary;
}

VALUE rb_str_vcatf(VALUE str, const char *fmt, va_list args) {
  StringValue(str);
  VALUE result = rb_vsprintf(fmt, args);
  rb_str_concat(str, result);
  return str;
}

VALUE rb_str_catf(VALUE str, const char *format, ...) {
  va_list ap;
  va_start(ap, format);
  str = rb_str_vcatf(str, format, ap);
  va_end(ap);
  return str;
}
