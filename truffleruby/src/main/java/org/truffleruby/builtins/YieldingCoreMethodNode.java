/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.yield.YieldNode;

public abstract class YieldingCoreMethodNode extends CoreMethodArrayArgumentsNode {

    @Child private YieldNode dispatchNode = new YieldNode();
    @Child private BooleanCastNode booleanCastNode;

    public YieldingCoreMethodNode() {
        super();
    }

    public Object yield(DynamicObject block, Object... arguments) {
        return dispatchNode.dispatch(block, arguments);
    }

    public Object yieldWithBlock(DynamicObject block, DynamicObject blockArgument, Object... arguments) {
        return dispatchNode.dispatchWithBlock(block, blockArgument, arguments);
    }

    public boolean yieldIsTruthy(DynamicObject block, Object... arguments) {
        return booleanCast(yield(block, arguments));
    }

    private boolean booleanCast(Object value) {
        if (booleanCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            booleanCastNode = insert(BooleanCastNodeGen.create(null));
        }
        return booleanCastNode.executeToBoolean(value);
    }

}
