/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchConfiguration;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;

@GenerateUncached
public abstract class GetMethodObjectNode extends RubyBaseNode {
    public static GetMethodObjectNode create() {
        return GetMethodObjectNodeGen.create();
    }

    public abstract RubyMethod executeGetMethodObject(Frame frame, Object self, Object name,
            DispatchConfiguration dispatchConfig, Frame callerFrame);

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentLanguage().options.METHOD_LOOKUP_CACHE;
    }

    @Specialization(guards = {
            "dispatchConfig == cachedDispatchConfig",
            "self == cachedSelf",
            "name.equals(cachedName)",
            "contextReference.get() == cachedContext"
    }, limit = "getCacheLimit()")
    protected RubyMethod doCached(Frame frame, Object self, Object name,
            DispatchConfiguration dispatchConfig,
            Frame callerFrame,
            @Cached("self") Object cachedSelf,
            @Cached("name") Object cachedName,
            @Cached("dispatchConfig") DispatchConfiguration cachedDispatchConfig,
            @CachedContext(RubyLanguage.class) TruffleLanguage.ContextReference<RubyContext> contextReference,
            @Cached("contextReference.get()") RubyContext cachedContext,
            @Cached NameToJavaStringNode nameToJavaStringNode,
            @Cached LookupMethodOnSelfNode lookupMethodNode,
            @Cached DispatchNode respondToMissingNode,
            @Cached BooleanCastNode booleanCastNode,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile respondToMissingProfile,
            @Cached LogicalClassNode logicalClassNode,
            @Cached("getMethodObject(frame, self, cachedName, cachedDispatchConfig, callerFrame, cachedContext, nameToJavaStringNode, lookupMethodNode, respondToMissingNode, booleanCastNode, notFoundProfile, respondToMissingProfile, logicalClassNode)") RubyMethod cachedRubyMethod) {
        return cachedRubyMethod;
    }


    @Specialization(replaces = "doCached")
    protected RubyMethod doGeneric(Frame frame, Object self, Object name,
            DispatchConfiguration dispatchConfig,
            Frame callerFrame,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached NameToJavaStringNode nameToJavaStringNode,
            @Cached LookupMethodOnSelfNode lookupMethodNode,
            @Cached DispatchNode respondToMissingNode,
            @Cached BooleanCastNode booleanCastNode,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile respondToMissingProfile,
            @Cached LogicalClassNode logicalClassNode) {
        assert this != GetMethodObjectNodeGen.getUncached() || frame == null;

        return getMethodObject(
                frame,
                self,
                name,
                dispatchConfig,
                callerFrame,
                context,
                nameToJavaStringNode,
                lookupMethodNode,
                respondToMissingNode,
                booleanCastNode,
                notFoundProfile,
                respondToMissingProfile,
                logicalClassNode);
    }

    protected RubyMethod getMethodObject(Frame frame, Object self, Object name,
            DispatchConfiguration dispatchConfig,
            Frame callerFrame,
            RubyContext context,
            NameToJavaStringNode nameToJavaStringNode,
            LookupMethodOnSelfNode lookupMethodNode,
            DispatchNode respondToMissingNode,
            BooleanCastNode booleanCastNode,
            ConditionProfile notFoundProfile,
            ConditionProfile respondToMissingProfile,
            LogicalClassNode logicalClassNode) {
        DeclarationContext originalDeclarationContext = null;

        if (frame != null) {
            originalDeclarationContext = RubyArguments.getDeclarationContext(frame);

            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(callerFrame);
            if (declarationContext != null) {
                RubyArguments.setDeclarationContext(frame, declarationContext);
            }
        }

        final String normalizedName = nameToJavaStringNode.execute(name);
        InternalMethod method = lookupMethodNode
                .lookup(frame, self, normalizedName, dispatchConfig);

        if (notFoundProfile.profile(method == null)) {
            final Object respondToMissing = respondToMissingNode
                    .call(self, "respond_to_missing?", name, dispatchConfig.ignoreVisibility);
            if (respondToMissingProfile.profile(booleanCastNode.executeToBoolean(respondToMissing))) {
                /** refinements should not affect BasicObject#method_missing */
                RubyArguments.setDeclarationContext(frame, originalDeclarationContext);
                final InternalMethod methodMissing = lookupMethodNode
                        .lookup(frame, self, "method_missing", dispatchConfig);
                method = createMissingMethod(self, name, normalizedName, methodMissing, context);
            } else {
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().nameErrorUndefinedMethod(
                                normalizedName,
                                logicalClassNode.execute(self),
                                this));
            }
        }
        final RubyMethod instance = new RubyMethod(
                context.getCoreLibrary().methodClass,
                RubyLanguage.getCurrentLanguage().methodShape,
                self,
                method);
        AllocationTracing.trace(RubyLanguage.getCurrentLanguage(), context, instance, this);
        return instance;
    }

    @TruffleBoundary
    private InternalMethod createMissingMethod(Object self, Object name, String normalizedName,
            InternalMethod methodMissing,
            RubyContext context) {
        final SharedMethodInfo info = methodMissing
                .getSharedMethodInfo()
                .convertMethodMissingToMethod(methodMissing.getDeclaringModule(), normalizedName);

        final RubyNode newBody = new CallMethodMissingWithStaticName(name);
        final RubyRootNode newRootNode = new RubyRootNode(
                RubyLanguage.getCurrentLanguage(),
                info.getSourceSection(),
                new FrameDescriptor(nil),
                info,
                newBody,
                Split.HEURISTIC);
        final RootCallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

        final RubyClass module = MetaClassNode.getUncached().execute(self);
        return new InternalMethod(
                context,
                info,
                methodMissing.getLexicalScope(),
                DeclarationContext.NONE,
                normalizedName,
                module,
                Visibility.PUBLIC,
                newCallTarget);
    }

    private static class CallMethodMissingWithStaticName extends RubyContextSourceNode {
        private final Object methodName;
        @Child private DispatchNode methodMissing = DispatchNode.create();

        public CallMethodMissingWithStaticName(Object methodName) {
            this.methodName = methodName;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object[] originalUserArguments = RubyArguments.getArguments(frame);
            final Object[] newUserArguments = ArrayUtils.unshift(originalUserArguments, methodName);
            return methodMissing.callWithBlock(
                    RubyArguments.getSelf(frame),
                    "method_missing",
                    RubyArguments.getBlock(frame),
                    newUserArguments);
        }
    }
}
