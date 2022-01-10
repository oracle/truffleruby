/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
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

extern bool (*rb_tr_is_native_object)(VALUE value);
