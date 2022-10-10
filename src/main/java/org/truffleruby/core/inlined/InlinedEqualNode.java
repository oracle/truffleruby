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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public abstract class InlinedEqualNode extends BinaryInlinedOperationNode {

    protected static final String METHOD = "==";

    final Assumption integerEqualAssumption;
    final Assumption floatEqualAssumption;

    public InlinedEqualNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
        this.integerEqualAssumption = language.coreMethodAssumptions.integerEqualAssumption;
        this.floatEqualAssumption = language.coreMethodAssumptions.floatEqualAssumption;
    }

    @Specialization(assumptions = { "assumptions", "integerEqualAssumption" })
    protected boolean intEqual(int a, int b) {
        return a == b;
    }

    @Specialization(assumptions = { "assumptions", "integerEqualAssumption" })
    protected boolean longEqual(long a, long b) {
        return a == b;
    }

    @Specialization(assumptions = { "assumptions", "floatEqualAssumption" })
    protected boolean doDouble(double a, double b) {
        return a == b;
    }

    @Specialization(assumptions = { "assumptions", "integerEqualAssumption" })
    protected boolean longDouble(long a, double b) {
        return a == b;
    }

    @Specialization(assumptions = { "assumptions", "floatEqualAssumption" })
    protected boolean doubleLong(double a, long b) {
        return a == b;
    }

    @Specialization(
            guards = {
                    "libA.isRubyString(a)",
                    "libB.isRubyString(b)",
                    "lookupNode.lookupProtected(frame, a, METHOD) == coreMethods().STRING_EQUAL"
            },
            assumptions = "assumptions", limit = "1")
    protected boolean stringEqual(VirtualFrame frame, Object a, Object b,
            @Cached RubyStringLibrary libA,
            @Cached RubyStringLibrary libB,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached EncodingNodes.NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
            @Cached StringHelperNodes.StringEqualInternalNode stringEqualInternalNode) {
        var tstringA = libA.getTString(a);
        var encA = libA.getEncoding(a);
        var tstringB = libB.getTString(b);
        var encB = libB.getEncoding(b);
        var compatibleEncoding = negotiateCompatibleStringEncodingNode.execute(tstringA, encA, tstringB, encB);
        return stringEqualInternalNode.executeInternal(tstringA, tstringB, compatibleEncoding);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = InlinedEqualNodeGen.create(
                getLanguage(),
                this.parameters,
                getLeftNode().cloneUninitialized(),
                getRightNode().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
