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
import org.truffleruby.core.numeric.IntegerNodes.AddNode;
import org.truffleruby.core.numeric.IntegerNodesFactory.AddNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedAddNode extends BinaryInlinedOperationNode {

    @Child AddNode fixnumAdd;

    public InlinedAddNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerAddAssumption,
                language.coreMethodAssumptions.floatAddAssumption);
    }

    @Specialization(assumptions = "assumptions")
    protected Object intAdd(int a, int b) {
        return getAddNode().executeAdd(a, b);
    }

    @Specialization(assumptions = "assumptions")
    protected Object longAdd(long a, long b) {
        return getAddNode().executeAdd(a, b);
    }

    @Specialization(assumptions = "assumptions")
    protected double floatAdd(double a, double b) {
        return a + b;
    }

    @Specialization(assumptions = "assumptions")
    protected double longDouble(long a, double b) {
        return a + b;
    }

    @Specialization(assumptions = "assumptions")
    protected double doubleLong(double a, long b) {
        return a + b;
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    private AddNode getAddNode() {
        if (fixnumAdd == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumAdd = insert(AddNodeFactory.create(null));
        }
        return fixnumAdd;
    }

}
