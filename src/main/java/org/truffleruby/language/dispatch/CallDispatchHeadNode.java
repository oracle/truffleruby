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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import org.truffleruby.core.proc.RubyProc;

public class CallDispatchHeadNode extends DispatchHeadNode {

    public static final byte PRIVATE = 0b10;
    public static final byte PUBLIC = 0b00;
    public static final byte RETURN_MISSING = 0b11;
    public static final byte PUBLIC_RETURN_MISSING = 0b01;

    public static CallDispatchHeadNode create() {
        return create(PRIVATE);
    }

    public static CallDispatchHeadNode create(byte configuration) {
        switch (configuration) {
            case PRIVATE:
                return createPrivate();
            case PUBLIC:
                return createPublic();
            case RETURN_MISSING:
                return createReturnMissing();
            case PUBLIC_RETURN_MISSING:
                return new CallDispatchHeadNode(false, true, MissingBehavior.RETURN_MISSING);
            default:
                throw new IllegalStateException("Unexpected value: " + configuration);
        }
    }

    /** Create a dispatch node ignoring visibility. This is the case for most calls from Java nodes and from the C-API,
     * as checking visibility doesn't make much sense in this context and MRI doesn't do it either. */
    public static CallDispatchHeadNode createPrivate() {
        return new CallDispatchHeadNode(true, false, MissingBehavior.CALL_METHOD_MISSING);
    }

    /** Create a dispatch node only allowed to call public methods. This is rather rare. */
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

    public Object callWithBlock(Object receiver, String method, RubyProc block, Object... arguments) {
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
        public Object callWithBlock(Object receiver, String methodName, RubyProc block, Object... arguments) {
            return DSLUncachedDispatchNodeGen.getUncached().dispatch(
                    null,
                    receiver,
                    methodName,
                    null,
                    block,
                    arguments,
                    this.dispatchAction,
                    this.missingBehavior,
                    true,
                    false);
        }

        @Override
        public Object dispatch(VirtualFrame frame, Object receiverObject, Object methodName, RubyProc blockObject,
                Object[] argumentsObjects) {
            throw CompilerDirectives.shouldNotReachHere();

        }

        @Override
        public void reset(String reason) {
            throw CompilerDirectives.shouldNotReachHere();

        }

        @Override
        public DispatchNode getFirstDispatchNode() {
            throw CompilerDirectives.shouldNotReachHere();
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

    private static final CallDispatchHeadNode UNCACHED_PRIVATE = new Uncached(
            true,
            false,
            MissingBehavior.CALL_METHOD_MISSING);
    private static final CallDispatchHeadNode UNCACHED_PUBLIC = new Uncached(
            false,
            true,
            MissingBehavior.CALL_METHOD_MISSING);

    private static final CallDispatchHeadNode UNCACHED_RETURN_MISSING = new Uncached(
            true,
            false,
            MissingBehavior.RETURN_MISSING);

    private static final CallDispatchHeadNode UNCACHED_PUBLIC_RETURN_MISSING = new Uncached(
            false,
            true,
            MissingBehavior.RETURN_MISSING);

    public static CallDispatchHeadNode getUncached() {
        return UNCACHED_PRIVATE;
    }

    public static CallDispatchHeadNode getUncached(byte configuration) {
        switch (configuration) {
            case PRIVATE:
                return UNCACHED_PRIVATE;
            case PUBLIC:
                return UNCACHED_PUBLIC;
            case RETURN_MISSING:
                return UNCACHED_RETURN_MISSING;
            case PUBLIC_RETURN_MISSING:
                return UNCACHED_PUBLIC_RETURN_MISSING;
            default:
                throw unexpectedConfiguration("Unexpected value: " + configuration);
        }
    }

    @TruffleBoundary
    private static IllegalStateException unexpectedConfiguration(String msg) {
        return new IllegalStateException(msg);
    }
}
