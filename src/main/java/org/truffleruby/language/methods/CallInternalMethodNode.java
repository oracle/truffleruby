/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.nodes.NodeUtil;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;

@ReportPolymorphism
@GenerateUncached
public abstract class CallInternalMethodNode extends RubyBaseNode {

    public static CallInternalMethodNode create() {
        return CallInternalMethodNodeGen.create();
    }

    public abstract Object execute(Object callerData, InternalMethod method, Object self, Object block, Object[] args);

    @Specialization(
            guards = "method.getCallTarget() == cachedCallTarget",
            assumptions = "getModuleAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object callCached(Object callerData, InternalMethod method, Object self, Object block, Object[] args,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createCall(cachedMethod.getName(), cachedCallTarget)") DirectCallNode callNode) {
        return callNode.call(packArguments(callerData, method, self, block, args));
    }

    @Specialization(replaces = "callCached")
    protected Object callUncached(Object callerData, InternalMethod method, Object self, Object block, Object[] args,
            @Cached IndirectCallNode indirectCallNode) {
        return indirectCallNode.call(method.getCallTarget(), packArguments(callerData, method, self, block, args));
    }

    static Object[] packArguments(Object callerData, InternalMethod method, Object self, Object block, Object[] args) {
        return RubyArguments.pack(null, callerData, method, null, self, block, args);
    }

    protected Assumption getModuleAssumption(InternalMethod method) {
        return method.getDeclaringModule().fields.getMethodsUnmodifiedAssumption();
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentLanguage().options.DISPATCH_CACHE;
    }

    protected DirectCallNode createCall(String methodName, RootCallTarget callTarget) {
        final DirectCallNode callNode = DirectCallNode.create(callTarget);
        final DispatchNode dispatch = NodeUtil.findParent(this, DispatchNode.class);
        if (dispatch != null) {
            dispatch.applySplittingInliningStrategy(callTarget, methodName, callNode);
        }
        return callNode;
    }
}
