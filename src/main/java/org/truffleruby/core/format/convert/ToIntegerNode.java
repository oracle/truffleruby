/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import org.truffleruby.core.format.FormatNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("value")
public abstract class ToIntegerNode extends FormatNode {

    @Child private CallDispatchHeadNode integerNode;

    public abstract Object executeToInteger(VirtualFrame frame, Object object);

    @Specialization
    protected int toInteger(int value) {
        return value;
    }

    @Specialization
    protected long toInteger(long value) {
        return value;
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected DynamicObject toInteger(DynamicObject value) {
        return value;
    }

    @Specialization
    protected long toInteger(double value) {
        return (long) value;
    }

    @Specialization(guards = { "!isInteger(value)", "!isLong(value)", "!isRubyBignum(value)" })
    protected Object toInteger(VirtualFrame frame, Object value) {
        if (integerNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            integerNode = insert(CallDispatchHeadNode.createPrivate());
        }

        return integerNode.call(getContext().getCoreLibrary().getKernelModule(), "Integer", value);
    }

}
