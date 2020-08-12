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
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import org.truffleruby.core.proc.RubyProc;

public class DoesRespondDispatchHeadNode extends DispatchHeadNode {

    public static final DispatchConfiguration PRIVATE_DOES_RESPOND = DispatchConfiguration.PRIVATE_DOES_RESPOND;
    public static final DispatchConfiguration PUBLIC_DOES_RESPOND = DispatchConfiguration.PUBLIC_DOES_RESPOND;

    public static DoesRespondDispatchHeadNode create() {
        return create(PRIVATE_DOES_RESPOND);
    }

    public static DoesRespondDispatchHeadNode create(DispatchConfiguration config) {
        return new DoesRespondDispatchHeadNode(config);
    }

    @Child NewDispatchHeadNode newDispatch;

    private DoesRespondDispatchHeadNode(DispatchConfiguration config, NewDispatchHeadNode newDispatch) {
        super(config.ignoreVisibility, config.onlyLookupPublic, config.missingBehavior, config.dispatchAction);
        this.newDispatch = newDispatch;
    }

    private DoesRespondDispatchHeadNode(DispatchConfiguration config) {
        this(config, NewDispatchHeadNode.create(config));
    }

    /** Check if a specific method is defined on the receiver object. This check is "static" and should only be used in
     * a few VM operations. In many cases, a dynamic call to Ruby's respond_to? should be used instead. Similar to MRI
     * rb_check_funcall(). */
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
                EMPTY_ARGUMENTS);
    }

    @Override
    public Object dispatch(VirtualFrame frame, Object receiverObject, Object methodName, RubyProc blockObject,
            Object[] argumentsObjects) {
        return newDispatch.execute(frame, receiverObject, methodName, blockObject, argumentsObjects);
    }

    public static DoesRespondDispatchHeadNode getUncached() {
        return Uncached.UNCACHED_PRIVATE;
    }

    public static DoesRespondDispatchHeadNode getUncached(DispatchConfiguration config) {
        switch (config) {
            case PUBLIC_DOES_RESPOND:
                return Uncached.UNCACHED_PUBLIC;
            case PRIVATE_DOES_RESPOND:
                return Uncached.UNCACHED_PRIVATE;
            default:
                throw CompilerDirectives.shouldNotReachHere();
        }
    }

    private static class Uncached extends DoesRespondDispatchHeadNode {

        private static final Uncached UNCACHED_PRIVATE = new Uncached(DispatchConfiguration.PRIVATE_DOES_RESPOND);
        private static final Uncached UNCACHED_PUBLIC = new Uncached(DispatchConfiguration.PUBLIC_DOES_RESPOND);

        Uncached(DispatchConfiguration config) {
            super(config, NewDispatchHeadNode.getUncached(config));
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
