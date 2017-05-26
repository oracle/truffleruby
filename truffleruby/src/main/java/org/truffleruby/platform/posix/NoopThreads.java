package org.truffleruby.platform.posix;

public class NoopThreads implements Threads {

    public long pthread_self() {
        return 0L;
    }

    public int pthread_kill(long thread, int sig) {
        return 0;
    }

}
