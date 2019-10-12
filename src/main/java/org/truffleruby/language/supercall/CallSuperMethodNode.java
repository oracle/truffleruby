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
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class CallSuperMethodNode extends RubyBaseNode {

    private final ConditionProfile missingProfile = ConditionProfile.createBinaryProfile();

    @Child private CallInternalMethodNode callMethodNode;
    @Child private CallDispatchHeadNode callMethodMissingNode;

    public static CallSuperMethodNode create() {
        return CallSuperMethodNodeGen.create();
    }

    public abstract Object executeCallSuperMethod(VirtualFrame frame, Object self, Object superMethod,
            Object[] arguments, Object block);

    // superMethod is typed as Object below because it must accept "null".
    @Specialization
    protected Object callSuperMethod(VirtualFrame frame, Object self, Object superMethodObject, Object[] arguments,
            Object block) {
        final InternalMethod superMethod = (InternalMethod) superMethodObject;

        if (missingProfile.profile(superMethod == null)) {
            final String name = RubyArguments.getMethod(frame).getSharedMethodInfo().getName(); // use the original name
            final Object[] methodMissingArguments = ArrayUtils
                    .unshift(arguments, getContext().getSymbolTable().getSymbol(name));
            return callMethodMissing(frame, self, block, methodMissingArguments);
        }

        final Object[] frameArguments = RubyArguments
                .pack(null, null, superMethod, null, self, (DynamicObject) block, arguments);

        return callMethod(superMethod, frameArguments);
    }

    private Object callMethod(InternalMethod superMethod, Object[] frameArguments) {
        if (callMethodNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodNode = insert(CallInternalMethodNode.create());
        }
        return callMethodNode.executeCallMethod(superMethod, frameArguments);
    }

    private Object callMethodMissing(VirtualFrame frame, Object receiver, Object block, Object[] arguments) {
        if (callMethodMissingNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodMissingNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return callMethodMissingNode.callWithBlock(receiver, "method_missing", (DynamicObject) block, arguments);
    }

}
