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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.utilities.AlwaysValidAssumption;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethodNodeManager;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
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
@ImportStatic(RubyArguments.class)
public abstract class CallInternalMethodNode extends RubyBaseNode {

    public static CallInternalMethodNode create() {
        return CallInternalMethodNodeGen.create();
    }

    public abstract Object execute(Frame frame, Object[] rubyArgs);

    @Specialization(
            guards = {
                    "isSingleContext()",
                    "method.getCallTarget() == cachedCallTarget",
                    "!cachedMethod.alwaysInlined()" },
            assumptions = "getMethodAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object callCached(Object[] rubyArgs,
            @Bind("getMethod(rubyArgs)") InternalMethod method,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createCall(cachedMethod.getName(), cachedCallTarget)") DirectCallNode callNode) {
        return callNode.call(rubyArgs);
    }

    @Specialization(guards = "!method.alwaysInlined()", replaces = "callCached")
    protected Object callUncached(Object[] rubyArgs,
            @Bind("getMethod(rubyArgs)") InternalMethod method,
            @Cached IndirectCallNode indirectCallNode) {
        return indirectCallNode.call(method.getCallTarget(), rubyArgs);
    }

    @Specialization(
            guards = {
                    "isSingleContext()",
                    "method.getCallTarget() == cachedCallTarget",
                    "cachedMethod.alwaysInlined()" },
            assumptions = "getMethodAssumption(cachedMethod)", // to remove the inline cache entry when the method is redefined or removed
            limit = "getCacheLimit()")
    protected Object alwaysInlined(Frame frame, Object[] rubyArgs,
            @Bind("getCallerData(rubyArgs)") Object callerData,
            @Bind("getMethod(rubyArgs)") InternalMethod method,
            @Cached(value = "method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("method") InternalMethod cachedMethod,
            @Cached("createAlwaysInlinedMethodNode(cachedMethod)") AlwaysInlinedMethodNode alwaysInlinedNode,
            @Cached(value = "cachedMethod.getSharedMethodInfo().getArity()") Arity cachedArity,
            @Cached BranchProfile checkArityProfile,
            @Cached BranchProfile exceptionProfile) {
        try {
            RubyCheckArityRootNode.checkArity(cachedArity, RubyArguments.getArgumentsCount(rubyArgs), checkArityProfile,
                    alwaysInlinedNode);

            return alwaysInlinedNode.execute(frame, rubyArgs, cachedCallTarget);
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

    @Specialization(guards = "method.alwaysInlined()", replaces = "alwaysInlined")
    protected Object alwaysInlinedUncached(Frame frame, Object[] rubyArgs,
            @Bind("getMethod(rubyArgs)") InternalMethod method) {
        return alwaysInlinedBoundary(
                frame == null ? null : frame.materialize(),
                rubyArgs,
                isAdoptable());
    }

    @TruffleBoundary // getUncachedAlwaysInlinedMethodNode(method) and arity are not PE constants
    private Object alwaysInlinedBoundary(
            MaterializedFrame frame, Object[] rubyArgs,
            boolean cachedToUncached) {
        EncapsulatingNodeReference encapsulating = null;
        Node prev = null;
        if (cachedToUncached) {
            encapsulating = EncapsulatingNodeReference.getCurrent();
            prev = encapsulating.set(this);
        }
        try {
            Object callerData = RubyArguments.getCallerData(rubyArgs);
            InternalMethod method = RubyArguments.getMethod(rubyArgs);

            return alwaysInlined(
                    frame,
                    rubyArgs,
                    callerData,
                    method,
                    method.getCallTarget(),
                    method,
                    getUncachedAlwaysInlinedMethodNode(method),
                    method.getSharedMethodInfo().getArity(),
                    BranchProfile.getUncached(),
                    BranchProfile.getUncached());
        } finally {
            if (cachedToUncached) {
                encapsulating.set(prev);
            }
        }
    }

    protected AlwaysInlinedMethodNode createAlwaysInlinedMethodNode(InternalMethod method) {
        return (AlwaysInlinedMethodNode) CoreMethodNodeManager
                .createNodeFromFactory(method.alwaysInlinedNodeFactory, RubyNode.EMPTY_ARRAY);
    }

    /** Asserted in {@link CoreMethodNodeManager#createCoreMethodRootNode} */
    protected AlwaysInlinedMethodNode getUncachedAlwaysInlinedMethodNode(InternalMethod method) {
        return (AlwaysInlinedMethodNode) method.alwaysInlinedNodeFactory.getUncachedInstance();
    }

    static Object[] packArguments(Object callerData, InternalMethod method, Object self, Object block, Object[] args) {
        return RubyArguments.pack(null, callerData, method, null, self, block, args);
    }

    protected Assumption getMethodAssumption(InternalMethod method) {
        return isSingleContext()
                ? method.getDeclaringModule().fields.getOrCreateMethodAssumption(method.getName())
                : AlwaysValidAssumption.INSTANCE;
    }

    protected int getCacheLimit() {
        return getLanguage().options.DISPATCH_CACHE;
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
