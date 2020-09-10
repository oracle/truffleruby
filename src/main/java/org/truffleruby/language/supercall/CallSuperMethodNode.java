/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.supercall;

import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.FrameOrStorageSendingNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class CallSuperMethodNode extends FrameOrStorageSendingNode {

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
            Object[] arguments,
            RubyProc block) {

        if (missingProfile.profile(superMethod == null)) {
            final String name = RubyArguments.getMethod(frame).getSharedMethodInfo().getName(); // use the original name
            final Object[] methodMissingArguments = ArrayUtils
                    .unshift(arguments, getContext().getSymbol(name));
            return callMethodMissing(frame, self, block, methodMissingArguments);
        }

        final Object[] frameArguments = RubyArguments
                .pack(
                        null,
                        getFrameIfRequired(frame),
                        getStorageIfRequired(frame),
                        superMethod,
                        null,
                        self,
                        block,
                        arguments);

        return callMethod(superMethod, frameArguments);
    }

    private Object callMethod(InternalMethod superMethod, Object[] frameArguments) {
        if (callMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodNode = insert(CallInternalMethodNode.create());
        }
        return callMethodNode.execute(superMethod, frameArguments);
    }

    private Object callMethodMissing(VirtualFrame frame, Object receiver, RubyProc block, Object[] arguments) {
        if (callMethodMissingNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodMissingNode = insert(DispatchNode.create());
        }
        return callMethodMissingNode.callWithBlock(receiver, "method_missing", block, arguments);
    }
}
