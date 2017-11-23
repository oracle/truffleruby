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
import jnr.posix.POSIX;
import org.truffleruby.RubyContext;

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
    public int errno() {
        return posix.errno();
    }

}
