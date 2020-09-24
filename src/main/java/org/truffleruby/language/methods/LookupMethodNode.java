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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchConfiguration;
import org.truffleruby.language.objects.MetaClassNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** Caches {@link ModuleOperations#lookupMethodCached(RubyModule, String, DeclarationContext)} on an actual instance. */
@ReportPolymorphism
@GenerateUncached
public abstract class LookupMethodNode extends RubyBaseNode {

    public static LookupMethodNode create() {
        return LookupMethodNodeGen.create();
    }

    public InternalMethod lookup(VirtualFrame frame, RubyClass metaClass, String name) {
        return execute(frame, metaClass, name, DispatchConfiguration.PROTECTED);
    }

    public abstract InternalMethod execute(Frame frame, RubyClass metaClass, String name,
            DispatchConfiguration config);

    @Specialization(
            guards = {
                    "metaClass == cachedMetaClass",
                    "name == cachedName",
                    "config == cachedConfig",
                    "contextReference.get() == cachedContext" },
            assumptions = "methodLookupResult.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodCached(
            Frame frame,
            RubyClass metaClass,
            String name,
            DispatchConfiguration config,
            @CachedContext(RubyLanguage.class) TruffleLanguage.ContextReference<RubyContext> contextReference,
            @Cached("contextReference.get()") RubyContext cachedContext,
            @Cached("metaClass") RubyClass cachedMetaClass,
            @Cached("name") String cachedName,
            @Cached("config") DispatchConfiguration cachedConfig,
            @Cached("lookupCached(cachedContext, frame, cachedMetaClass, cachedName, config)") MethodLookupResult methodLookupResult) {

        return methodLookupResult.getMethod();
    }

    @Specialization(replaces = "lookupMethodCached")
    protected InternalMethod lookupMethodUncached(
            Frame frame,
            RubyClass metaClass,
            String name,
            DispatchConfiguration config,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached MetaClassNode metaClassNode,
            @Cached ConditionProfile noCallerMethodProfile,
            @Cached ConditionProfile isSendProfile,
            @Cached ConditionProfile foreignProfile,
            @Cached ConditionProfile noPrependedModulesProfile,
            @Cached ConditionProfile onMetaClassProfile,
            @Cached ConditionProfile hasRefinementsProfile,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile publicProfile,
            @Cached ConditionProfile privateProfile,
            @Cached ConditionProfile isVisibleProfile) {

        assert this != LookupMethodNodeGen.getUncached() || frame == null;

        // Actual lookup

        if (foreignProfile.profile(metaClass == context.getCoreLibrary().truffleInteropForeignClass)) {
            return null;
        }

        final DeclarationContext declarationContext = RubyArguments.tryGetDeclarationContext(frame);
        final InternalMethod method;
        // Lookup first in the metaclass as we are likely to find the method there
        final ModuleFields fields = metaClass.fields;
        InternalMethod topMethod;
        if (noPrependedModulesProfile.profile(fields.getFirstModuleChain() == fields) &&
                onMetaClassProfile.profile((topMethod = fields.getMethod(name)) != null) &&
                !hasRefinementsProfile.profile(declarationContext != null && declarationContext.hasRefinements())) {
            method = topMethod;
        } else {
            method = ModuleOperations.lookupMethodUncached(metaClass, name, declarationContext);
        }

        if (notFoundProfile.profile(method == null || method.isUndefined())) {
            return null;
        }

        // Check visibility

        if (!config.ignoreVisibility) {
            final Visibility visibility = method.getVisibility();
            if (publicProfile.profile(visibility == Visibility.PUBLIC)) {
                return method;
            }

            if (config.onlyLookupPublic) {
                return null;
            }

            if (privateProfile.profile(visibility == Visibility.PRIVATE)) {
                // A private method may only be called with an implicit receiver.
                return null;
            }

            // Find the caller class
            final RubyClass callerClass;
            final InternalMethod callerMethod = RubyArguments.tryGetMethod(frame);

            if (noCallerMethodProfile.profile(callerMethod == null)) {
                callerClass = context.getCoreLibrary().objectClass;
            } else if (!isSendProfile.profile(context.getCoreLibrary().isSend(callerMethod))) {
                callerClass = metaClassNode.execute(RubyArguments.getSelf(frame));
            } else {
                Frame callerFrame = context.getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
                callerClass = metaClassNode.execute(RubyArguments.getSelf(callerFrame));
            }

            if (!isVisibleProfile.profile(method.isProtectedMethodVisibleTo(callerClass))) {
                return null;
            }
        }

        return method;
    }

    protected static MethodLookupResult lookupCached(RubyContext context, Frame callingFrame,
            RubyClass metaClass, String name, DispatchConfiguration config) {
        CompilerAsserts.neverPartOfCompilation("slow-path method lookup should not be compiled");

        if (metaClass == context.getCoreLibrary().truffleInteropForeignClass) {
            return new MethodLookupResult(null);
        }

        final DeclarationContext declarationContext = RubyArguments.tryGetDeclarationContext(callingFrame);
        final MethodLookupResult method = ModuleOperations.lookupMethodCached(metaClass, name, declarationContext);

        if (!method.isDefined()) {
            return method.withNoMethod();
        }

        // Check visibility
        if (!config.ignoreVisibility) {
            final Visibility visibility = method.getMethod().getVisibility();
            if (visibility == Visibility.PUBLIC) {
                return method;
            }

            if (config.onlyLookupPublic) {
                return method.withNoMethod();
            }

            if (visibility == Visibility.PRIVATE) {
                // A private method may only be called with an implicit receiver.
                return method.withNoMethod();
            }

            final RubyClass callerClass = getCallerClass(context, callingFrame);
            if (!method.getMethod().isProtectedMethodVisibleTo(callerClass)) {
                return method.withNoMethod();
            }
        }

        return method;
    }

    private static RubyClass getCallerClass(RubyContext context, Frame callingFrame) {
        final InternalMethod callerMethod = RubyArguments.tryGetMethod(callingFrame);
        if (callerMethod == null) {
            return context.getCoreLibrary().objectClass;
        } else if (!context.getCoreLibrary().isSend(callerMethod)) {
            return context.getCoreLibrary().getMetaClass(RubyArguments.getSelf(callingFrame));
        } else {
            final Frame callerFrame = context.getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
            return context.getCoreLibrary().getMetaClass(RubyArguments.getSelf(callerFrame));
        }
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
    }
}
