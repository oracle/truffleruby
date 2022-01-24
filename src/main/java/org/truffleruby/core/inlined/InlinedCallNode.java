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
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public class InlinedCallNode extends InlinedReplaceableNode {
    private final String methodName;

    @Child private RubyNode receiver;
    @Child private RubyNode block;
    @Children private final RubyNode[] arguments;

    @Child private LookupMethodOnSelfNode lookupNode;

    @Child private InlinedMethodNode inlinedMethod;

    public InlinedCallNode(
            RubyLanguage language,
            InlinedMethodNode inlinedMethod,
            RubyCallNodeParameters parameters,
            Assumption... assumptions) {
        super(language, parameters, assumptions);

        this.methodName = parameters.getMethodName();
        this.receiver = parameters.getReceiver();
        this.arguments = parameters.getArguments();
        this.block = parameters.getBlock();

        lookupNode = LookupMethodOnSelfNode.create();

        this.inlinedMethod = inlinedMethod;
    }


    @Override
    public Object execute(VirtualFrame frame) {
        final Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiver.execute(frame));

        executeArguments(frame, rubyArgs);

        executeBlock(frame, rubyArgs);

        // The expansion of the splat is done after executing the block, for m(*args, &args.pop)

        if ((lookupNode.lookupProtected(frame, RubyArguments.getSelf(rubyArgs), methodName) != inlinedMethod
                .getMethod()) ||
                !Assumption.isValidAssumption(assumptions)) {
            return rewriteAndCallWithBlock(frame, rubyArgs);
        } else {
            return executeWithArgumentsEvaluated(frame, rubyArgs);
        }
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object[] rubyArgs) {
        return inlinedMethod.inlineExecute(frame, rubyArgs);
    }

    private void executeBlock(VirtualFrame frame, Object[] rubyArgs) {
        if (block != null) {
            RubyArguments.setBlock(rubyArgs, block.execute(frame));
        } else {
            RubyArguments.setBlock(rubyArgs, nil);
        }
    }

    @ExplodeLoop
    private void executeArguments(VirtualFrame frame, Object[] rubyArgs) {
        for (int i = 0; i < arguments.length; i++) {
            RubyArguments.setArgument(rubyArgs, i, arguments[i].execute(frame));
        }
    }

    protected Object rewriteAndCallWithBlock(VirtualFrame frame, Object[] rubyArgs) {
        return rewriteToCallNode().executeWithArgumentsEvaluated(frame, rubyArgs);
    }

    @Override
    protected RubyNode getReceiverNode() {
        return receiver;
    }

    @Override
    protected RubyNode[] getArgumentNodes() {
        return arguments;
    }
}
