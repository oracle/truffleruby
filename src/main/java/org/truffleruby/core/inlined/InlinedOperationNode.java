/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeUtil;

public abstract class InlinedOperationNode extends RubyContextSourceNode {

    private final RubyCallNodeParameters callNodeParameters;

    protected final Assumption[] assumptions;

    private RubyCallNode replacedBy = null;

    public InlinedOperationNode(
            RubyLanguage language,
            RubyCallNodeParameters callNodeParameters,
            Assumption... assumptions) {
        this.callNodeParameters = callNodeParameters;

        this.assumptions = new Assumption[1 + assumptions.length];
        this.assumptions[0] = language.traceFuncUnusedAssumption.getAssumption();
        ArrayUtils.arraycopy(assumptions, 0, this.assumptions, 1, assumptions.length);
    }

    protected abstract RubyNode getReceiverNode();

    protected abstract RubyNode[] getArgumentNodes();

    protected RubyNode getBlockNode() {
        return callNodeParameters.getBlock();
    }

    private RubyCallNode rewriteToCallNode() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return atomic(() -> {
            // Check if we are still in the AST
            boolean found = !NodeUtil.forEachChild(getParent(), node -> node != this);

            if (found) {
                // We need to pass the updated children of this node to the call node
                RubyCallNode callNode = new RubyCallNode(callNodeParameters.withReceiverAndArguments(
                        getReceiverNode(),
                        getArgumentNodes(),
                        getBlockNode()));
                callNode.unsafeSetSourceSection(getSourceIndexLength());
                replacedBy = callNode;
                return replace(callNode, this + " could not be executed inline");
            } else {
                return replacedBy;
            }
        });
    }

    protected Object rewriteAndCall(VirtualFrame frame, Object receiver, Object... arguments) {
        return rewriteAndCallWithBlock(frame, receiver, null, arguments);
    }

    protected Object rewriteAndCallWithBlock(VirtualFrame frame, Object receiver, RubyProc block,
            Object... arguments) {
        return rewriteToCallNode().executeWithArgumentsEvaluated(frame, receiver, block, arguments);
    }

    protected CoreMethods coreMethods() {
        return getContext().getCoreMethods();
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        return rewriteToCallNode().isDefined(frame, context);
    }

}
