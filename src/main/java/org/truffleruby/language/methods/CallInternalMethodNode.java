/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.language.dispatch.DispatchNode;

@ReportPolymorphism
@GenerateUncached
public abstract class CallInternalMethodNode extends RubyBaseNode {

    public static CallInternalMethodNode create() {
        return CallInternalMethodNodeGen.create();
    }

    public abstract Object execute(InternalMethod method, Object[] frameArguments);

    @Specialization(
            guards = "method.getCallTarget() == cachedCallTarget",
            assumptions = "getModuleAssumption(cachedMethod)",
            limit = "getCacheLimit()")
    protected Object callMethodCached(InternalMethod method, Object[] frameArguments,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createCall(cachedMethod.getName(), cachedCallTarget)") DirectCallNode callNode) {
        return callNode.call(frameArguments);
    }

    @Specialization(replaces = "callMethodCached")
    protected Object callMethodUncached(InternalMethod method, Object[] frameArguments,
            @Cached IndirectCallNode indirectCallNode) {
        return indirectCallNode.call(method.getCallTarget(), frameArguments);
    }

    protected Assumption getModuleAssumption(InternalMethod method) {
        return method.getDeclaringModule().fields.getMethodsUnmodifiedAssumption();
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().DISPATCH_CACHE;
    }

    protected DirectCallNode createCall(String methodName, RootCallTarget callTarget) {
        DirectCallNode callNode = DirectCallNode.create(callTarget);
        final DispatchNode dispatch = NodeUtil.findParent(this, DispatchNode.class);
        if (dispatch != null) {
            dispatch.applySplittingInliningStrategy(callTarget, methodName, callNode);
        }
        return callNode;
    }
}
