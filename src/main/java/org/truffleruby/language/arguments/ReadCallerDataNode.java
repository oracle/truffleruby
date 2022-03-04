/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.FrameAndVariablesSendingNode;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

public abstract class ReadCallerDataNode extends RubyBaseNode implements CallerDataReadingNode {

    private final ConditionProfile callerDataProfile = ConditionProfile.create();
    @Child private NotOptimizedWarningNode notOptimizedNode = null;

    public Object execute(Frame frame) {
        final Object data = getData(frame);

        if (callerDataProfile.profile(data != null)) {
            return data;
        } else {
            return getCallerData();
        }
    }

    protected abstract Object getData(Frame frame);

    @TruffleBoundary
    protected Object getCallerData() {
        final Node callerNode = getContext().getCallStack().getCallerNode(1, false);
        if (!CallerDataReadingNode.notifyCallerToSendData(getContext(), callerNode, this)) {
            // If we fail to notify the call node (e.g., because it is a UncachedDispatchNode which is not handled yet),
            // we don't want to deoptimize this CallTarget on every call.
            getNotOptimizedNode().warn("Unoptimized reading of caller data.");
        }

        final MaterializedFrame callerFrame = Truffle.getRuntime()
                .iterateFrames(f -> f.getFrame(FrameAccess.MATERIALIZE).materialize(), 1);
        if (!CallStackManager.isRubyFrame(callerFrame)) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().runtimeError(
                            "Cannot call Ruby method which needs caller data directly in a foreign language",
                            this));
        }

        return getDataFromFrame(callerFrame);
    }

    protected abstract Object getDataFromFrame(MaterializedFrame frame);

    public abstract void startSending(FrameAndVariablesSendingNode node);

    private NotOptimizedWarningNode getNotOptimizedNode() {
        if (notOptimizedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            notOptimizedNode = insert(NotOptimizedWarningNode.create());
        }
        return notOptimizedNode;
    }
}
