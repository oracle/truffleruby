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

    public static final DispatchConfiguration PRIVATE = DispatchConfiguration.PRIVATE;
    public static final DispatchConfiguration PUBLIC = DispatchConfiguration.PUBLIC;
    public static final DispatchConfiguration PRIVATE_RETURN_MISSING = DispatchConfiguration.PRIVATE_RETURN_MISSING;
    public static final DispatchConfiguration PUBLIC_RETURN_MISSING = DispatchConfiguration.PUBLIC_RETURN_MISSING;

    public static CallDispatchHeadNode create() {
        return createPrivate();
    }

    public static CallDispatchHeadNode create(DispatchConfiguration config) {
        return new CallDispatchHeadNode(config);
    }

    /** Create a dispatch node ignoring visibility. This is the case for most calls from Java nodes and from the C-API,
     *  as checking visibility doesn't make much sense in this context and MRI doesn't do it either. */
    public static CallDispatchHeadNode createPrivate() {
        return new CallDispatchHeadNode(PRIVATE);
    }

    /** Create a dispatch node only allowed to call public methods. This is rather rare. */
    public static CallDispatchHeadNode createPublic() {
        return new CallDispatchHeadNode(PUBLIC);
    }

    public static CallDispatchHeadNode createReturnMissing() {
        return new CallDispatchHeadNode(PRIVATE_RETURN_MISSING);
    }

    @Child NewDispatchHeadNode newDispatch;

    private CallDispatchHeadNode(DispatchConfiguration config, NewDispatchHeadNode newDispatch) {
        super(config.ignoreVisibility, config.onlyLookupPublic, config.missingBehavior, config.dispatchAction);
        this.newDispatch = newDispatch;
    }

    private CallDispatchHeadNode(DispatchConfiguration config) {
        this(config, NewDispatchHeadNode.create(config));
    }

    public Object call(Object receiver, String method, Object... arguments) {
        return dispatch(null, receiver, method, null, arguments);
    }

    public Object callWithBlock(Object receiver, String method, RubyProc block, Object... arguments) {
        return dispatch(null, receiver, method, block, arguments);
    }

    @Override
    public Object dispatch(VirtualFrame frame, Object receiverObject, Object methodName, RubyProc blockObject,
            Object[] argumentsObjects) {
        return newDispatch.execute(frame, receiverObject, methodName, blockObject, argumentsObjects);
    }

    public static CallDispatchHeadNode getUncached() {
        return Uncached.UNCACHED_PRIVATE;
    }

    public static CallDispatchHeadNode getUncached(DispatchConfiguration config) {
        switch (config) {
            case PRIVATE:
                return Uncached.UNCACHED_PRIVATE;
            case PUBLIC:
                return Uncached.UNCACHED_PUBLIC;
            case PRIVATE_RETURN_MISSING:
                return Uncached.UNCACHED_PRIVATE_RETURN_MISSING;
            case PUBLIC_RETURN_MISSING:
                return Uncached.UNCACHED_PUBLIC_RETURN_MISSING;
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }
    }

    @TruffleBoundary
    private static IllegalStateException unexpectedConfiguration(String msg) {
        return new IllegalStateException(msg);
    }

    private static class Uncached extends CallDispatchHeadNode {

        private static final Uncached UNCACHED_PRIVATE = new Uncached(PRIVATE);
        private static final Uncached UNCACHED_PUBLIC = new Uncached(PUBLIC);
        private static final Uncached UNCACHED_PRIVATE_RETURN_MISSING = new Uncached(PRIVATE_RETURN_MISSING);
        private static final CallDispatchHeadNode UNCACHED_PUBLIC_RETURN_MISSING = new Uncached(PUBLIC_RETURN_MISSING);

        public static CallDispatchHeadNode getUncached() {
            return UNCACHED_PRIVATE;
        }

        Uncached(DispatchConfiguration config) {
            super(config, NewDispatchHeadNode.getUncached(config));
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
}
