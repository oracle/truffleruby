/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedBlockGivenNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "block_given?";

    public InlinedBlockGivenNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters);
    }

    @Specialization(guards = {
            "lookupNode.lookup(frame, self, METHOD) == coreMethods().BLOCK_GIVEN",
    }, assumptions = "assumptions", limit = "1")
    boolean blockGiven(VirtualFrame frame, Object self,
            @Cached("createIgnoreVisibility()") LookupMethodNode lookupNode) {
        return RubyArguments.getBlock(frame) != null;
    }

    @Specialization
    Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

}
