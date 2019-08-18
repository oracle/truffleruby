/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild(value = "block", type = RubyNode.class)
public abstract class InlinedLambdaNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "lambda";

    public InlinedLambdaNode(RubyContext context, RubyCallNodeParameters callNodeParameters) {
        super(callNodeParameters);
    }

    @Specialization(guards = {
            "lookupNode.lookupIgnoringVisibility(frame, self, METHOD) == coreMethods().LAMBDA",
    }, assumptions = "assumptions", limit = "1")
    protected DynamicObject lambda(VirtualFrame frame, Object self, DynamicObject block,
            @Cached LookupMethodNode lookupNode) {
        return ProcOperations.createLambdaFromBlock(getContext(), block);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self, DynamicObject block) {
        return rewriteAndCallWithBlock(frame, self, block);
    }

    protected abstract RubyNode getBlock();

    @Override
    protected RubyNode getBlockNode() {
        return getBlock();
    }

}
