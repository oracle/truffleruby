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
#include <ruby/ractor.h>

// Ractor, rb_ractor_*

// Because of a mix of #if HAVE_RB_EXT_RACTOR_SAFE and #ifdef HAVE_RB_EXT_RACTOR_SAFE,
// we cannot just leave HAVE_RB_EXT_RACTOR_SAFE undefined or defined to 0 without getting
// -Wundef warnings & errors. So we let it defined to 1 but rb_ext_ractor_safe() has no effect.
// Also rb_ext_ractor_safe() is sometimes called directly instead of RB_EXT_RACTOR_SAFE().
void rb_ext_ractor_safe(bool flag) {
  // No-op
}
