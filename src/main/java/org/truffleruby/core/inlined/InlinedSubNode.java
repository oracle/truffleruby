/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.numeric.IntegerNodes.SubNode;
import org.truffleruby.core.numeric.IntegerNodesFactory.SubNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedSubNode extends BinaryInlinedOperationNode {

    @Child SubNode fixnumSub;

    public InlinedSubNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerSubAssumption,
                language.coreMethodAssumptions.floatSubAssumption);
    }

    @Specialization(assumptions = "assumptions")
    protected Object intSub(int a, int b) {
        return getSubNode().executeSub(a, b);
    }

    @Specialization(assumptions = "assumptions")
    protected Object longSub(long a, long b) {
        return getSubNode().executeSub(a, b);
    }

    @Specialization(assumptions = "assumptions")
    protected double floatSub(double a, double b) {
        return a - b;
    }

    @Specialization(assumptions = "assumptions")
    protected double longDouble(long a, double b) {
        return a - b;
    }

    @Specialization(assumptions = "assumptions")
    protected double doubleLong(double a, long b) {
        return a - b;
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
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
