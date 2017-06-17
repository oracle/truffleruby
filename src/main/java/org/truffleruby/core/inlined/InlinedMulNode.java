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
import org.truffleruby.core.numeric.FixnumNodes.MulNode;
import org.truffleruby.core.numeric.FixnumNodesFactory.MulNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedMulNode extends BinaryInlinedOperationNode {

    @Child MulNode fixnumMul;

    public InlinedMulNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters,
                context.getCoreMethods().fixnumMulAssumption,
                context.getCoreMethods().floatMulAssumption);
    }

    @Specialization(assumptions = "assumptions")
    Object intMul(int a, int b) {
        return getMulNode().executeMul(a, b);
    }

    @Specialization(assumptions = "assumptions")
    Object longMul(long a, long b) {
        return getMulNode().executeMul(a, b);
    }

    @Specialization(assumptions = "assumptions")
    double floatMul(double a, double b) {
        return a * b;
    }

    @Specialization
    Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    private MulNode getMulNode() {
        if (fixnumMul == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumMul = insert(MulNodeFactory.create(null));
        }
        return fixnumMul;
    }

}
