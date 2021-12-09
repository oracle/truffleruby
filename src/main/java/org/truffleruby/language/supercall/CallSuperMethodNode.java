/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.FrameAndVariablesSendingNode;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class CallSuperMethodNode extends FrameAndVariablesSendingNode {

    private final KeywordDescriptor keywordDescriptor;

    private final ConditionProfile missingProfile = ConditionProfile.create();

    @Child private CallInternalMethodNode callMethodNode;
    @Child private DispatchNode callMethodMissingNode;

    public static CallSuperMethodNode create(KeywordDescriptor keywordDescriptor) {
        return new CallSuperMethodNode(keywordDescriptor);
    }

    private CallSuperMethodNode(KeywordDescriptor keywordDescriptor) {
        this.keywordDescriptor = keywordDescriptor;
    }

    public Object execute(
            VirtualFrame frame,
            Object self,
            InternalMethod superMethod,
            Object[] arguments,
            Object block) {

        if (missingProfile.profile(superMethod == null)) {
            final String name = RubyArguments.getMethod(frame).getSharedMethodInfo().getMethodNameForNotBlock(); // use the original name
            final Object[] methodMissingArguments = ArrayUtils
                    .unshift(arguments, getSymbol(name));
            return callMethodMissing(frame, self, block, methodMissingArguments);
        }

        final Object callerFrameOrVariables = getFrameOrStorageIfRequired(frame);

        return getCallMethodNode().execute(
                frame,
                callerFrameOrVariables,
                superMethod,
                self,
                block,
                arguments,
                keywordDescriptor);
    }

    private CallInternalMethodNode getCallMethodNode() {
        if (callMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodNode = insert(CallInternalMethodNode.create());
        }
        return callMethodNode;
    }

    private Object callMethodMissing(VirtualFrame frame, Object receiver, Object block, Object[] arguments) {
        if (callMethodMissingNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodMissingNode = insert(DispatchNode.create());
        }
        return callMethodMissingNode.callWithBlock(receiver, "method_missing", block, arguments);
    }
}
