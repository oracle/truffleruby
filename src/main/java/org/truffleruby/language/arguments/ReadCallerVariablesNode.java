/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SpecialVariablesSendingNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

import com.oracle.truffle.api.frame.MaterializedFrame;

/** See {@link SpecialVariablesSendingNode} */
public class ReadCallerVariablesNode extends RubyBaseNode {

    private final ConditionProfile inArgumentsProfile = ConditionProfile.create();
    @Child private NotOptimizedWarningNode notOptimizedNode = null;

    public static ReadCallerVariablesNode create() {
        return new ReadCallerVariablesNode();
    }

    public SpecialVariableStorage execute(Frame frame) {
        final SpecialVariableStorage data = RubyArguments.getCallerSpecialVariables(frame);
        if (inArgumentsProfile.profile(data != null)) {
            return data;
        } else {
            return getCallerSpecialVariables();
        }
    }

    @TruffleBoundary
    protected SpecialVariableStorage getCallerSpecialVariables() {
        final Node callerNode = getContext().getCallStack().getCallerNode(1, false);
        if (!notifyCallerToSendSpecialVariables(callerNode)) {
            // If we fail to notify the call node (e.g., because it is a UncachedDispatchNode which is not handled yet),
            // we don't want to deoptimize this CallTarget on every call.
            getNotOptimizedNode().warn("Unoptimized reading of caller special variables.");
        }

        final MaterializedFrame callerFrame = Truffle.getRuntime()
                .iterateFrames(f -> f.getFrame(FrameAccess.MATERIALIZE).materialize(), 1);
        if (!CallStackManager.isRubyFrame(callerFrame)) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().runtimeError(
                            "Cannot call Ruby method which needs caller special variables directly in a foreign language",
                            this));
        }

        return GetSpecialVariableStorage.getSlow(callerFrame);
    }

    public static boolean notifyCallerToSendSpecialVariables(Node callerNode) {
        if (callerNode instanceof DirectCallNode || callerNode instanceof IndirectCallNode) {
            Node parent = callerNode.getParent();
            while (parent != null) {
                if (parent instanceof SpecialVariablesSendingNode) {
                    ((SpecialVariablesSendingNode) parent).startSendingOwnVariables();
                    return true;
                }
                if (parent instanceof RubyNode) {
                    // A node with source info representing Ruby code, we could not find the FrameAndVariablesSendingNode
                    return false;
                }
                parent = parent.getParent();
            }
        }

        return false;
    }

    private NotOptimizedWarningNode getNotOptimizedNode() {
        if (notOptimizedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            notOptimizedNode = insert(NotOptimizedWarningNode.create());
        }
        return notOptimizedNode;
    }
}
