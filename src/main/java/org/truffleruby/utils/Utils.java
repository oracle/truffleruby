/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/** General purpose utility functions that do not fit in other utility classes. */
public final class Utils {
    /** Build a {@link UnsupportedOperationException} behind a {@link TruffleBoundary} so as to avoid performance
     * warnings from the call to {@link Throwable#fillInStackTrace()}. */
    @TruffleBoundary
    public static UnsupportedOperationException unsupportedOperation(String msg) {
        return new UnsupportedOperationException(msg);
    }

    /** Performs {@link Objects#equals(Object, Object)} behind a {@link TruffleBoundary} so as to avoid performance
     * warnings, since {@link Object#equals} is blacklisted. */
    @TruffleBoundary
    public static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }
}
