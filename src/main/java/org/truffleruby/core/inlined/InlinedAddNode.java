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
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

/** See {@link org.truffleruby.core.numeric.IntegerNodes.AddNode} and
 * {@link org.truffleruby.core.numeric.FloatNodes.AddNode} */
public abstract class InlinedAddNode extends BinaryInlinedOperationNode {

    public InlinedAddNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerAddAssumption,
                language.coreMethodAssumptions.floatAddAssumption);
    }

    @Specialization(rewriteOn = ArithmeticException.class, assumptions = "assumptions")
    protected int intAdd(int a, int b) {
        return Math.addExact(a, b);
    }

    @Specialization(assumptions = "assumptions")
    protected long intAddWithOverflow(int a, int b) {
        return (long) a + (long) b;
    }

    @Specialization(rewriteOn = ArithmeticException.class, assumptions = "assumptions")
    protected long longAdd(long a, long b) {
        return Math.addExact(a, b);
    }

    @Specialization(assumptions = "assumptions")
    protected Object longAddWithOverflow(long a, long b,
            @Cached FixnumOrBignumNode fixnumOrBignum) {
        return fixnumOrBignum.fixnumOrBignum(BigIntegerOps.add(a, b));
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
}
