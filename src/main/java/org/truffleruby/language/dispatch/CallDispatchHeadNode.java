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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class CallDispatchHeadNode extends DispatchHeadNode {

    /** Create a dispatch node ignoring visibility. */
    public static CallDispatchHeadNode createOnSelf() {
        return new CallDispatchHeadNode(true, false, MissingBehavior.CALL_METHOD_MISSING);
    }

    public static CallDispatchHeadNode createCallPublicOnly() {
        return new CallDispatchHeadNode(false, true, MissingBehavior.CALL_METHOD_MISSING);
    }

    public static CallDispatchHeadNode createReturnMissing() {
        return new CallDispatchHeadNode(true, false, MissingBehavior.RETURN_MISSING);
    }

    CallDispatchHeadNode(boolean ignoreVisibility, boolean onlyCallPublic, MissingBehavior missingBehavior) {
        super(ignoreVisibility, onlyCallPublic, missingBehavior, DispatchAction.CALL_METHOD);
    }

    public Object call(VirtualFrame frame, Object receiver, Object method, Object... arguments) {
        return dispatch(null, receiver, method, null, arguments);
    }

    public Object callWithBlock(
            VirtualFrame frame,
            Object receiver,
            Object method,
            DynamicObject block,
            Object... arguments) {
        return dispatch(null, receiver, method, block, arguments);
    }

}
