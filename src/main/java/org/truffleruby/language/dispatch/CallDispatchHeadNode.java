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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;

public class CallDispatchHeadNode extends DispatchHeadNode {

    @Child private BooleanCastNode booleanCastNode;

    public static CallDispatchHeadNode create() {
        return new CallDispatchHeadNode(
                false, false, MissingBehavior.CALL_METHOD_MISSING);
    }

    /** Create a dispatch node ignoring visibility. */
    public static CallDispatchHeadNode createOnSelf() {
        return new CallDispatchHeadNode(
                true, false, MissingBehavior.CALL_METHOD_MISSING);
    }

    public static CallDispatchHeadNode createCallPublicOnly() {
        return new CallDispatchHeadNode(false, true, MissingBehavior.CALL_METHOD_MISSING);
    }

    public static CallDispatchHeadNode createReturnMissing() {
        return new CallDispatchHeadNode(true, false, MissingBehavior.RETURN_MISSING);
    }

    private CallDispatchHeadNode(boolean ignoreVisibility, boolean onlyCallPublic, MissingBehavior missingBehavior) {
        super(ignoreVisibility, onlyCallPublic, missingBehavior, DispatchAction.CALL_METHOD);
    }

    public Object call(VirtualFrame frame, Object receiver, Object method, Object... arguments) {
        return dispatch(frame, receiver, method, null, arguments);
    }

    public Object callWithBlock(
            VirtualFrame frame,
            Object receiver,
            Object method,
            DynamicObject block,
            Object... arguments) {
        return dispatch(frame, receiver, method, block, arguments);
    }

    public boolean callBoolean(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            Object... argumentsObjects) {
        if (booleanCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            booleanCastNode = insert(BooleanCastNodeGen.create(null));
        }
        return booleanCastNode.executeToBoolean(call(frame, receiverObject, methodName, argumentsObjects));
    }

}
