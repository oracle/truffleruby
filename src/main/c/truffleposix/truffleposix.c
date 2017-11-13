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

#include <dirent.h>
#include <errno.h>
#include <stdlib.h>
#include <stdint.h>

#include <sys/resource.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h>

struct truffleposix_stat {
  uint64_t atime;
  uint64_t mtime;
  uint64_t ctime;
  uint64_t nlink;
  uint64_t rdev;
  uint64_t blksize;
  uint64_t blocks;
  uint64_t dev;
  uint64_t ino;
  uint64_t size;
  uint64_t mode;
  uint64_t gid;
  uint64_t uid;
};

static void copy_stat(struct stat *stat, struct truffleposix_stat* buffer);

char* truffleposix_readdir(DIR *dirp) {
  errno = 0;
  struct dirent *entry = readdir(dirp);
  if (entry != NULL) {
    return entry->d_name;
  } else if (errno == 0) {
    return "";
  } else {
    return NULL;
  }
}

void truffleposix_rewinddir(DIR *dirp) {
  rewinddir(dirp);
}

int truffleposix_getpriority(int which, id_t who) {
  /* getpriority() can return -1 so errno has to be cleared. */
  errno = 0;
  int r = getpriority(which, who);
  if (r == -1 && errno != 0) {
    /* getpriority() is between -20 and 19 on Linux and -20 and 20 on macOS and Solaris */
    return -100 - errno;
  }
  return r;
}

pid_t truffleposix_waitpid(pid_t pid, int options, int result[3]) {
  int status = 0;
  pid_t r = waitpid(pid, &status, options);
  if (r <= 0) {
    return r;
  }

  int exitcode = -1000, termsig = -1000, stopsig = -1000;
  if (WIFEXITED(status)) {
    exitcode = WEXITSTATUS(status);
  } else if (WIFSIGNALED(status)) {
    termsig = WTERMSIG(status);
  } else if (WIFSTOPPED(status)) {
    stopsig = WSTOPSIG(status);
  }

  result[0] = exitcode;
  result[1] = termsig;
  result[2] = stopsig;
  return r;
}

/* flock() is not available on Solaris */
#if defined __sun
#define LOCK_SH 1
#define LOCK_EX 2
#define LOCK_NB 4
#define LOCK_UN 8

#include <fcntl.h>
#include <unistd.h>

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

int truffleposix_stat(const char *path, struct truffleposix_stat *buffer) {
  struct stat native_stat;
  int result = stat(path, &native_stat);
  if (result == 0) {
    copy_stat(&native_stat, buffer);
  }
  return result;
}

int truffleposix_fstat(int fd, struct truffleposix_stat *buffer) {
  struct stat native_stat;
  int result = fstat(fd, &native_stat);
  if (result == 0) {
    copy_stat(&native_stat, buffer);
  }
  return result;
}

int truffleposix_lstat(const char *path, struct truffleposix_stat *buffer) {
  struct stat native_stat;
  int result = lstat(path, &native_stat);
  if (result == 0) {
    copy_stat(&native_stat, buffer);
  }
  return result;
}

static void copy_stat(struct stat *native_stat, struct truffleposix_stat* buffer) {
  buffer->atime   = native_stat->st_atime;
  buffer->atime   = native_stat->st_atime;
  buffer->mtime   = native_stat->st_mtime;
  buffer->ctime   = native_stat->st_ctime;
  buffer->nlink   = native_stat->st_nlink;
  buffer->rdev    = native_stat->st_rdev;
  buffer->blksize = native_stat->st_blksize;
  buffer->blocks  = native_stat->st_blocks;
  buffer->dev     = native_stat->st_dev;
  buffer->ino     = native_stat->st_ino;
  buffer->size    = native_stat->st_size;
  buffer->mode    = native_stat->st_mode;
  buffer->gid     = native_stat->st_gid;
  buffer->uid     = native_stat->st_uid;
}
