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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.StringNodes;
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
                    "stringsSelf.isRubyString(self)",
                    "stringsB.isRubyString(b)",
                    "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().STRING_EQUAL"
            },
            assumptions = "assumptions")
    protected boolean stringEqual(VirtualFrame frame, Object self, Object b,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsSelf,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsB,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached StringNodes.StringEqualNode stringEqualNode) {
        return stringEqualNode.executeStringEqual(stringsSelf.getRope(self), stringsB.getRope(b));
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

}
