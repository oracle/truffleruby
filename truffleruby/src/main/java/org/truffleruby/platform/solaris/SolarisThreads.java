/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.solaris;

import org.truffleruby.platform.posix.SigAction;
import org.truffleruby.platform.posix.Threads;

public interface SolarisThreads {

    // pthread_t is a 4 bytes unsigned int on Solaris

    // pthread_t pthread_self(void);
    int pthread_self();

    // int pthread_kill(pthread_t thread, int sig);
    int pthread_kill(int thread, int sig);

    // int sigaction(int signum, const struct sigaction *act, struct sigaction *oldact);
    int sigaction(int signum, SigAction act, SigAction oldAct);

}

class SolarisThreadsImplementation implements Threads {

    final SolarisThreads nativeThreads;

    public SolarisThreadsImplementation(SolarisThreads nativeThreads) {
        this.nativeThreads = nativeThreads;
    }

    public long pthread_self() {
        return nativeThreads.pthread_self();
    }

    public int pthread_kill(long thread, int sig) {
        return nativeThreads.pthread_kill((int) thread, sig);
    }

    public int sigaction(int signum, SigAction act, SigAction oldAct) {
        return nativeThreads.sigaction(signum, act, oldAct);
    }

}
