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
#include <ruby/io.h>
#include <fcntl.h>
#include <internal/io.h>

// IO, rb_io_*

void rb_io_check_writable(rb_io_t *io) {
  if (!rb_tr_writable(io->mode)) {
    rb_raise(rb_eIOError, "not opened for writing");
  }
}

void rb_io_check_readable(rb_io_t *io) {
  if (!rb_tr_readable(io->mode)) {
    rb_raise(rb_eIOError, "not opened for reading");
  }
}

void rb_update_max_fd(int fd) {
}

void rb_fd_fix_cloexec(int fd) {
  fcntl(fd, F_SETFD, fcntl(fd, F_GETFD) | FD_CLOEXEC);
}

int rb_io_wait_readable(int fd) {
  if (fd < 0) {
    rb_raise(rb_eIOError, "closed stream");
  }

  switch (errno) {
    case EAGAIN:
  #if defined(EWOULDBLOCK) && EWOULDBLOCK != EAGAIN
    case EWOULDBLOCK:
  #endif
      rb_thread_wait_fd(fd);
      return true;

    default:
      return false;
  }
}

int rb_io_wait_writable(int fd) {
  if (fd < 0) {
    rb_raise(rb_eIOError, "closed stream");
  }

  switch (errno) {
    case EAGAIN:
  #if defined(EWOULDBLOCK) && EWOULDBLOCK != EAGAIN
    case EWOULDBLOCK:
  #endif
      rb_thread_fd_writable(fd);
      return true;

    default:
      return false;
  }
}

int rb_thread_wait_fd(int fd) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_thread_wait_fd", fd));
}

int rb_wait_for_single_fd(int fd, int events, struct timeval *tv) {
  long tv_sec = -1;
  long tv_usec = -1;
  if (tv != NULL) {
    tv_sec = tv->tv_sec;
    tv_usec = tv->tv_usec;
  }
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_wait_for_single_fd", fd, events, tv_sec, tv_usec));
}

VALUE rb_io_addstr(VALUE io, VALUE str) {
  // use write instead of just #<<, it's closer to what MRI does
  // and avoids stack-overflow in zlib where #<< is defined with this method
  rb_io_write(io, str);
  return io;
}

VALUE rb_io_check_io(VALUE io) {
  return rb_check_convert_type(io, T_FILE, "IO", "to_io");
}

void rb_io_check_closed(rb_io_t *fptr) {
  if (fptr->fd < 0) {
    rb_raise(rb_eIOError, "closed stream");
  }
}

VALUE rb_io_taint_check(VALUE io) {
  rb_check_frozen(io);
  return io;
}

VALUE rb_io_close(VALUE io) {
  return RUBY_INVOKE(io, "close");
}

VALUE rb_io_print(int argc, const VALUE *argv, VALUE out) {
  return RUBY_CEXT_INVOKE("rb_io_print", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_printf(int argc, const VALUE *argv, VALUE out) {
  return RUBY_CEXT_INVOKE("rb_io_printf", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_puts(int argc, const VALUE *argv, VALUE out) {
  return RUBY_CEXT_INVOKE("rb_io_puts", out, rb_ary_new4(argc, argv));
}

VALUE rb_io_write(VALUE io, VALUE str) {
  return RUBY_INVOKE(io, "write", str);
}

VALUE rb_io_binmode(VALUE io) {
  return RUBY_INVOKE(io, "binmode");
}

int rb_thread_fd_writable(int fd) {
  return polyglot_as_i32(polyglot_invoke(RUBY_CEXT, "rb_thread_fd_writable", fd));
}

int rb_cloexec_open(const char *pathname, int flags, mode_t mode) {
  int fd = open(pathname, flags, mode);
  if (fd >= 0) {
    rb_fd_fix_cloexec(fd);
  }
  return fd;
}

VALUE rb_file_open(const char *fname, const char *modestr) {
  return RUBY_INVOKE(rb_cFile, "open", rb_str_new_cstr(fname), rb_str_new_cstr(modestr));
}

VALUE rb_file_open_str(VALUE fname, const char *modestr) {
  return RUBY_INVOKE(rb_cFile, "open", fname, rb_str_new_cstr(modestr));
}

VALUE rb_get_path(VALUE object) {
  return RUBY_INVOKE(rb_cFile, "path", object);
}

int rb_tr_readable(int mode) {
  return mode & FMODE_READABLE;
}

int rb_tr_writable(int mode) {
  return mode & FMODE_WRITABLE;
}

int rb_io_extract_encoding_option(VALUE opt, rb_encoding **enc_p, rb_encoding **enc2_p, int *fmode_p) {
  // TODO (pitr-ch 12-Jun-2017): review, just approximate implementation
  VALUE encoding = rb_cEncoding;
  VALUE external_encoding = RUBY_INVOKE(encoding, "default_external");
  VALUE internal_encoding = RUBY_INVOKE(encoding, "default_internal");
  if (!NIL_P(external_encoding)) {
    *enc_p = rb_to_encoding(external_encoding);
  }
  if (!NIL_P(internal_encoding)) {
    *enc2_p = rb_to_encoding(internal_encoding);
  }
  return 1;
}

static int rb_fd_set_nonblock(int fd) {
  int oflags = fcntl(fd, F_GETFL);
  if (oflags == -1)
    return -1;
  if (oflags & O_NONBLOCK)
    return 0;
  oflags |= O_NONBLOCK;
  return fcntl(fd, F_SETFL, oflags);
}

void rb_io_set_nonblock(rb_io_t *fptr) {
  if (rb_fd_set_nonblock(fptr->fd) != 0) {
    rb_sys_fail("rb_io_set_nonblock failed");
  }
}

// For 'gem install curb'
FILE *rb_io_stdio_file(rb_io_t *fptr) {
  rb_tr_error("rb_io_stdio_file not yet implemented");
}

VALUE rb_lastline_get(void) {
  return RUBY_CEXT_INVOKE("rb_lastline_get");
}

void rb_lastline_set(VALUE str) {
  RUBY_CEXT_INVOKE_NO_WRAP("rb_lastline_set", str);
}

