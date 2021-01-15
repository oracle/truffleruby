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

#if defined(__linux__) && !defined(__GLIBC__)
#define __MUSLC__
#endif

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

#ifdef __MUSLC__
int rb_tr_vasprintf(char **output, const char *format, va_list args_in);

#else  // __MUSLC__
#include <printf.h>

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

#else  // __APPLE__
static int rb_tr_fprintf_value_arginfo(const struct printf_info *info,
                                       size_t n,
                                       int *argtypes, int *argsize) {
  if (n > 0) {
    *argtypes = PA_POINTER;
    *argsize = sizeof(VALUE);
  }
  return 1;
}
#endif  // __APPLE__

static int rb_tr_fprintf_value(FILE *stream,
                               const struct printf_info *info,
                               const void *const *args) {
  char *cstr = rb_value_to_str((const VALUE *) args[0], info->showsign);
  return fprintf(stream, "%s", cstr);
}
#endif  // __MUSLC__

VALUE rb_enc_vsprintf(rb_encoding *enc, const char *format, va_list args) {
  char *buffer;
  #ifdef __APPLE__
  if (vasxprintf(&buffer, printf_domain, NULL, format, args) < 0) {
  #elif defined(__MUSLC__)
  if (rb_tr_vasprintf(&buffer, format, args) < 0) {
  #else
  if (vasprintf(&buffer, format, args) < 0) {
  #endif
    rb_tr_error("vasprintf error");
  }
  VALUE string = rb_enc_str_new_cstr(buffer, enc);
  free(buffer);
  return string;
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

void rb_tr_init_printf(void) {
  #ifdef __APPLE__
  printf_domain = new_printf_domain();
  register_printf_domain_function(printf_domain, 'P', rb_tr_fprintf_value, rb_tr_fprintf_value_arginfo, NULL);
  #elif defined(__MUSLC__)
  // no op in musl
  #else
  register_printf_specifier('P', rb_tr_fprintf_value, rb_tr_fprintf_value_arginfo);
  #endif
}
