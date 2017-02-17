/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.dispatch;

import static org.truffleruby.language.RubyGuards.isForeignObject;

import org.truffleruby.interop.OutgoingForeignCallNode;
import org.truffleruby.interop.OutgoingForeignCallNodeGen;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;

public final class CachedForeignDispatchNode extends CachedDispatchNode {

    private final String name;

    @Child private OutgoingForeignCallNode outgoingForeignCallNode;

    public CachedForeignDispatchNode(DispatchNode next, Object cachedName) {
        super(cachedName, next, DispatchAction.CALL_METHOD);
        name = cachedName.toString();
        outgoingForeignCallNode = OutgoingForeignCallNodeGen.create(name, null, null);
    }

    @Override
    protected boolean guard(Object methodName, Object receiver) {
        return guardName(methodName) && isForeignObject(receiver);
    }

    @Override
    public Object executeDispatch(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            DynamicObject blockObject,
            Object[] argumentsObjects) {
        if (guard(methodName, receiverObject)) {
            return doDispatch(frame, (TruffleObject) receiverObject, argumentsObjects);
        } else {
            return next.executeDispatch(frame, receiverObject, methodName, blockObject, argumentsObjects);
        }
    }

    private Object doDispatch(VirtualFrame frame, TruffleObject receiverObject, Object[] arguments) {
        return outgoingForeignCallNode.executeCall(frame, receiverObject, arguments);
    }

}
