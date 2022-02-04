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
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public abstract class InlinedNotNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "!";

    public InlinedNotNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    @Specialization(
            guards = { "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().NOT", },
            assumptions = "assumptions",
            limit = "1")
    protected boolean not(VirtualFrame frame, Object self,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached BooleanCastNode booleanCastNode) {
        return !booleanCastNode.executeToBoolean(self);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

}
