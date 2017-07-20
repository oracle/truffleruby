/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.launcher.TruffleLessLog;

import java.util.logging.Level;

public class Log extends TruffleLessLog {

    /**
     * Warn about code that works but is not yet optimized as Truffle code normally would be. Only
     * prints the warning once, and only if called from compiled code. Don't call this method from
     * behind a boundary, as it will never print the warning because it will never be called from
     * compiled code. Use {@link #performanceOnce} instead if you need to warn in code that is never
     * compiled.
     */
    public static void notOptimizedOnce(String message) {
        if (CompilerDirectives.inCompiledCode()) {
            performanceOnce(message);
        }
    }

    /**
     * Warn about something that has lower performance than might be expected. Only prints the
     * warning once.
     */
    @TruffleBoundary
    public static void performanceOnce(String message) {
        TruffleLessLog.performanceOnce(message);
    }

    @TruffleBoundary
    public static void warning(String format, Object... args) {
        TruffleLessLog.warning(format, args);
    }

    @TruffleBoundary
    public static void info(String format, Object... args) {
        TruffleLessLog.info(format, args);
    }

    @TruffleBoundary
    public static void log(Level level, String message) {
        TruffleLessLog.log(level, message);
    }

}
