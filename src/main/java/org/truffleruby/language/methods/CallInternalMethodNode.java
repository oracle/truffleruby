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

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.utilities.AlwaysValidAssumption;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethodNodeManager;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import org.truffleruby.language.RubyCheckArityRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

@ReportPolymorphism
@GenerateUncached
public abstract class CallInternalMethodNode extends RubyBaseNode {

    public static CallInternalMethodNode create() {
        return CallInternalMethodNodeGen.create();
    }

    public abstract Object execute(Frame frame, Object callerData, InternalMethod method, Object self, Object block,
            Object[] args);

    @Specialization(
            guards = { "method.getCallTarget() == cachedCallTarget", "!cachedMethod.alwaysInlined()" },
            assumptions = "getModuleAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object callCached(Object callerData, InternalMethod method, Object self, Object block, Object[] args,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createCall(cachedMethod.getName(), cachedCallTarget)") DirectCallNode callNode) {
        return callNode.call(packArguments(callerData, method, self, block, args));
    }

    @Specialization(guards = "!method.alwaysInlined()", replaces = "callCached")
    protected Object callUncached(Object callerData, InternalMethod method, Object self, Object block, Object[] args,
            @Cached IndirectCallNode indirectCallNode) {
        return indirectCallNode.call(method.getCallTarget(), packArguments(callerData, method, self, block, args));
    }

    @Specialization(
            guards = { "method.getCallTarget() == cachedCallTarget", "cachedMethod.alwaysInlined()" },
            assumptions = "getModuleAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object alwaysInlinedCached(
            Frame frame, Object callerData, InternalMethod method, Object self, Object block, Object[] args,
            @Cached(value = "method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createAlwaysInlinedMethodNode(cachedMethod)") AlwaysInlinedMethodNode alwaysInlinedNode,
            @Cached(value = "cachedMethod.getSharedMethodInfo().getArity()") Arity cachedArity,
            @Cached BranchProfile checkArityProfile,
            @Cached BranchProfile exceptionProfile,
            @CachedContext(RubyLanguage.class) ContextReference<RubyContext> contextRef) {
        assert RubyArguments.assertValues(callerData, method, method.getDeclarationContext(), self, block, args);

        try {
            RubyCheckArityRootNode
                    .checkArity(cachedArity, args.length, checkArityProfile, contextRef, alwaysInlinedNode);

            return alwaysInlinedNode.execute(frame, self, args, block, cachedCallTarget);
        } catch (RaiseException e) {
            exceptionProfile.enter();
            final Node location = e.getLocation();
            if (location != null && location.getRootNode() == alwaysInlinedNode.getRootNode()) {
                // if the error originates from the inlined node, rethrow it through the CallTarget to get a proper backtrace
                return RubyContext.indirectCallWithCallNode(this, cachedCallTarget, e);
            } else {
                throw e;
            }
        }
    }

    @Specialization(guards = "method.alwaysInlined()", replaces = "alwaysInlinedCached")
    protected Object alwaysInlinedUncached(
            Frame frame, Object callerData, InternalMethod method, Object self, Object block, Object[] args,
            @Cached BranchProfile checkArityProfile,
            @Cached BranchProfile exceptionProfile,
            @CachedContext(RubyLanguage.class) ContextReference<RubyContext> contextRef) {
        return alwaysInlinedCached(
                frame,
                callerData,
                method,
                self,
                block,
                args,
                method.getCallTarget(),
                method,
                getUncachedAlwaysInlinedMethodNode(method),
                method.getSharedMethodInfo().getArity(),
                checkArityProfile,
                exceptionProfile,
                contextRef);
    }

    protected AlwaysInlinedMethodNode createAlwaysInlinedMethodNode(InternalMethod method) {
        return (AlwaysInlinedMethodNode) CoreMethodNodeManager
                .createNodeFromFactory(method.alwaysInlinedNodeFactory, RubyNode.EMPTY_ARRAY);
    }

    protected AlwaysInlinedMethodNode getUncachedAlwaysInlinedMethodNode(InternalMethod method) {
        return (AlwaysInlinedMethodNode) method.alwaysInlinedNodeFactory.getUncachedInstance();
    }

    static Object[] packArguments(Object callerData, InternalMethod method, Object self, Object block, Object[] args) {
        return RubyArguments.pack(null, callerData, method, null, self, block, args);
    }

    protected Assumption getModuleAssumption(InternalMethod method) {
        return RubyLanguage.getCurrentLanguage().singleContext
                ? method.getDeclaringModule().fields.getMethodsUnmodifiedAssumption()
                : AlwaysValidAssumption.INSTANCE;
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
