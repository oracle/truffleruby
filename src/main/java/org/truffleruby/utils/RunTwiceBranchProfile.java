/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.nodes.NodeCloneable;

public final class RunTwiceBranchProfile extends NodeCloneable {

    private enum ExecuteCounter {
        NEVER,
        ONCE,
        MANY;

        public ExecuteCounter next() {
            if (this == NEVER) {
                return ONCE;
            } else {
                return MANY;
            }
        }
    }

    @CompilationFinal private ExecuteCounter executeCounter = ExecuteCounter.NEVER;

    public void enter() {
        if (executeCounter != ExecuteCounter.MANY) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            executeCounter = executeCounter.next();
        }
    }
}
