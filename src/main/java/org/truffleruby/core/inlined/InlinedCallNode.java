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
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.cast.ProcOrNullNodeGen;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public class InlinedCallNode extends InlinedReplaceableNode {
    private final String methodName;
    @CompilationFinal private InternalMethod coreMethod;

    @Child private RubyNode receiver;
    @Child private ProcOrNullNode block;
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

        if (parameters.getBlock() == null) {
            this.block = null;
        } else {
            this.block = ProcOrNullNodeGen.create(parameters.getBlock());
        }

        lookupNode = LookupMethodOnSelfNode.create();

        this.inlinedMethod = inlinedMethod;
    }


    @Override
    public Object execute(VirtualFrame frame) {
        final Object receiverObject = receiver.execute(frame);

        final Object[] executedArguments = executeArguments(frame);

        final RubyProc blockObject = executeBlock(frame);

        // The expansion of the splat is done after executing the block, for m(*args, &args.pop)

        if ((lookupNode.lookupProtected(frame, receiverObject, methodName) != coreMethod()) ||
                !Assumption.isValidAssumption(assumptions)) {
            return rewriteAndCallWithBlock(frame, receiverObject, blockObject, executedArguments);
        } else {
            return executeWithArgumentsEvaluated(frame, receiverObject, blockObject, executedArguments);
        }
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, RubyProc blockObject,
            Object[] argumentsObjects) {
        return inlinedMethod.inlineExecute(
                frame,
                receiverObject,
                argumentsObjects,
                blockObject == null ? NotProvided.INSTANCE : blockObject);
    }

    private RubyProc executeBlock(VirtualFrame frame) {
        if (block != null) {
            return block.executeProcOrNull(frame);
        } else {
            return null;
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

    protected Object rewriteAndCallWithBlock(VirtualFrame frame, Object receiver, RubyProc block,
            Object... arguments) {
        return rewriteToCallNode().executeWithArgumentsEvaluated(frame, receiver, block, arguments);
    }

    protected InternalMethod coreMethod() {
        if (coreMethod == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            coreMethod = inlinedMethod.getMethod();
        }
        return coreMethod;
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
