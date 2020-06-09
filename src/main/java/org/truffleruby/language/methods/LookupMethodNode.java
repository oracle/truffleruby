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

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
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
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.utils.Utils;

/** Caches {@link ModuleOperations#lookupMethodCached(DynamicObject, String, DeclarationContext)} on an actual
 * instance. */
@ReportPolymorphism
@GenerateUncached
public abstract class LookupMethodNode extends RubyBaseNode {

    public static LookupMethodNode create() {
        return LookupMethodNodeGen.create();
    }

    public InternalMethod lookup(VirtualFrame frame, Object self, String name) {
        return executeLookupMethod(frame, self, name, false, false);
    }

    public InternalMethod lookup(
            VirtualFrame frame, Object self, String name, boolean ignoreVisibility, boolean onlyLookupPublic) {
        return executeLookupMethod(frame, self, name, ignoreVisibility, onlyLookupPublic);
    }

    public InternalMethod lookupIgnoringVisibility(VirtualFrame frame, Object self, String name) {
        return executeLookupMethod(frame, self, name, true, false);
    }

    protected abstract InternalMethod executeLookupMethod(Frame frame, Object self, String name,
            boolean ignoreVisibility, boolean onlyLookupPublic);

    @Specialization(
            guards = {
                    "metaClass(metaClassNode, self) == cachedSelfMetaClass",
                    "name == cachedName",
                    "contextReference.get() == cachedContext",
                    "ignoreVisibility == cachedIgnoreVisibility",
                    "onlyLookupPublic == cachedOnlyLookupPublic" },
            assumptions = "methodLookupResult.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodCached(
            Frame frame,
            Object self,
            String name,
            boolean ignoreVisibility,
            boolean onlyLookupPublic,
            @CachedContext(RubyLanguage.class) TruffleLanguage.ContextReference<RubyContext> contextReference,
            @Cached("contextReference.get()") RubyContext cachedContext,
            @Cached("name") String cachedName,
            @Cached MetaClassNode metaClassNode,
            @Cached(value = "ignoreVisibility", allowUncached = true) boolean cachedIgnoreVisibility,
            @Cached(value = "onlyLookupPublic", allowUncached = true) boolean cachedOnlyLookupPublic,
            @Cached("metaClass(metaClassNode, self)") DynamicObject cachedSelfMetaClass,
            @Cached("doCachedLookup(cachedContext, frame, self, cachedName, cachedIgnoreVisibility, cachedOnlyLookupPublic)") MethodLookupResult methodLookupResult) {

        return methodLookupResult.getMethod();
    }

