/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.utilities.AlwaysValidAssumption;

public abstract class CachedDispatchNode extends DispatchNode {

    protected enum SendsFrame {
        NO_FRAME,
        MY_FRAME,
        CALLER_FRAME;
    }

    private final Object cachedName;
    private final DynamicObject cachedNameAsSymbol;

    @Child protected DispatchNode next;
    @Child private RopeNodes.BytesEqualNode ropeEqualsNode;

    private final BranchProfile moreThanReferenceCompare = BranchProfile.create();

    @CompilationFinal private SendsFrame sendsFrame = SendsFrame.NO_FRAME;
    @CompilationFinal private Assumption needsCallerAssumption;

    @Child private ReadCallerFrameNode readCaller;

    public CachedDispatchNode(
            RubyContext context,
            Object cachedName,
            DispatchNode next,
            DispatchAction dispatchAction) {
        super(dispatchAction);

        assert (cachedName instanceof String) || (RubyGuards.isRubySymbol(cachedName)) || (RubyGuards.isRubyString(cachedName));
        this.cachedName = cachedName;

        if (RubyGuards.isRubySymbol(cachedName)) {
            cachedNameAsSymbol = (DynamicObject) cachedName;
        } else if (RubyGuards.isRubyString(cachedName)) {
            cachedNameAsSymbol = context.getSymbolTable().getSymbol(StringOperations.rope((DynamicObject) cachedName));
            ropeEqualsNode = RopeNodes.BytesEqualNode.create();
        } else if (cachedName instanceof String) {
            cachedNameAsSymbol = context.getSymbolTable().getSymbol((String) cachedName);
        } else {
            throw new UnsupportedOperationException();
        }

        this.next = next;
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
            this.readCaller = insert(new ReadCallerFrameNode());
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

    @Override
    protected DispatchNode getNext() {
        return next;
    }

    @ExplodeLoop
    protected static void checkAssumptions(Assumption[] assumptions) throws InvalidAssumptionException {
        for (Assumption assumption : assumptions) {
            CompilerAsserts.compilationConstant(assumption);
            assumption.check();
        }
    }

    protected final boolean guardName(Object methodName) {
        if (cachedName == methodName) {
            return true;
        }

        moreThanReferenceCompare.enter();

        if (cachedName instanceof String) {
            return cachedName.equals(methodName);
        } else if (ropeEqualsNode != null) { // cachedName is a Ruby String
            return RubyGuards.isRubyString(methodName) && ropeEqualsNode.execute(StringOperations.rope((DynamicObject) cachedName), StringOperations.rope((DynamicObject) methodName));
        } else { // cachedName is a Symbol
            // cachedName == methodName was checked above and was not true,
            // and since Symbols are compared by identity we know they don't match.
            // We also want to keep the fast-path identity comparison above for non-Symbols,
            // as it is more efficient than a full comparison.
            return false;
        }
    }

    protected DynamicObject getCachedNameAsSymbol() {
        return cachedNameAsSymbol;
    }

    protected abstract void reassessSplittingInliningStrategy();

    protected void applySplittingInliningStrategy(DirectCallNode callNode, InternalMethod method) {
        if (callNode.isCallTargetCloningAllowed() && method.getSharedMethodInfo().shouldAlwaysClone()) {
            callNode.cloneCallTarget();
        } else if (getContext().getOptions().CALL_WITH_BLOCK_ALWAYS_CLONE) {
            final RubyCallNode rubyCallNode = findRubyCallNode();
            if (rubyCallNode != null && rubyCallNode.hasLiteralBlock()) {
                // If the call has a literal block, it is interesting to split so the method can
                // specialize for that block only and not all blocks given to the method so far.
                callNode.cloneCallTarget();
            }
        }

        if (sendingFrames() && getContext().getOptions().INLINE_NEEDS_CALLER_FRAME && callNode.isInlinable()) {
            callNode.forceInlining();
        }
    }

    protected Object call(DirectCallNode callNode, VirtualFrame frame, InternalMethod method, Object receiver, DynamicObject block, Object[] arguments) {
        if (needsCallerAssumption == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            resetNeedsCallerAssumption();
        }
        try {
            needsCallerAssumption.check();
        } catch (InvalidAssumptionException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            resetNeedsCallerAssumption();
            reassessSplittingInliningStrategy();
        }

        final MaterializedFrame callerFrame = getFrameIfRequired(frame);
        return callNode.call(RubyArguments.pack(null, callerFrame, method, null, receiver, block, arguments));
    }

    private MaterializedFrame getFrameIfRequired(VirtualFrame frame) {
        switch (sendsFrame) {
            case MY_FRAME:
                return frame.materialize();
            case CALLER_FRAME:
                return readCaller.execute(frame);
            default:
                return null;
        }
    }
}
