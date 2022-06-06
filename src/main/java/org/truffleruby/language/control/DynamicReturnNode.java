/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class DynamicReturnNode extends RubyContextSourceNode {

    public final ReturnID returnID;

    @Child public RubyNode value;

    public DynamicReturnNode(ReturnID returnID, RubyNode value) {
        this.returnID = returnID;
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object returned = value.execute(frame);

        if (Nil.is(returned)) {
            ((Nil) returned).trace(this, "returned");
        }

        throw new DynamicReturnException(returnID, returned);
    }

    @Override
    public boolean isContinuable() {
        return false;
    }
}
