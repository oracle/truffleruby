/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.posix;

import jnr.constants.platform.Fcntl;
import jnr.constants.platform.Signal;
import jnr.constants.platform.Sysconf;
import jnr.posix.FileStat;
import jnr.posix.Passwd;
import jnr.posix.SignalHandler;
import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import jnr.posix.Times;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface TrufflePosix {

    byte[] crypt(byte[] key, byte[] salt);
    FileStat allocateStat();
    int execve(String path, String[] argv, String[] envp);
    int fstat(int fd, FileStat stat);
    int getpid();
    Passwd getpwnam(String which);
    int kill(int pid, int signal);
    SignalHandler signal(Signal sig, SignalHandler handler);
    int lstat(String path, FileStat stat);
    FileStat stat(String path);
    int stat(String path, FileStat stat);
    int symlink(String oldpath,String newpath);
    int waitpid(int pid, int[] status, int flags);
    int errno();
    void errno(int value);
    int chdir(String path);
    long sysconf(Sysconf name);
    Times times();
    int posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends SpawnAttribute> spawnAttributes,
            Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp);
    int flock(int fd, int operation);

    int dup2(int oldFd, int newFd);
    int fcntlInt(int fd, Fcntl fcntlConst, int arg);
    int fcntl(int fd, Fcntl fcntlConst);
    int close(int fd);
    int open(CharSequence path, int flags, int perm);
    int write(int fd, byte[] buf, int n);
    int read(int fd, byte[] buf, int n);
    int write(int fd, ByteBuffer buf, int n);
    int read(int fd, ByteBuffer buf, int n);
    int lseek(int fd, long offset, int whence);
    int pipe(int[] fds);
    int truncate(CharSequence path, long length);
    int ftruncate(int fd, long offset);
    int rename(CharSequence oldName, CharSequence newName);
    String getcwd();
    int isatty(int fd);
    String nl_langinfo(int item);

}
