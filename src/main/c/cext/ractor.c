/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
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

// Simplified to main Ractor only

VALUE rb_ractor_stdin(void) {
  return rb_stdin;
}

VALUE rb_ractor_stdout(void) {
  return rb_stdout;
}

VALUE rb_ractor_stderr(void) {
  return rb_stderr;
}

// Ractor local storage, simplified to main Ractor only

struct rb_ractor_local_key_struct {
    const struct rb_ractor_local_storage_type *type;
    void *main_cache;
};

static const struct rb_ractor_local_storage_type ractor_local_storage_type_null = {
  NULL,
  NULL,
};

rb_ractor_local_key_t rb_ractor_local_storage_ptr_newkey(const struct rb_ractor_local_storage_type *type) {
  rb_ractor_local_key_t key = ALLOC(struct rb_ractor_local_key_struct);
  key->type = type ? type : &ractor_local_storage_type_null;
  key->main_cache = (void *)Qundef;
  return key;
}

void *rb_ractor_local_storage_ptr(rb_ractor_local_key_t key) {
  if (key->main_cache != (void*)Qundef) {
    return key->main_cache;
  } else {
    return NULL;
  }
}

void rb_ractor_local_storage_ptr_set(rb_ractor_local_key_t key, void *ptr) {
  key->main_cache = ptr;
}
