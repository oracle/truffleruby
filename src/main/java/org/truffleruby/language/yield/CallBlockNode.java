/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.yield;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CallerFrameAccess;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.DeclarationContext;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.utilities.AlwaysValidAssumption;

@NodeChildren({
        @NodeChild("block"),
        @NodeChild("self"),
        @NodeChild("blockArgument"),
        @NodeChild(value = "arguments", type = RubyNode[].class)
})
public abstract class CallBlockNode extends RubyNode {

    protected static enum SendsFrame {
        NO_FRAME,
        MY_FRAME,
        CALLER_FRAME;
    }

    private final DeclarationContext declarationContext;
    @CompilationFinal private SendsFrame sendsFrame = SendsFrame.NO_FRAME;
    @CompilationFinal private Assumption needsCallerAssumption;

    @Child private ReadCallerFrameNode readCaller;

    public CallBlockNode(DeclarationContext declarationContext) {
        this.declarationContext = declarationContext;
    }

    private boolean sendingFrames() {
        return sendsFrame != SendsFrame.NO_FRAME;
    }

    public void startSendingOwnFrame() {
        if (getContext().getCallStack().callerIsSend()) {
            startSendingFrame(SendsFrame.CALLER_FRAME);
        } else {
            startSendingFrame(SendsFrame.MY_FRAME);
        }
    }

    private synchronized void startSendingFrame(SendsFrame frameToSend) {
        if (sendingFrames()) {
            assert sendsFrame == frameToSend;
            return;
        }
        assert needsCallerAssumption != AlwaysValidAssumption.INSTANCE;
        this.sendsFrame = frameToSend;
        if (frameToSend == SendsFrame.CALLER_FRAME) {
            this.readCaller = insert(new ReadCallerFrameNode(CallerFrameAccess.MATERIALIZE));
        }
        Node root = getRootNode();
        if (root instanceof RubyRootNode) {
            ((RubyRootNode) root).invalidateNeedsCallerAssumption();
        } else {
            throw new Error();
        }
    }

    private synchronized void resetNeedsCallerAssumption() {
        Node root = getRootNode();
        if (root instanceof RubyRootNode && !sendingFrames()) {
            needsCallerAssumption = ((RubyRootNode) root).getNeedsCallerAssumption();
        } else {
            needsCallerAssumption = AlwaysValidAssumption.INSTANCE;
        }
    }

    public abstract Object executeCallBlock(VirtualFrame frame, DynamicObject block, Object self, Object blockArgument, Object[] arguments);

    // blockArgument is typed as Object below because it must accept "null".
    @Specialization(
            guards = "getBlockCallTarget(block) == cachedCallTarget",
            limit = "getCacheLimit()")
    protected Object callBlockCached(
            VirtualFrame frame,
            DynamicObject block,
            Object self,
            Object blockArgument,
            Object[] arguments,
            @Cached("getBlockCallTarget(block)") CallTarget cachedCallTarget,
            @Cached("createBlockCallNode(block, cachedCallTarget)") DirectCallNode callNode) {
        final Object[] frameArguments = packArguments(frame, block, self, blockArgument, arguments);
        return callNode.call(frameArguments);
    }

    @Specialization(replaces = "callBlockCached")
    protected Object callBlockUncached(
            VirtualFrame frame,
            DynamicObject block,
            Object self,
            Object blockArgument,
            Object[] arguments,
            @Cached("create()") IndirectCallNode callNode) {
        final Object[] frameArguments = packArguments(frame, block, self, blockArgument, arguments);
        return callNode.call(getBlockCallTarget(block), frameArguments);
    }

    private Object[] packArguments(VirtualFrame frame, DynamicObject block, Object self, Object blockArgument, Object[] arguments) {
        if (needsCallerAssumption == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            resetNeedsCallerAssumption();
        }
        try {
            needsCallerAssumption.check();
        } catch (InvalidAssumptionException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            resetNeedsCallerAssumption();
        }

        MaterializedFrame callerFrame;
        if (frame != null) {
            switch (sendsFrame) {
                case MY_FRAME:
                    callerFrame = frame.materialize();
                    break;
                case CALLER_FRAME:
                    callerFrame = readCaller.execute(frame).materialize();
                    break;
                default:
                    callerFrame = null;
            }
        } else {
            callerFrame = null;
        }

        return RubyArguments.pack(
                Layouts.PROC.getDeclarationFrame(block),
                callerFrame,
                Layouts.PROC.getMethod(block),
                declarationContext,
                Layouts.PROC.getFrameOnStackMarker(block),
                self,
                (DynamicObject) blockArgument,
                arguments);
    }

    protected static CallTarget getBlockCallTarget(DynamicObject block) {
        return Layouts.PROC.getCallTargetForType(block);
    }

    protected DirectCallNode createBlockCallNode(DynamicObject block, CallTarget callTarget) {
        final DirectCallNode callNode = Truffle.getRuntime().createDirectCallNode(callTarget);

        final boolean clone = Layouts.PROC.getSharedMethodInfo(block).shouldAlwaysClone() || getContext().getOptions().YIELD_ALWAYS_CLONE;
        if (clone && callNode.isCallTargetCloningAllowed()) {
            callNode.cloneCallTarget();
        }

        if (getContext().getOptions().YIELD_ALWAYS_INLINE && callNode.isInlinable()) {
            callNode.forceInlining();
        }

        return callNode;
    }

    protected int getCacheLimit() {
        return getContext().getOptions().YIELD_CACHE;
    }
}
