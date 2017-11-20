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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import jnr.constants.platform.Sysconf;
import jnr.posix.POSIX;
import jnr.posix.Passwd;
import jnr.posix.Times;
import org.truffleruby.RubyContext;
import org.truffleruby.language.control.JavaException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class JNRTrufflePosix implements TrufflePosix {

    protected final RubyContext context;
    private final POSIX posix;

    public JNRTrufflePosix(RubyContext context, POSIX posix) {
        this.context = context;
        this.posix = posix;
    }

    protected POSIX getPosix() {
        return posix;
    }

    @TruffleBoundary
    @Override
    public Passwd getpwnam(String which) {
        return posix.getpwnam(which);
    }

    @TruffleBoundary
    @Override
    public int errno() {
        return posix.errno();
    }

    @TruffleBoundary
    @Override
    public long sysconf(Sysconf name) {
        return posix.sysconf(name);
    }

    @TruffleBoundary
    @Override
    public Times times() {
        return posix.times();
    }

    @TruffleBoundary
    @Override
    public int read(int fd, ByteBuffer buf, int n) {
        if (context.getOptions().POLYGLOT_STDIO && fd == 0) {
            return polyglotRead(buf.array(), buf.arrayOffset(), n);
        }

        return posix.read(fd, buf, n);
    }

    @TruffleBoundary
    protected int polyglotRead(byte[] buf, int offset, int n) {
        try {
            return context.getEnv().in().read(buf, offset, n);
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    @TruffleBoundary
    @Override
    public String getcwd() {
        return posix.getcwd();
    }

    @TruffleBoundary
    @Override
    public String nl_langinfo(int item) {
        return posix.nl_langinfo(item);
    }

}
