/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;
import org.truffleruby.language.objects.IsANode;

public abstract class InlinedCaseEqualNode extends BinaryInlinedBooleanNode {

    protected static final String METHOD = "===";

    final Assumption integerCaseEqualAssumption;

    public InlinedCaseEqualNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
        this.integerCaseEqualAssumption = language.coreMethodAssumptions.integerCaseEqualAssumption;
    }

    @Specialization(assumptions = { "assumptions", "integerCaseEqualAssumption" })
    protected boolean intCaseEqual(int a, int b) {
        return a == b;
    }

    @Specialization(assumptions = { "assumptions", "integerCaseEqualAssumption" })
    protected boolean longCaseEqual(long a, long b) {
        return a == b;
    }

    @Specialization(
            guards = {
                    "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().MODULE_CASE_EQUAL"
            },
            assumptions = "assumptions",
            limit = "1")
    protected boolean doModule(VirtualFrame frame, RubyModule self, Object b,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached IsANode isANode) {
        return isANode.executeIsA(b, self);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self, Object b) {
        return rewriteAndCall(frame, self, b);
    }

}
