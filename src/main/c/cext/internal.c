/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * This file contains code that is based on the Ruby API headers and implementation,
 * copyright (C) Yukihiro Matsumoto, licensed under the 2-clause BSD licence
 * as described in the file BSDL included with TruffleRuby.
 */

// Functions defined in MRI's internal.h, used by some MRI C-ext tests.
// These need to be defined for linking to succeed on macOS.

#include <ruby.h>

// Used in test/mri/tests/cext-c/hash/delete.c
VALUE rb_hash_delete_entry(VALUE hash, VALUE key) {
  rb_tr_error("rb_hash_delete_entry not implemented");
}

// Used in test/mri/tests/cext-c/integer/core_ext.c
VALUE rb_int_positive_pow(long x, unsigned long y) {
  rb_tr_error("rb_int_positive_pow not implemented");
}

// Used in test/mri/tests/cext-c/string/fstring.c
VALUE rb_fstring(VALUE str) {
  rb_tr_error("rb_fstring not implemented");
}

// Used in test/mri/tests/cext-c/string/normalize.c
VALUE rb_str_normalize_ospath(const char *ptr, long len) {
  rb_tr_error("rb_str_normalize_ospath not implemented");
}
