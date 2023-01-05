/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.SpecialVariablesSendingNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.LiteralCallNode;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

public class CallSuperMethodNode extends SpecialVariablesSendingNode {

    private final ConditionProfile missingProfile = ConditionProfile.create();

    @Child private CallInternalMethodNode callMethodNode;
    @Child private DispatchNode callMethodMissingNode;

    public static CallSuperMethodNode create() {
        return new CallSuperMethodNode();
    }

    private CallSuperMethodNode() {
    }

    public Object execute(
            VirtualFrame frame,
            Object self,
            InternalMethod superMethod,
            ArgumentsDescriptor descriptor,
            Object[] arguments,
            Object block,
            LiteralCallNode literalCallNode) {

        if (missingProfile.profile(superMethod == null)) {
            final String name = RubyArguments.getMethod(frame).getSharedMethodInfo().getMethodNameForNotBlock(); // use the original name
            final Object[] methodMissingArguments = ArrayUtils.unshift(arguments, getSymbol(name));
            return callMethodMissing(self, block, descriptor, methodMissingArguments, literalCallNode);
        }

        final SpecialVariableStorage callerSpecialVariables = getSpecialVariablesIfRequired(frame);
        final Object[] rubyArgs = RubyArguments.pack(
                null, callerSpecialVariables, superMethod, null, self, block, descriptor, arguments);

        return getCallMethodNode().execute(frame, superMethod, self, rubyArgs, literalCallNode);
    }

    private CallInternalMethodNode getCallMethodNode() {
        if (callMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodNode = insert(CallInternalMethodNode.create());
        }
        return callMethodNode;
    }

    private Object callMethodMissing(Object receiver, Object block, ArgumentsDescriptor descriptor, Object[] arguments,
            LiteralCallNode literalCallNode) {
        if (callMethodMissingNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodMissingNode = insert(DispatchNode.create());
        }
        return callMethodMissingNode.callWithDescriptor(receiver, "method_missing", block, descriptor, arguments,
                literalCallNode);
    }
}
