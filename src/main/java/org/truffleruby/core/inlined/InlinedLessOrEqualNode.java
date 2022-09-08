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
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedLessOrEqualNode extends BinaryInlinedOperationNode {

    public InlinedLessOrEqualNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerLessOrEqualAssumption,
                language.coreMethodAssumptions.floatLessOrEqualAssumption);
    }

    @Specialization(assumptions = "assumptions")
    protected boolean doInt(int a, int b) {
        return a <= b;
    }

    @Specialization(assumptions = "assumptions")
    protected boolean doLong(long a, long b) {
        return a <= b;
    }

    @Specialization(assumptions = "assumptions")
    protected boolean doDouble(double a, double b) {
        return a <= b;
    }

    @Specialization(assumptions = "assumptions")
    protected boolean longDouble(long a, double b) {
        return a <= b;
    }

    @Specialization(assumptions = "assumptions")
    protected boolean doubleLong(double a, long b) {
        return a <= b;
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    public RubyNode cloneUninitialized() {
        var copy = InlinedLessOrEqualNodeGen.create(
                getLanguage(),
                this.parameters,
                getLeftNode().cloneUninitialized(),
                getRightNode().cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
