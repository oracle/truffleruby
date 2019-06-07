/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import org.truffleruby.Layouts;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.GetCurrentRubyThreadNodeGen;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class SetExceptionVariableNode extends RubyBaseWithoutContextNode {

    @Child private GetCurrentRubyThreadNode getCurrentThreadNode;

    public Object setLastExceptionAndRun(VirtualFrame frame, RaiseException exception, RubyNode node) {
        final DynamicObject thread = getCurrentThreadNode().executeGetRubyThread(frame);
        final ThreadLocalGlobals threadLocalGlobals = Layouts.THREAD.getThreadLocalGlobals(thread);
        final DynamicObject lastException = threadLocalGlobals.exception;
        threadLocalGlobals.exception = exception.getException();

        try {
            return node.execute(frame);
        } finally {
            threadLocalGlobals.exception = lastException;
        }
    }


    public void setLastException(VirtualFrame frame, DynamicObject exception) {
        final DynamicObject thread = getCurrentThreadNode().executeGetRubyThread(frame);
        final ThreadLocalGlobals threadLocalGlobals = Layouts.THREAD.getThreadLocalGlobals(thread);
        threadLocalGlobals.exception = exception;
    }

    private GetCurrentRubyThreadNode getCurrentThreadNode() {
        if (getCurrentThreadNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getCurrentThreadNode = insert(GetCurrentRubyThreadNodeGen.create());
        }
        return getCurrentThreadNode;
    }
}
