/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.interop.ToJavaStringNodeGen;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.InternalMethod;

public class DoesRespondDispatchHeadNode extends DispatchHeadNode {

    public static DoesRespondDispatchHeadNode create() {
        return new DoesRespondDispatchHeadNode(true);
    }

    public static DoesRespondDispatchHeadNode createPublic() {
        return new DoesRespondDispatchHeadNode(false);
    }

    private DoesRespondDispatchHeadNode(boolean ignoreVisibility) {
        super(ignoreVisibility, !ignoreVisibility, MissingBehavior.RETURN_MISSING, DispatchAction.RESPOND_TO_METHOD);
    }

    /**
     * Check if a specific method is defined on the receiver object.
     * This check is "static" and should only be used in a few VM operations.
     * In many cases, a dynamic call to Ruby's respond_to? should be used instead.
     * Similar to MRI rb_check_funcall().
     */
    public boolean doesRespondTo(
            VirtualFrame frame,
            Object methodName,
            Object receiverObject) {
        // It's ok to cast here as we control what RESPOND_TO_METHOD returns
        return (boolean) dispatch(
                frame,
                receiverObject,
                methodName,
                null,
                RubyNode.EMPTY_ARGUMENTS);
    }

    private static class Uncached extends DoesRespondDispatchHeadNode {
        private final TruffleLanguage.ContextReference<RubyContext> contextReference = lookupContextReference(
                RubyLanguage.class);

        Uncached(boolean ignoreVisibility) {
            super(ignoreVisibility);
        }

        @Override
        public boolean doesRespondTo(VirtualFrame frame, Object name, Object receiver) {
            // FIXME (pitr 19-Jun-2019): this is probably not entirely correct, but works for now
            //  we need to migrate dispatch nodes to DSL and add @GenerateUncached instead
            boolean result = respondTo(name, receiver);
            boolean check = create().doesRespondTo(frame, name, receiver);
            assert result == check;
            return result;
        }

        @TruffleBoundary
        protected boolean respondTo(Object name, Object receiver) {
            RubyContext context = contextReference.get();
            String methodName = ToJavaStringNodeGen.getUncached().executeToJavaString(name);
            DynamicObject metaClass = context.getCoreLibrary().getMetaClass(receiver);
            final InternalMethod method = ModuleOperations.lookupMethodUncached(metaClass, methodName, null);
            return method != null && !method.isUndefined();
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

    private static final Uncached UNCACHED_IGNORING_VISIBILITY = new Uncached(true);

    public static DoesRespondDispatchHeadNode getUncached() {
        return UNCACHED_IGNORING_VISIBILITY;
        //return create();
    }

}
