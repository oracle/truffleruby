/*
Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
code is released under a tri EPL/GPL/LGPL license. You can use it,
redistribute it and/or modify it under the terms of the:

Eclipse Public License version 1.0
GNU General Public License version 2
GNU Lesser General Public License version 2.1
*/

/*
Copyright (C) 1993-2013 Yukihiro Matsumoto. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
*/

extern int my_flock(int fd, int operation);

/* flock() is not available on Solaris */
#if defined __sun
#define LOCK_SH 1
#define LOCK_EX 2
#define LOCK_NB 4
#define LOCK_UN 8

#include <fcntl.h>
#include <unistd.h>
#include <errno.h>

int truffleposix_flock(int fd, int operation) {
  struct flock lock;
  switch (operation & ~LOCK_NB) {
  case LOCK_SH:
    lock.l_type = F_RDLCK;
    break;
  case LOCK_EX:
    lock.l_type = F_WRLCK;
    break;
  case LOCK_UN:
    lock.l_type = F_UNLCK;
    break;
  default:
    errno = EINVAL;
    return -1;
  }
  lock.l_whence = SEEK_SET;
  lock.l_start = 0L;
  lock.l_len = 0L;
  int r = fcntl(fd, (operation & LOCK_NB) ? F_SETLK : F_SETLKW, &lock);
  if (r == -1 && errno == EAGAIN) {
    errno = EWOULDBLOCK;
  }
  return r;
}
#else
#include <sys/file.h>
int truffleposix_flock(int fd, int operation) {
  return flock(fd, operation);
}
#endif
