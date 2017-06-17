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
import org.truffleruby.core.numeric.FixnumNodes.SubNode;
import org.truffleruby.core.numeric.FixnumNodesFactory.SubNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedSubNode extends BinaryInlinedOperationNode {

    @Child SubNode fixnumSub;

    public InlinedSubNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters,
                context.getCoreMethods().fixnumSubAssumption,
                context.getCoreMethods().floatSubAssumption);
    }

    @Specialization(assumptions = "assumptions")
    Object intSub(int a, int b) {
        return getSubNode().executeSub(a, b);
    }

    @Specialization(assumptions = "assumptions")
    Object longSub(long a, long b) {
        return getSubNode().executeSub(a, b);
    }

    @Specialization(assumptions = "assumptions")
    double floatSub(double a, double b) {
        return a - b;
    }

    @Specialization
    Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    private SubNode getSubNode() {
        if (fixnumSub == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumSub = insert(SubNodeFactory.create(null));
        }
        return fixnumSub;
    }

}
