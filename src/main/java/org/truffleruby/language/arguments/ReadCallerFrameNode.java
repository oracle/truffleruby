/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameInstance;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.CachedDispatchNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadCallerFrameNode extends RubyBaseNode {

    private final ConditionProfile callerFrameProfile = ConditionProfile.createBinaryProfile();
    @CompilationFinal private volatile boolean firstCall = true;

    public static ReadCallerFrameNode create() {
        return new ReadCallerFrameNode();
    }

    public MaterializedFrame execute(VirtualFrame frame) {
        // Avoid polluting the profile for the first call which has to use getCallerFrame()
        if (firstCall) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            firstCall = false;
            notifyCallerToSendFrame();
            return getCallerFrame();
        }

        final MaterializedFrame callerFrame = RubyArguments.getCallerFrame(frame);

        if (callerFrameProfile.profile(callerFrame != null)) {
            return callerFrame;
        } else {
            return getCallerFrame();
        }
    }

    private void notifyCallerToSendFrame() {
        final Node callerNode = getContext().getCallStack().getCallerNode(0, false);
        if (callerNode instanceof DirectCallNode) {
            final Node parent = callerNode.getParent();
            if (parent instanceof CachedDispatchNode) {
                ((CachedDispatchNode) parent).startSendingOwnFrame();
            }
        }
    }

    @TruffleBoundary
    private MaterializedFrame getCallerFrame() {
        return getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameInstance.FrameAccess.MATERIALIZE).materialize();
    }

}
