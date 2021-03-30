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
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ReturnID;
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

    public abstract RubyMethod execute(Frame frame, Object self, Object name,
            DispatchConfiguration dispatchConfig, MaterializedFrame callerFrame);

    @Specialization
    protected RubyMethod getMethodObject(
            Frame frame, Object self, Object name, DispatchConfiguration dispatchConfig, MaterializedFrame callerFrame,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached NameToJavaStringNode nameToJavaStringNode,
            @Cached LookupMethodOnSelfNode lookupMethodNode,
            @Cached ToSymbolNode toSymbolNode,
            @Cached DispatchNode respondToMissingNode,
            @Cached BooleanCastNode booleanCastNode,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile respondToMissingProfile,
            @Cached LogicalClassNode logicalClassNode) {
        assert this != GetMethodObjectNodeGen.getUncached() || frame == null;
        DeclarationContext originalDeclarationContext = null;

        if (frame != null) {
            originalDeclarationContext = RubyArguments.getDeclarationContext(frame);

            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(callerFrame);
            if (declarationContext != null) {
                RubyArguments.setDeclarationContext(frame, declarationContext);
            }
        }

        final String normalizedName = nameToJavaStringNode.execute(name);
        InternalMethod method = lookupMethodNode.execute(frame, self, normalizedName, dispatchConfig);

        if (notFoundProfile.profile(method == null)) {
            final RubySymbol symbolName = toSymbolNode.execute(name);
            final Object respondToMissing = respondToMissingNode
                    .call(self, "respond_to_missing?", symbolName, dispatchConfig.ignoreVisibility);
            if (respondToMissingProfile.profile(booleanCastNode.executeToBoolean(respondToMissing))) {
                if (frame != null) {
                    // refinements should not affect BasicObject#method_missing
                    RubyArguments.setDeclarationContext(frame, originalDeclarationContext);
                }
                final InternalMethod methodMissing = lookupMethodNode
                        .execute(frame, self, "method_missing", dispatchConfig);
                method = createMissingMethod(self, symbolName, normalizedName, methodMissing, language, context);
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
                language.methodShape,
                self,
                method);
        AllocationTracing.trace(language, context, instance, this);
        return instance;
    }

    @TruffleBoundary
    private InternalMethod createMissingMethod(Object self, RubySymbol name, String normalizedName,
            InternalMethod methodMissing, RubyLanguage language, RubyContext context) {
        final SharedMethodInfo info = methodMissing
                .getSharedMethodInfo()
                .convertMethodMissingToMethod(methodMissing.getDeclaringModule(), normalizedName);

        final RubyNode newBody = new CallMethodMissingWithStaticName(name);
        final RubyRootNode newRootNode = new RubyRootNode(
                language,
                info.getSourceSection(),
                new FrameDescriptor(nil),
                info,
                newBody,
                Split.HEURISTIC,
                ReturnID.INVALID);
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

        private final RubySymbol methodName;
        @Child private DispatchNode methodMissing = DispatchNode.create();

        public CallMethodMissingWithStaticName(RubySymbol methodName) {
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
