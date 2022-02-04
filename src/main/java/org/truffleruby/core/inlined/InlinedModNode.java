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
import org.truffleruby.core.numeric.FloatNodes;
import org.truffleruby.core.numeric.IntegerNodes.ModNode;
import org.truffleruby.core.numeric.IntegerNodesFactory.ModNodeFactory;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedModNode extends BinaryInlinedOperationNode {

    @Child ModNode fixnumMod;

    public InlinedModNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(
                language,
                callNodeParameters,
                language.coreMethodAssumptions.integerModAssumption,
                language.coreMethodAssumptions.floatModAssumption);
    }

    // We need to avoid the % 0 case as it would give a wrong Ruby backtrace.

    @Specialization(guards = "b != 0", assumptions = "assumptions")
    protected Object intMod(int a, int b) {
        return getModNode().executeMod(a, b);
    }

    @Specialization(guards = "b != 0", assumptions = "assumptions")
    protected Object longMod(long a, long b) {
        return getModNode().executeMod(a, b);
    }

    protected static final double ZERO = 0.0;

    @Specialization(guards = "b != ZERO", assumptions = "assumptions")
    protected Object floatMod(double a, double b,
            @Cached FloatNodes.ModNode modNode) {
        return modNode.executeMod(a, b);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    private ModNode getModNode() {
        if (fixnumMod == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fixnumMod = insert(ModNodeFactory.create(null));
        }
        return fixnumMod;
    }

}
