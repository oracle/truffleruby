/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.arguments;

import org.truffleruby.builtins.CallerFrameAccess;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CachedDispatchNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadCallerFrameNode extends RubyNode {

    private final ConditionProfile callerFrameProfile = ConditionProfile.createBinaryProfile();

    private final CallerFrameAccess accessMode;

    public ReadCallerFrameNode(CallerFrameAccess callerFrameAccess) {
        this.accessMode = callerFrameAccess;
    }

    @Override
    public Frame execute(VirtualFrame frame) {
        final MaterializedFrame callerFrame = RubyArguments.getCallerFrame(frame);

        if (callerFrameProfile.profile(callerFrame != null)) {
            return callerFrame;
        } else {
            return getCallerFrame();
        }
    }

    private void replaceDispatchNode() {
        CompilerAsserts.neverPartOfCompilation("Dispatch nodes should never be replaced after compilation.");
        Node callerNode = getContext().getCallStack().getCallerNode();
        if (callerNode instanceof DirectCallNode) {
            Node parent = callerNode.getParent();
            if (parent instanceof CachedDispatchNode) {
                if (getContext().getCallStack().callerIsSend()) {
                    ((CachedDispatchNode) parent).replaceSendingCallerFrame(accessMode);
                } else {
                    ((CachedDispatchNode) parent).replaceSendingFrame();
                }
            }
        }
    }

    @TruffleBoundary
    private Frame getCallerFrame() {
        if (!CompilerDirectives.inCompiledCode()) {
            replaceDispatchNode();
        }
        return getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(accessMode.getFrameAccess());
    }

}
