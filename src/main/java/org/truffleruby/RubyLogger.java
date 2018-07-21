/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class RubyLogger {

    /**
     * Warn about code that works but is not yet optimized as Truffle code normally would be. Only
     * prints the warning once, and only if called from compiled code. Don't call this method from
     * behind a boundary, as it will never print the warning because it will never be called from
     * compiled code.
     */
    public static void notOptimizedOnce(String message) {
        if (CompilerDirectives.inCompiledCode()) {
            notOptimizedOnceBoundary(message);
        }
    }

    @TruffleBoundary
    private static void notOptimizedOnceBoundary(String message) {
        if (DISPLAYED_WARNINGS.add(message)) {
            RubyLanguage.LOGGER.log(Level.WARNING, message);
        }
    }

    private static final Set<String> DISPLAYED_WARNINGS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static final String KWARGS_NOT_OPTIMIZED_YET = "keyword arguments are not yet optimized";
    public static final String UNSTABLE_INTERPOLATED_REGEXP = "unstable interpolated regexps are not optimized";

}
