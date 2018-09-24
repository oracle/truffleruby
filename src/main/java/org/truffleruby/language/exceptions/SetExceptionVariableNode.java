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

import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.GetCurrentRubyThreadNodeGen;
import org.truffleruby.core.thread.ThreadNodes.GetThreadLocalExceptionNode;
import org.truffleruby.core.thread.ThreadNodes.SetThreadLocalExceptionNode;
import org.truffleruby.core.thread.ThreadNodesFactory.GetThreadLocalExceptionNodeFactory;
import org.truffleruby.core.thread.ThreadNodesFactory.SetThreadLocalExceptionNodeFactory;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

public class SetExceptionVariableNode extends Node {

    @Child private GetCurrentRubyThreadNode getCurrentThreadNode;
    @Child private SetThreadLocalExceptionNode setThreadLocalExceptionNode;
    @Child private GetThreadLocalExceptionNode getThreadLocalExceptionNode;

    public Object setLastExceptionAndRun(VirtualFrame frame, RaiseException exception, RubyNode node) {
        final DynamicObject lastException = getLastException(frame);
        setLastException(frame, exception.getException());

        try {
            return node.execute(frame);
        } finally {
            setLastException(frame, lastException);
        }
    }

    public void setLastException(VirtualFrame frame, DynamicObject exception) {
        if (setThreadLocalExceptionNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            setThreadLocalExceptionNode = insert(SetThreadLocalExceptionNodeFactory.create(new RubyNode[0]));
        }

        setThreadLocalExceptionNode.setException(frame, exception, getCurrentThreadNode());
    }

    public DynamicObject getLastException(VirtualFrame frame) {
        if (getThreadLocalExceptionNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getThreadLocalExceptionNode = insert(GetThreadLocalExceptionNodeFactory.create(new RubyNode[0]));
        }

        return getThreadLocalExceptionNode.getException(frame, getCurrentThreadNode());
    }

    private GetCurrentRubyThreadNode getCurrentThreadNode() {
        if (getCurrentThreadNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getCurrentThreadNode = GetCurrentRubyThreadNodeGen.create();
        }
        return getCurrentThreadNode;
    }
}
