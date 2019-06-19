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
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.InternalMethod;

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
        private final TruffleLanguage.ContextReference<RubyContext> contextReference = lookupContextReference(RubyLanguage.class);

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
            // FIXME (pitr 19-Jun-2019): this is probably not entirely correct, but works for now
            //  we need to migrate dispatch nodes to DSL and add @GenerateUncached instead
            RubyContext context = contextReference.get();
            DynamicObject metaClass = context.getCoreLibrary().getMetaClass(receiver);
            final InternalMethod method = ModuleOperations.lookupMethodUncached(metaClass, methodName, null);
            if (method != null && !method.isUndefined()) {
                return method.getCallTarget().call(RubyArguments.pack(
                        null, null, method, null, receiver, block, arguments));
            }

            final InternalMethod methodMissing = ModuleOperations.lookupMethodUncached(metaClass, "method_missing", null);
            if (methodMissing != null && !methodMissing.isUndefined()) {
                Object[] missingAruments = new Object[arguments.length + 1];
                System.arraycopy(arguments, 0, missingAruments, 1, arguments.length);
                missingAruments[0] = context.getSymbolTable().getSymbol(methodName);

                return methodMissing.getCallTarget().call(RubyArguments.pack(
                        null, null, methodMissing, null, receiver, block, missingAruments));
            }

            final DynamicObject formatter = ExceptionOperations.getFormatter(ExceptionOperations.NO_METHOD_ERROR, context);
            throw new RaiseException(context, context.getCoreExceptions().noMethodErrorFromMethodMissing(
                    formatter, receiver, methodName, arguments, this));
        }

        @Override
        public Object dispatch(VirtualFrame frame, Object receiverObject, Object methodName, DynamicObject blockObject, Object[] argumentsObjects) {
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

    private static final CallDispatchHeadNode UNCACHED_IGNORING_VISIBILITY =
            new Uncached(true, false, MissingBehavior.CALL_METHOD_MISSING);

    public static CallDispatchHeadNode getUncached() {
        return UNCACHED_IGNORING_VISIBILITY;
    }

}
