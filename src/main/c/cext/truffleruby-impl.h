/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <ruby.h>
#include <ruby/encoding.h>

#include <stdlib.h>
#include <stdarg.h>
#include <stdbool.h>

#include <internal_all.h>

// Private helper macros

#define rb_boolean(c) ((c) ? Qtrue : Qfalse)

// Private functions

extern bool (*rb_tr_is_native_object)(VALUE value);

// Create a native MutableTruffleString from ptr and len without copying.
// The returned RubyString is only valid as long as ptr is valid (typically only as long as the caller is on the stack),
// so this must be only used as an argument to an internal Truffle::CExt method which does not return or store
// the RubyString but only run some operation on it.
VALUE rb_tr_temporary_native_string(const char *ptr, long len, rb_encoding *enc);
