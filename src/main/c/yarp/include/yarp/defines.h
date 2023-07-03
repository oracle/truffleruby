#ifndef YARP_DEFINES_H
#define YARP_DEFINES_H

// This file should be included first by any *.h or *.c in YARP

#include "yarp/config.h"

#include <ctype.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>

// YP_EXPORTED_FUNCTION
#if defined(YP_STATIC)
#   define YP_EXPORTED_FUNCTION
#elif defined(_WIN32)
#   define YP_EXPORTED_FUNCTION __declspec(dllexport) extern
#else
#   ifndef YP_EXPORTED_FUNCTION
#       ifndef RUBY_FUNC_EXPORTED
#           define YP_EXPORTED_FUNCTION __attribute__((__visibility__("default"))) extern
#       else
#           define YP_EXPORTED_FUNCTION RUBY_FUNC_EXPORTED
#       endif
#   endif
#endif

// YP_ATTRIBUTE_UNUSED
#if defined(__GNUC__)
#   define YP_ATTRIBUTE_UNUSED __attribute__((unused))
#else
#   define YP_ATTRIBUTE_UNUSED
#endif

// inline
#if defined(_MSC_VER) && !defined(inline)
#   define inline __inline
#endif

// strncasecmp
#if !defined(HAVE_STRNCASECMP) && !defined(strncasecmp)
    // In case strncasecmp isn't present on the system, we provide our own.
    int yp_strncasecmp(const char *string1, const char *string2, size_t length);
#   define strncasecmp yp_strncasecmp
#endif

// snprintf
#if !defined(HAVE_SNPRINTF) && !defined(snprintf)
    // In case snprintf isn't present on the system, we provide our own that
    // simply forwards to the less-safe sprintf.
    int yp_snprintf(char *dest, YP_ATTRIBUTE_UNUSED size_t size, const char *format, ...);
#   define snprintf yp_snprintf
#endif

#endif
