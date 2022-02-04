/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include <truffleruby-impl.h>
#include <internal/gc.h>
#include <internal.h>
#include <internal/imemo.h>

// Memory-related function, *alloc*, *free*, rb_mem*

void ruby_malloc_size_overflow(size_t count, size_t elsize) {
  rb_raise(rb_eArgError,
     "malloc: possible integer overflow (%"PRIdSIZE"*%"PRIdSIZE")",
     count, elsize);
}

size_t xmalloc2_size(const size_t count, const size_t elsize) {
  size_t ret;
  if (rb_mul_size_overflow(count, elsize, SSIZE_MAX, &ret)) {
    ruby_malloc_size_overflow(count, elsize);
  }
  return ret;
}

void *ruby_xmalloc(size_t size) {
  return malloc(size);
}

void *ruby_xmalloc2(size_t n, size_t size) {
  size_t total_size = xmalloc2_size(n, size);
  if (total_size == 0) {
    total_size = 1;
  }
  return malloc(total_size);
}

void* rb_xmalloc_mul_add(size_t x, size_t y, size_t z) {
  return ruby_xmalloc(x * y + z);
}

void *ruby_xcalloc(size_t n, size_t size) {
  return calloc(n, size);
}

void *ruby_xrealloc(void *ptr, size_t new_size) {
  return realloc(ptr, new_size);
}

void *ruby_xrealloc2(void *ptr, size_t n, size_t size) {
  size_t len = size * n;
  if (n != 0 && size != len / n) {
    rb_raise(rb_eArgError, "realloc: possible integer overflow");
  }
  return realloc(ptr, len);
}

void ruby_xfree(void *address) {
  free(address);
}

void *rb_alloc_tmp_buffer(volatile VALUE *store, long len) {
  if (len == 0) {
    len = 1;
  }
  void *ptr = malloc(len);
  *((void**)store) = ptr;
  return ptr;
}

void *rb_alloc_tmp_buffer_with_count(volatile VALUE *store, size_t size, size_t cnt) {
  return rb_alloc_tmp_buffer(store, size);
}

void rb_free_tmp_buffer(volatile VALUE *store) {
  free(*((void**)store));
}

void rb_mem_clear(VALUE *mem, long n) {
  for (int i = 0; i < n; i++) {
    mem[i] = Qnil;
  }
}

VALUE rb_imemo_tmpbuf_auto_free_pointer(void) {
  return RUBY_CEXT_INVOKE("rb_imemo_tmpbuf_auto_free_pointer");
}

void* rb_imemo_tmpbuf_set_ptr(VALUE imemo, void *ptr) {
  polyglot_invoke(RUBY_CEXT, "rb_imemo_tmpbuf_set_ptr", rb_tr_unwrap(imemo), ptr);
  return ptr;
}
