/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>

#if defined(__linux__) && !defined(__GLIBC__)
#include <ffi.h>
// musl won't allow us to register %P as custom printf specifier,
// so we roll out our own `vasprintf` implementation which scans the format string,
// handles %Ps inside it, and delegates the rest of work to system function.
// Because the number and types of arguments are not known at compile time,
// we construct the call frame at run time using libffi.

char* rb_value_to_str(const VALUE *arg, int showsign);

enum {
   PARSE_CHARS,
   PARSE_PERCENT,
   PARSE_FORMAT
};

enum {
    FLAG_NONE,
    FLAG_L    = 1,
    FLAG_PLUS = 2
};

#define VA_ARG(arg_dst, type_dst, src, type, ffi_type) { \
    *(arg_dst) = alloca(sizeof(type)); \
    *((type*) *(arg_dst)++) = va_arg((src), type); \
    *((type_dst)++) = &(ffi_type); \
}

static int call_asprintf(char **output, int nargs, void **args, ffi_type **arg_types) {
  int result = -1;
  ffi_cif cif;
  if (ffi_prep_cif(&cif, FFI_DEFAULT_ABI, nargs, &ffi_type_sint, arg_types) == FFI_OK) {
    ffi_call(&cif, FFI_FN(asprintf), &result, args);
  }
  return result;
}

int rb_tr_vasprintf(char **output, const char *format, va_list args_in) {
    int fmt_len = strlen(format);
    char *fmt_out = (char*) malloc(fmt_len + 1);
    void **args_out = (void**) malloc(sizeof(void*) * fmt_len / 2 + 2);
    ffi_type **types_out = (ffi_type**) malloc(sizeof(ffi_type*) * fmt_len / 2 + 2);

    void **argp = args_out;
    ffi_type **argt = types_out;
    *argp++ = &output;
    *argt++ = &ffi_type_pointer;
    *argp++ = &fmt_out;
    *argt++ = &ffi_type_pointer;

    char *p = fmt_out;
    int state = PARSE_CHARS;
    int flags = FLAG_NONE;
    for (; *format != '\0'; p++, format++) {
        *p = *format;
        if (state == PARSE_CHARS) {
            if (*format == '%')
                state = PARSE_PERCENT;
            continue;
        }
        switch (*format) {
            case '%':
                state = state == PARSE_PERCENT ? PARSE_CHARS : PARSE_PERCENT;
                continue;
            case 'l':
                flags |= FLAG_L;
                continue;
            case '+':
                flags |= FLAG_PLUS;
                continue;
            default:
                // assume some other modifier
                if (state == PARSE_PERCENT)
                    state = PARSE_FORMAT;
                continue;
            case 'm':
                // no argument is consumed
                break;
            case 'c': case 'C':
                VA_ARG(argp, argt, args_in, int, ffi_type_sint)
                break;
            case 'd': case 'i': case 'o': case 'u': case 'x': case 'X':
                if (flags & FLAG_L)
                    VA_ARG(argp, argt, args_in, long, ffi_type_slong)
                else
                    VA_ARG(argp, argt, args_in, int, ffi_type_sint)
                break;
            case 'a': case 'e': case 'f': case 'g':
            case 'A': case 'E': case 'F': case 'G':
                VA_ARG(argp, argt, args_in, double, ffi_type_double)
                break;
            case 'n': case 'p': case 's': case 'S':
                VA_ARG(argp, argt, args_in, void*, ffi_type_pointer)
                break;
            case 'P':
                *p = 's';
                VALUE obj = va_arg(args_in, VALUE);
                *argp = alloca(sizeof(char*));
                *((char**) *argp++) = rb_value_to_str(&obj, flags & FLAG_PLUS);
                *argt++ = &ffi_type_pointer;
                break;
        }
        state = PARSE_CHARS;
        flags = FLAG_NONE;
    }
    *p = '\0';
    int result = call_asprintf(output, (int)(argp-args_out), args_out, types_out);

    free(fmt_out);
    free(args_out);
    free(types_out);
    return result;
}

#endif  // defined(__linux__) && !defined(__GLIBC__)
