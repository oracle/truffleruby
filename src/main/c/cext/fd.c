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
#include <fcntl.h>
#include <ruby/thread.h>

// For howmany()
#ifdef HAVE_SYS_PARAM_H
#include <sys/param.h>
#endif

// File descriptors, rb_fd_*

// Assumption for rb_fd_ implementations below
#if !(defined(NFDBITS) && defined(HAVE_RB_FD_INIT))
#error "expected NFDBITS and HAVE_RB_FD_INIT to be defined"
#endif

void rb_fd_init(rb_fdset_t *fds) {
  fds->maxfd = 0;
  fds->fdset = ALLOC(fd_set);
  FD_ZERO(fds->fdset);
}

void rb_fd_term(rb_fdset_t *fds) {
  if (fds->fdset) {
    xfree(fds->fdset);
  }
  fds->maxfd = 0;
  fds->fdset = NULL;
}

void rb_fd_zero(rb_fdset_t *fds) {
  if (fds->fdset) {
    MEMZERO(fds->fdset, fd_mask, howmany(fds->maxfd, NFDBITS));
  }
}

static void
rb_fd_resize(int n, rb_fdset_t *fds) {
  size_t m = howmany(n + 1, NFDBITS) * sizeof(fd_mask);
  size_t o = howmany(fds->maxfd, NFDBITS) * sizeof(fd_mask);

  if (m < sizeof(fd_set)) {
    m = sizeof(fd_set);
  }
  if (o < sizeof(fd_set)) {
    o = sizeof(fd_set);
  }

  if (m > o) {
    fds->fdset = xrealloc(fds->fdset, m);
    memset((char *)fds->fdset + o, 0, m - o);
  }
  if (n >= fds->maxfd) {
    fds->maxfd = n + 1;
  }
}

void rb_fd_set(int n, rb_fdset_t *fds) {
  rb_fd_resize(n, fds);
  FD_SET(n, fds->fdset);
}

void rb_fd_clr(int n, rb_fdset_t *fds) {
  if (n >= fds->maxfd) {
    return;
  }
  FD_CLR(n, fds->fdset);
}

int rb_fd_isset(int n, const rb_fdset_t *fds) {
  if (n >= fds->maxfd) {
    return 0;
  }
  return FD_ISSET(n, fds->fdset);
}

void rb_fd_copy(rb_fdset_t *dst, const fd_set *src, int max) {
  size_t size = howmany(max, NFDBITS) * sizeof(fd_mask);

  if (size < sizeof(fd_set)) {
    size = sizeof(fd_set);
  }
  dst->maxfd = max;
  dst->fdset = xrealloc(dst->fdset, size);
  memcpy(dst->fdset, src, size);
}

void rb_fd_dup(rb_fdset_t *dst, const rb_fdset_t *src) {
  size_t size = howmany(rb_fd_max(src), NFDBITS) * sizeof(fd_mask);

  if (size < sizeof(fd_set)) {
    size = sizeof(fd_set);
  }
  dst->maxfd = src->maxfd;
  dst->fdset = xrealloc(dst->fdset, size);
  memcpy(dst->fdset, src->fdset, size);
}

int rb_fd_select(int n, rb_fdset_t *readfds, rb_fdset_t *writefds, rb_fdset_t *exceptfds, struct timeval *timeout) {
  fd_set *r = NULL, *w = NULL, *e = NULL;
  if (readfds) {
    rb_fd_resize(n - 1, readfds);
    r = rb_fd_ptr(readfds);
  }
  if (writefds) {
    rb_fd_resize(n - 1, writefds);
    w = rb_fd_ptr(writefds);
  }
  if (exceptfds) {
    rb_fd_resize(n - 1, exceptfds);
    e = rb_fd_ptr(exceptfds);
  }
  return select(n, r, w, e, timeout);
}

// NOTE: MRI's version has more fields
struct select_set {
  int max;
  rb_fdset_t *rset;
  rb_fdset_t *wset;
  rb_fdset_t *eset;
  struct timeval *timeout;
};

static void* rb_thread_fd_select_blocking(void *data) {
  struct select_set *set = (struct select_set*)data;
  int result = rb_fd_select(set->max, set->rset, set->wset, set->eset, set->timeout);
  return (void*)(long)result;
}

int rb_thread_fd_select(int max, rb_fdset_t *read, rb_fdset_t *write, rb_fdset_t *except, struct timeval *timeout) {
  // NOTE: MRI has more logic in here
  struct select_set set;
  set.max = max;
  set.rset = read;
  set.wset = write;
  set.eset = except;
  set.timeout = timeout;

  void* result = rb_thread_call_without_gvl(rb_thread_fd_select_blocking, (void*)(&set), RUBY_UBF_IO, 0);
  return (int)(long)result;
}
