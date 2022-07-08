/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.HostCompilerDirectives.InliningCutoff;
import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.ImportStatic;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.SharedIndicesMap;
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
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.profiles.ConditionProfile;

import java.util.Map;

/** Caches {@link ModuleOperations#lookupMethodCached(RubyModule, String, DeclarationContext)} on an actual instance. */
@ReportPolymorphism
@GenerateUncached
@ImportStatic(DeclarationContext.class)
public abstract class LookupMethodNode extends RubyBaseNode {

    public static LookupMethodNode create() {
        return LookupMethodNodeGen.create();
    }

    public abstract InternalMethod execute(Frame frame, RubyClass metaClass, String name, DispatchConfiguration config);

    @Specialization(
            // no need to guard on the context, the metaClass is context-specific
            guards = {
                    "isSingleContext()",
                    "metaClass == cachedMetaClass",
                    "hasNoRefinements",
                    "name == cachedName",
                    "config == cachedConfig" },
            assumptions = "methodLookupResult.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodSingleVTableCached(
            Frame frame, RubyClass metaClass, String name, DispatchConfiguration config,
            @Cached("metaClass") RubyClass cachedMetaClass,
            @Cached("name") String cachedName,
            @Cached("config") DispatchConfiguration cachedConfig,
            @Cached("getRefinements(frame, config) == NO_REFINEMENTS") boolean hasNoRefinements,
            @Cached("lookupCachedVTable(getContext(), frame, cachedMetaClass, cachedConfig, cachedName)") MethodLookupResult methodLookupResult) {

        return methodLookupResult.getMethod();
    }

    @Specialization(
            guards = {
                    "metaClass.methodNamesToIndex == cachedMethodNamesToIndex",
                    "getRefinements(frame, cachedConfig) == NO_REFINEMENTS",
                    "name == cachedName",
                    "config == cachedConfig" },
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodCached(
            Frame frame, RubyClass metaClass, String name, DispatchConfiguration config,
            @Cached("name") String cachedName,
            @Cached("config") DispatchConfiguration cachedConfig,
            @Cached("metaClass.methodNamesToIndex") SharedIndicesMap cachedMethodNamesToIndex,
            @Cached("cachedMethodNamesToIndex.lookup(name)") int index,
            @Cached MetaClassNode metaClassNode,
            @Cached ConditionProfile noCallerMethodProfile,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile outOfBoundsVTableProfile,
            @Cached ConditionProfile publicProfile,
            @Cached ConditionProfile privateProfile,
            @Cached ConditionProfile isVisibleProfile) {
        final InternalMethod[] methods = metaClass.methodVTable;
        final InternalMethod method = outOfBoundsVTableProfile.profile(index < methods.length) ? methods[index] : null;

        if (notFoundProfile.profile(method == null || method.isUndefined())) {
            return null;
        }

        return getInternalMethod(frame, config, metaClassNode, noCallerMethodProfile, publicProfile, privateProfile,
                isVisibleProfile, method);
    }

    @Specialization(
            // no need to guard on the context, the metaClass is context-specific
            guards = {
                    "isSingleContext()",
                    "metaClass == cachedMetaClass",
                    "name == cachedName",
                    "config == cachedConfig" },
            assumptions = "methodLookupResult.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodRefinementsCached(
            Frame frame, RubyClass metaClass, String name, DispatchConfiguration config,
            @Cached("metaClass") RubyClass cachedMetaClass,
            @Cached("name") String cachedName,
            @Cached("config") DispatchConfiguration cachedConfig,
            @Cached("lookupCached(getContext(), frame, cachedMetaClass, cachedName, config)") MethodLookupResult methodLookupResult) {

        return methodLookupResult.getMethod();
    }

    @InliningCutoff
    @Specialization(
            replaces = { "lookupMethodCached", "lookupMethodRefinementsCached", "lookupMethodSingleVTableCached" })
    protected InternalMethod lookupMethodUncached(
            Frame frame, RubyClass metaClass, String name, DispatchConfiguration config,
            @Cached MetaClassNode metaClassNode,
            @Cached ConditionProfile noCallerMethodProfile,
            @Cached ConditionProfile noPrependedModulesProfile,
            @Cached ConditionProfile onMetaClassProfile,
            @Cached ConditionProfile hasRefinementsProfile,
            @Cached ConditionProfile notFoundProfile,
            @Cached ConditionProfile publicProfile,
            @Cached ConditionProfile privateProfile,
            @Cached ConditionProfile isVisibleProfile) {
        CompilerAsserts.partialEvaluationConstant(config); // the DispatchConfiguration is always a constant in the caller

        // Actual lookup

        final DeclarationContext declarationContext = getDeclarationContext(frame, config);
        final InternalMethod method;
        // Lookup first in the metaclass as we are likely to find the method there
        final ModuleFields fields = metaClass.fields;
        InternalMethod topMethod;
        if (noPrependedModulesProfile.profile(!fields.hasPrependedModules()) &&
                onMetaClassProfile.profile((topMethod = fields.getMethod(name)) != null) &&
                !hasRefinementsProfile.profile(declarationContext != null && declarationContext.hasRefinements())) {
            method = topMethod;
        } else {
            method = ModuleOperations.lookupMethodUncached(metaClass, name, declarationContext);
        }

        if (notFoundProfile.profile(method == null || method.isUndefined())) {
            return null;
        }

        return getInternalMethod(frame, config, metaClassNode, noCallerMethodProfile, publicProfile, privateProfile,
                isVisibleProfile, method);
    }

    // Check visibility
    private InternalMethod getInternalMethod(Frame frame, DispatchConfiguration config, MetaClassNode metaClassNode,
            ConditionProfile noCallerMethodProfile, ConditionProfile publicProfile,
            ConditionProfile privateProfile, ConditionProfile isVisibleProfile,
            InternalMethod method) {
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
                callerClass = coreLibrary().objectClass;
            } else {
                callerClass = metaClassNode.execute(RubyArguments.getSelf(frame));
            }

            if (!isVisibleProfile.profile(method.isProtectedMethodVisibleTo(callerClass))) {
                return null;
            }
        }

