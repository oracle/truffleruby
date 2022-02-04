/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.numeric.IntegerNodes.NegNode;
import org.truffleruby.core.numeric.IntegerNodesFactory.NegNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedNegNode extends UnaryInlinedOperationNode {

    @Child NegNode fixnumNeg;

    public InlinedNegNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerNegAssumption,
                language.coreMethodAssumptions.floatNegAssumption);
    }

    @Specialization(assumptions = "assumptions")
    protected Object intNeg(int value) {
        return getNegNode().executeNeg(value);
    }

    @Specialization(assumptions = "assumptions")
    protected Object longNeg(long value) {
        return getNegNode().executeNeg(value);
    }

    @Specialization(assumptions = "assumptions")
    protected double floatNeg(double value) {
        return -value;
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

    private NegNode getNegNode() {
        if (fixnumNeg == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumNeg = insert(NegNodeFactory.create(null));
        }
        return fixnumNeg;
    }

}
