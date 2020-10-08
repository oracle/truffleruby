/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

@NodeChild(value = "block", type = RubyNode.class)
public abstract class InlinedLambdaNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "lambda";

    public InlinedLambdaNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    @Specialization(
            guards = { "lookupNode.lookupIgnoringVisibility(frame, self, METHOD) == coreMethods().LAMBDA", },
            assumptions = "assumptions",
            limit = "1")
    protected RubyProc lambda(VirtualFrame frame, Object self, RubyProc block,
            @Cached LookupMethodOnSelfNode lookupNode) {
        return ProcOperations.createLambdaFromBlock(getContext(), block);
    }

    // The lambda method might have been overriden, undefined, redefined, ...
    @Specialization
    protected Object fallback(VirtualFrame frame, Object self, RubyProc block) {
        return rewriteAndCallWithBlock(frame, self, block);
    }

    protected abstract RubyNode getBlock();

    @Override
    protected RubyNode getBlockNode() {
        return getBlock();
    }
}