        return method;
    }

    protected static MethodLookupResult lookupCachedVTable(RubyContext context, Frame callingFrame,
            RubyClass metaClass, DispatchConfiguration config, String name) {
        CompilerAsserts.neverPartOfCompilation("slow-path method lookup should not be compiled");

        final int index = metaClass.methodNamesToIndex.lookup(name);
        final int len = metaClass.methodVTable.length;
        assert len == metaClass.methodAssumptions.length;

        if (index >= len) {
            ModuleFields.growVTable(metaClass, index, len);
        }

        final InternalMethod internalMethod = metaClass.methodVTable[index];
        final Assumption assumption = metaClass.methodAssumptions[index];
        final MethodLookupResult method = new MethodLookupResult(internalMethod, assumption);

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

    protected static MethodLookupResult lookupCached(RubyContext context, Frame callingFrame,
            RubyClass metaClass, String name, DispatchConfiguration config) {
        CompilerAsserts.neverPartOfCompilation("slow-path method lookup should not be compiled");

        final DeclarationContext declarationContext = getDeclarationContext(callingFrame, config);
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

    private static DeclarationContext getDeclarationContext(Frame frame, DispatchConfiguration config) {
        if (config.ignoreRefinements) {
            return null;
        }

        return frame == null ? null : RubyArguments.getDeclarationContext(frame);
    }

    protected static Map<RubyModule, RubyModule[]> getRefinements(Frame frame, DispatchConfiguration config) {
        if (config.ignoreRefinements) {
            return DeclarationContext.NO_REFINEMENTS;
        }

        return frame == null
                ? DeclarationContext.NO_REFINEMENTS
                : RubyArguments.getDeclarationContext(frame).getRefinements();
    }

    private static RubyClass getCallerClass(RubyContext context, Frame callingFrame) {
        final InternalMethod callerMethod = RubyArguments.tryGetMethod(callingFrame);
        if (callerMethod == null) {
            return context.getCoreLibrary().objectClass;
        } else {
            return MetaClassNode.getUncached().execute(RubyArguments.getSelf(callingFrame));
        }
    }

    protected int getCacheLimit() {
        return getLanguage().options.METHOD_LOOKUP_CACHE;
    }
}
