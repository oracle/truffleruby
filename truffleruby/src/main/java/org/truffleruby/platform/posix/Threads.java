/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.posix;

public interface Threads {

    // Assumes pthread_t is 8 bytes, on Linux it's a long, on OS X a pointer

    // pthread_t pthread_self(void);
    long pthread_self();

    // int pthread_kill(pthread_t thread, int sig);
    int pthread_kill(long thread, int sig);

    // int sigaction(int signum, const struct sigaction *act, struct sigaction *oldact);
    int sigaction(int signum, SigAction act, SigAction oldAct);

}
