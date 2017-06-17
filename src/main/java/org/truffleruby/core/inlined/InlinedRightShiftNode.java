/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.core.numeric.FixnumNodes.RightShiftNode;
import org.truffleruby.core.numeric.FixnumNodesFactory.RightShiftNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedRightShiftNode extends BinaryInlinedOperationNode {

    @Child RightShiftNode fixnumRightShift;

    public InlinedRightShiftNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters,
                context.getCoreMethods().fixnumRightShiftAssumption);
    }

    @Specialization(assumptions = "assumptions")
    Object intRightShift(int a, int b) {
        return getRightShiftNode().executeRightShift(a, b);
    }

    @Specialization(assumptions = "assumptions")
    Object longRightShift(long a, int b) {
        return getRightShiftNode().executeRightShift(a, b);
    }

    @Specialization
    Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    private RightShiftNode getRightShiftNode() {
        if (fixnumRightShift == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumRightShift = insert(RightShiftNodeFactory.create(null));
        }
        return fixnumRightShift;
    }

}
