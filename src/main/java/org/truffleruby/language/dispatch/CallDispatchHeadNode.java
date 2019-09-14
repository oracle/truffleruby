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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;

public class CallDispatchHeadNode extends DispatchHeadNode {

    /**
     * Create a dispatch node ignoring visibility. This is the case for most calls from Java nodes
     * and from the C-API, as checking visibility doesn't make much sense in this context and MRI
     * doesn't do it either.
     */
    public static CallDispatchHeadNode createPrivate() {
        return new CallDispatchHeadNode(true, false, MissingBehavior.CALL_METHOD_MISSING);
    }

    /**
     * Create a dispatch node only allowed to call public methods. This is rather rare.
     */
    public static CallDispatchHeadNode createPublic() {
        return new CallDispatchHeadNode(false, true, MissingBehavior.CALL_METHOD_MISSING);
    }

    public static CallDispatchHeadNode createReturnMissing() {
        return new CallDispatchHeadNode(true, false, MissingBehavior.RETURN_MISSING);
    }

    CallDispatchHeadNode(boolean ignoreVisibility, boolean onlyCallPublic, MissingBehavior missingBehavior) {
        super(ignoreVisibility, onlyCallPublic, missingBehavior, DispatchAction.CALL_METHOD);
    }

    public Object call(Object receiver, String method, Object... arguments) {
        return dispatch(null, receiver, method, null, arguments);
    }

    public Object callWithBlock(Object receiver, String method, DynamicObject block, Object... arguments) {
        return dispatch(null, receiver, method, block, arguments);
    }

    private static class Uncached extends CallDispatchHeadNode {
        Uncached(boolean ignoreVisibility, boolean onlyCallPublic, MissingBehavior missingBehavior) {
            super(ignoreVisibility, onlyCallPublic, missingBehavior);
        }

        @Override
        public Object call(Object receiver, String method, Object... arguments) {
            return callWithBlock(receiver, method, null, arguments);
        }

        @Override
        @TruffleBoundary
        public Object callWithBlock(Object receiver, String methodName, DynamicObject block, Object... arguments) {
            return DSLUncachedDispatchNodeGen.getUncached().dispatch(
                    null,
                    receiver,
                    methodName,
                    block,
                    arguments,
                    DispatchAction.CALL_METHOD,
                    MissingBehavior.CALL_METHOD_MISSING,
                    true,
                    false);
        }

        @Override
        public Object dispatch(VirtualFrame frame, Object receiverObject, Object methodName, DynamicObject blockObject,
                Object[] argumentsObjects) {
            throw new AssertionError("never called");
        }

        @Override
        public void reset(String reason) {
            throw new AssertionError("never called");
        }

        @Override
        public DispatchNode getFirstDispatchNode() {
            throw new AssertionError("never called");
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @Override
        public boolean isAdoptable() {
            return false;
        }
    }

    private static final CallDispatchHeadNode UNCACHED_IGNORING_VISIBILITY = new Uncached(
            true,
            false,
            MissingBehavior.CALL_METHOD_MISSING);

    // FIXME (pitr 29-Jul-2019): this only matches common createPrivate
    public static CallDispatchHeadNode getUncached() {
        return UNCACHED_IGNORING_VISIBILITY;
    }

}
