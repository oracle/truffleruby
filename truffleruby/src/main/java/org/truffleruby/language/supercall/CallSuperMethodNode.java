/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.supercall;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.cast.ProcOrNullNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.CallInternalMethodNodeGen;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;

@NodeChildren({
        @NodeChild("superMethod"),
        @NodeChild("arguments"),
        @NodeChild("block")
})
public abstract class CallSuperMethodNode extends RubyNode {

    private final ConditionProfile missingProfile = ConditionProfile.createBinaryProfile();

    @Child private CallInternalMethodNode callMethodNode = CallInternalMethodNodeGen.create(null, new RubyNode[] {});
    @Child private CallDispatchHeadNode callMethodMissingNode;

    @Specialization
    public final Object callSuperMethod(VirtualFrame frame, InternalMethod superMethod, Object[] arguments, Object block) {
        final Object self = RubyArguments.getSelf(frame);

        if (missingProfile.profile(superMethod == null)) {
            final String name = RubyArguments.getMethod(frame).getSharedMethodInfo().getName(); // use the original name
            final Object[] methodMissingArguments = ArrayUtils.unshift(arguments, getContext().getSymbolTable().getSymbol(name));
            return callMethodMissing(frame, self, block, methodMissingArguments);
        }

        final Object[] frameArguments = RubyArguments.pack(null, null, superMethod, DeclarationContext.METHOD, null, self, (DynamicObject) block, arguments);

        return callMethodNode.executeCallMethod(frame, superMethod, frameArguments);
    }

    private Object callMethodMissing(VirtualFrame frame, Object receiver, Object block, Object[] arguments) {
        if (callMethodMissingNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodMissingNode = insert(DispatchHeadNodeFactory.createMethodCallOnSelf());
        }
        return callMethodMissingNode.callWithBlock(frame, receiver, "method_missing", (DynamicObject) block, arguments);
    }

}
