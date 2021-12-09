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
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.ExpandArgumentsNode;
import org.truffleruby.language.arguments.ExpandArgumentsNodeGen;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public class InlinedCallNode extends InlinedReplaceableNode {
    private final String methodName;
    private final KeywordDescriptor keywordDescriptor;

    @Child private RubyNode receiver;
    @Child private RubyNode block;
    @Children private final RubyNode[] arguments;

    @Child private LookupMethodOnSelfNode lookupNode;

    @Child private InlinedMethodNode inlinedMethod;

    @Child private ExpandArgumentsNode expandArgumentsNode = ExpandArgumentsNodeGen.create();

    public InlinedCallNode(
            RubyLanguage language,
            InlinedMethodNode inlinedMethod,
            KeywordDescriptor keywordDescriptor,
            RubyCallNodeParameters parameters,
            Assumption... assumptions) {
        super(language, parameters, assumptions);

        this.keywordDescriptor = keywordDescriptor;
        this.methodName = parameters.getMethodName();
        this.receiver = parameters.getReceiver();
        this.arguments = parameters.getArguments();
        this.block = parameters.getBlock();

        lookupNode = LookupMethodOnSelfNode.create();

        this.inlinedMethod = inlinedMethod;
    }


    @Override
    public Object execute(VirtualFrame frame) {
        final Object receiverObject = receiver.execute(frame);

        final Object[] executedArguments = executeArguments(frame);

        final Object blockObject = executeBlock(frame);

        // The expansion of the splat is done after executing the block, for m(*args, &args.pop)

        if ((lookupNode.lookupProtected(frame, receiverObject, methodName) != inlinedMethod.getMethod()) ||
                !Assumption.isValidAssumption(assumptions)) {
            return rewriteAndCallWithBlock(frame, receiverObject, blockObject, executedArguments);
        } else {
            return executeWithArgumentsEvaluated(
                    frame,
                    receiverObject,
                    blockObject,
                    expandArgumentsNode.execute(executedArguments, keywordDescriptor));
        }
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, Object blockObject,
            Object[] argumentsObjects) {
        return inlinedMethod.inlineExecute(frame, receiverObject, argumentsObjects, EmptyKeywordDescriptor.EMPTY,
                blockObject);
    }

    private Object executeBlock(VirtualFrame frame) {
        if (block != null) {
            return block.execute(frame);
        } else {
            return nil;
        }
    }

    @ExplodeLoop
    private Object[] executeArguments(VirtualFrame frame) {
        final Object[] argumentsObjects = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            argumentsObjects[i] = arguments[i].execute(frame);
        }

        return argumentsObjects;
    }

    protected Object rewriteAndCallWithBlock(VirtualFrame frame, Object receiver, Object block, Object... arguments) {
        return rewriteToCallNode().executeWithArgumentsEvaluated(frame, receiver, block, arguments);
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
