/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

public abstract class CachedDispatchNode extends DispatchNode {

    private final Object cachedName;

    @Child protected DispatchNode next;
    @Child private RopeNodes.BytesEqualNode ropeEqualsNode;

    private final BranchProfile moreThanReferenceCompare = BranchProfile.create();

    public CachedDispatchNode(
            RubyContext context,
            Object cachedName,
            DispatchNode next,
            DispatchAction dispatchAction) {
        super(dispatchAction);

        assert (cachedName instanceof String) || (RubyGuards.isRubySymbol(cachedName)) ||
                (RubyGuards.isRubyString(cachedName));
        this.cachedName = cachedName;

        if (RubyGuards.isRubyString(cachedName)) {
            ropeEqualsNode = RopeNodes.BytesEqualNode.create();
        }

        this.next = next;
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
            return RubyGuards.isRubyString(methodName) && ropeEqualsNode.execute(
                    StringOperations.rope((DynamicObject) cachedName),
                    StringOperations.rope((DynamicObject) methodName));
        } else { // cachedName is a Symbol
            // cachedName == methodName was checked above and was not true,
            // and since Symbols are compared by identity we know they don't match.
            // We also want to keep the fast-path identity comparison above for non-Symbols,
            // as it is more efficient than a full comparison.
            return false;
        }
    }

    public Object getCachedName() {
        return cachedName;
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

    protected Object call(DirectCallNode callNode, VirtualFrame frame, InternalMethod method, Object receiver,
            DynamicObject block, Object[] arguments) {
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
}
