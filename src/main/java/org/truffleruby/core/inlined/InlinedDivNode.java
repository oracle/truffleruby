/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedDivNode extends BinaryInlinedOperationNode {

    public InlinedDivNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerDivAssumption,
                language.coreMethodAssumptions.floatDivAssumption);
    }

    // We only handle the simplest and most common case for integer division.
    // We need to avoid the / 0 case as it would give a wrong Ruby backtrace.

    @Specialization(guards = { "a >= 0", "b > 0" }, assumptions = "assumptions")
    protected int intDiv(int a, int b) {
        return a / b;
    }

    @Specialization(guards = { "a >= 0", "b > 0" }, assumptions = "assumptions")
    protected long longDiv(long a, long b) {
        return a / b;
    }

    @Specialization(assumptions = "assumptions")
    protected double floatDiv(double a, double b) {
        return a / b;
    }

    @Specialization(assumptions = "assumptions")
    protected double longDouble(long a, double b) {
        return a / b;
    }

    @Specialization(assumptions = "assumptions")
    protected double doubleLong(double a, long b) {
        return a / b;
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = InlinedDivNodeGen.create(
                getLanguage(),
                this.parameters,
                getLeftNode().cloneUninitialized(),
                getRightNode().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
