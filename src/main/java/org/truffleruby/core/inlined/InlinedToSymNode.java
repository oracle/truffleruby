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

import org.truffleruby.RubyContext;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public abstract class InlinedToSymNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "to_sym";

    public InlinedToSymNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(context, callNodeParameters);
    }

    @Specialization(
            guards = "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().STRING_TO_SYM",
            assumptions = "assumptions",
            limit = "1")
    protected RubySymbol toSym(VirtualFrame frame, RubyString self,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached StringNodes.ToSymNode toSym) {
        return toSym.execute(self);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }
}
