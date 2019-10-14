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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class UncachedDispatchNode extends DispatchNode {

    private final MissingBehavior missingBehavior;
    private final boolean ignoreVisibility;
    private final boolean onlyCallPublic;

    @Child private DSLUncachedDispatchNode dslUncachedDispatchNode = DSLUncachedDispatchNode.create();

    public UncachedDispatchNode(
            boolean ignoreVisibility,
            boolean onlyCallPublic,
            DispatchAction dispatchAction,
            MissingBehavior missingBehavior) {
        super(dispatchAction);
        this.missingBehavior = missingBehavior;
        this.ignoreVisibility = ignoreVisibility;
        this.onlyCallPublic = onlyCallPublic;
    }

    @Override
    protected boolean guard(Object methodName, Object receiver) {
        return true;
    }

    @Override
    public Object executeDispatch(
            VirtualFrame frame,
            Object receiver,
            Object name,
            DynamicObject block,
            Object[] arguments) {

        return dslUncachedDispatchNode.dispatch(
                frame,
                receiver,
                name,
                block,
                arguments,
                getDispatchAction(),
                missingBehavior,
                ignoreVisibility,
                onlyCallPublic);
    }

}