    @Specialization(
            guards = {
                    "ignoreVisibility == cachedIgnoreVisibility",
                    "onlyLookupPublic == cachedOnlyLookupPublic" },
            replaces = "lookupMethodCached",
            limit = "1")
    protected InternalMethod lookupMethodUncached(
            Frame frame,
            Object self,
            String name,
            boolean ignoreVisibility,
            boolean onlyLookupPublic,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached MetaClassNode callerMetaClassNode,
            @Cached MetaClassNode metaClassNode,
            @Cached(value = "ignoreVisibility", allowUncached = true) boolean cachedIgnoreVisibility,
            @Cached(value = "onlyLookupPublic", allowUncached = true) boolean cachedOnlyLookupPublic,
            @Cached ConditionProfile noCallerMethodProfile,
            @Cached ConditionProfile isSendProfile,
            @Cached BranchProfile foreignProfile,
            @Cached ConditionProfile noPrependedModulesProfile,
            @Cached ConditionProfile onMetaClassProfile,
            @Cached ConditionProfile isRefinedProfile,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile publicProfile,
            @Cached ConditionProfile privateProfile,
            @Cached ConditionProfile isVisibleProfile) {

        assert this != LookupMethodNodeGen.getUncached() || frame == null;

        // Actual lookup

        final DynamicObject metaClass = metaClass(metaClassNode, self);

        if (metaClass == context.getCoreLibrary().truffleInteropForeignClass) {
            foreignProfile.enter();
            throw Utils.unsupportedOperation("method lookup not supported on foreign objects");
        }

        final DeclarationContext declarationContext = RubyArguments.tryGetDeclarationContext(frame);
        final InternalMethod method;
        // Lookup first in the metaclass as we are likely to find the method there
        final ModuleFields fields = Layouts.MODULE.getFields(metaClass);
        InternalMethod topMethod;
        if (noPrependedModulesProfile.profile(fields.getFirstModuleChain() == fields) &&
                onMetaClassProfile.profile((topMethod = fields.getMethod(name)) != null) &&
                !isRefinedProfile.profile(declarationContext != null && declarationContext.getRefinements() != null)) {
            method = topMethod;
        } else {
            method = ModuleOperations.lookupMethodUncached(metaClass, name, declarationContext);
        }

        if (notFoundProfile.profile(method == null || method.isUndefined())) {
            return null;
        }

        // Check visibility

        if (!cachedIgnoreVisibility) {
            final Visibility visibility = method.getVisibility();
            if (publicProfile.profile(visibility == Visibility.PUBLIC)) {
                return method;
            }

            if (cachedOnlyLookupPublic) {
                return null;
            }

            if (privateProfile.profile(visibility == Visibility.PRIVATE)) {
                // A private method may only be called with an implicit receiver.
                return null;
            }

            // Find the caller class
            final DynamicObject callerClass;
            final InternalMethod callerMethod = RubyArguments.tryGetMethod(frame);

            if (noCallerMethodProfile.profile(callerMethod == null)) {
                callerClass = context.getCoreLibrary().objectClass;
            } else if (!isSendProfile.profile(context.getCoreLibrary().isSend(callerMethod))) {
                callerClass = callerMetaClassNode.executeMetaClass(RubyArguments.getSelf(frame));
            } else {
                Frame callerFrame = context.getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
                callerClass = callerMetaClassNode.executeMetaClass(RubyArguments.getSelf(callerFrame));
            }

            if (!isVisibleProfile.profile(method.isProtectedMethodVisibleTo(callerClass))) {
                return null;
            }
        }

        return method;
    }

    protected DynamicObject metaClass(MetaClassNode metaClassNode, Object object) {
        return metaClassNode.executeMetaClass(object);
    }

    protected MethodLookupResult doCachedLookup(
            RubyContext context, Frame frame, Object self, String name, boolean ignoreVisibility,
            boolean onlyLookupPublic) {
        return lookupMethodCachedWithVisibility(context, frame, self, name, ignoreVisibility, onlyLookupPublic);
    }

    public static MethodLookupResult lookupMethodCachedWithVisibility(RubyContext context, Frame callingFrame,
            Object receiver, String name, boolean ignoreVisibility, boolean onlyLookupPublic) {
        CompilerAsserts.neverPartOfCompilation("slow-path method lookup should not be compiled");

        if (RubyGuards.isForeignObject(receiver)) {
            throw new UnsupportedOperationException("method lookup not supported on foreign objects");
        }
        final DeclarationContext declarationContext = RubyArguments.tryGetDeclarationContext(callingFrame);
        final MethodLookupResult method = ModuleOperations
                .lookupMethodCached(context.getCoreLibrary().getMetaClass(receiver), name, declarationContext);

        if (!method.isDefined()) {
            return method.withNoMethod();
        }

        // Check visibility
        if (!ignoreVisibility) {
            final Visibility visibility = method.getMethod().getVisibility();
            if (visibility == Visibility.PUBLIC) {
                return method;
            }

            if (onlyLookupPublic) {
                return method.withNoMethod();
            }

            if (visibility == Visibility.PRIVATE) {
                // A private method may only be called with an implicit receiver.
                return method.withNoMethod();
            }

            final DynamicObject callerClass = getCallerClass(context, callingFrame);
            if (!method.getMethod().isProtectedMethodVisibleTo(callerClass)) {
                return method.withNoMethod();
            }
        }

        return method;
    }

    protected static DynamicObject getCallerClass(RubyContext context, Frame callingFrame) {
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
        return getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
    }

    protected static RubyContext getCurrentContext() {
        return RubyLanguage.getCurrentContext();
    }

}
