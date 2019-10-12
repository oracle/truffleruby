/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.CachedDispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadCallerFrameNode extends RubyBaseNode {

    private final ConditionProfile callerFrameProfile = ConditionProfile.createBinaryProfile();
    @CompilationFinal private volatile boolean deoptWhenNotPassedCallerFrame = true;

    public static ReadCallerFrameNode create() {
        return new ReadCallerFrameNode();
    }

    public MaterializedFrame execute(VirtualFrame frame) {
        final MaterializedFrame callerFrame = RubyArguments.getCallerFrame(frame);

        if (callerFrameProfile.profile(callerFrame != null)) {
            return callerFrame;
        } else {
            // Every time the caller of the method using ReadCallerFrameNode changes,
            // we need to notify the caller's CachedDispatchNode to pass us the frame next time.
            if (deoptWhenNotPassedCallerFrame) {
                // Invalidate because deoptWhenNotPassedCallerFrame might change and require recompilation
                CompilerDirectives.transferToInterpreterAndInvalidate();
            }
            return getCallerFrame();
        }
    }

    @TruffleBoundary
    private MaterializedFrame getCallerFrame() {
        if (!notifyCallerToSendFrame()) {
            // If we fail to notify the call node (e.g., because it is a UncachedDispatchNode which is not handled yet),
            // we don't want to deoptimize this CallTarget on every call.
            deoptWhenNotPassedCallerFrame = false;
        }
        return getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.MATERIALIZE).materialize();
    }

    private boolean notifyCallerToSendFrame() {
        final Node callerNode = getContext().getCallStack().getCallerNode(1, false);
        if (callerNode instanceof DirectCallNode) {
            final Node parent = callerNode.getParent();
            if (parent instanceof CachedDispatchNode) {
                ((CachedDispatchNode) parent).startSendingOwnFrame();
                return true;
            }
        }

        return false;
    }

}
