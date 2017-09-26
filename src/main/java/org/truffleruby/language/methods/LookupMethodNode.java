/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.MetaClassNodeGen;

/**
 * Caches {@link ModuleOperations#lookupMethod(DynamicObject, String)}
 * on an actual instance.
 */
@NodeChildren({ @NodeChild("self"), @NodeChild("name") })
public abstract class LookupMethodNode extends RubyNode {

    private final boolean ignoreVisibility;
    private final boolean onlyLookupPublic;

    @Child private MetaClassNode metaClassNode = MetaClassNodeGen.create(null);

    public static LookupMethodNode create() {
        return LookupMethodNodeGen.create(false, false, null, null);
    }

    public static LookupMethodNode createIgnoreVisibility() {
        return LookupMethodNodeGen.create(true, false, null, null);
    }

    public LookupMethodNode(boolean ignoreVisibility, boolean onlyLookupPublic) {
        this.ignoreVisibility = ignoreVisibility;
        this.onlyLookupPublic = onlyLookupPublic;
    }

    public InternalMethod lookup(VirtualFrame frame, Object self, String name) {
        return executeLookupMethod(frame, self, name);
    }

    public abstract InternalMethod executeLookupMethod(VirtualFrame frame, Object self, String name);

    @Specialization(
            guards = {
                    "metaClass(self) == selfMetaClass",
                    "name == cachedName"
            },
            assumptions = "method.getAssumptions()",
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodCached(VirtualFrame frame, Object self, String name,
            @Cached("metaClass(self)") DynamicObject selfMetaClass,
            @Cached("name") String cachedName,
            @Cached("doCachedLookup(frame, self, name)") MethodLookupResult method) {
        return method.getMethod();
    }

    @Specialization(replaces = "lookupMethodCached")
    protected InternalMethod lookupMethodUncached(VirtualFrame frame, Object self, String name,
            @Cached("create()") MetaClassNode callerMetaClassNode,
            @Cached("createBinaryProfile()") ConditionProfile noCallerMethodProfile,
            @Cached("createBinaryProfile()") ConditionProfile isSendProfile,
            @Cached("create()") BranchProfile foreignProfile,
            @Cached("createBinaryProfile()") ConditionProfile noPrependedModulesProfile,
            @Cached("createBinaryProfile()") ConditionProfile onMetaClassProfile,
            @Cached("createBinaryProfile()") ConditionProfile foundProfile,
            @Cached("createBinaryProfile()") ConditionProfile publicProfile) {
        // Actual lookup

        final DynamicObject metaClass = metaClass(self);

        if (metaClass == coreLibrary().getTruffleInteropForeignClass()) {
            foreignProfile.enter();
            throw new UnsupportedOperationException("method lookup not supported on foreign objects");
        }

        final InternalMethod method;
        // Lookup first in the metaclass as we are likely to find the method there
        final ModuleFields fields = Layouts.MODULE.getFields(metaClass);
        InternalMethod topMethod;
        if (noPrependedModulesProfile.profile(fields.getFirstModuleChain() == fields) &&
                onMetaClassProfile.profile((topMethod = fields.getMethod(name)) != null)) {
            method = topMethod;
        } else {
            method = ModuleOperations.lookupMethodUncached(metaClass, name);
        }

        if (foundProfile.profile(method == null || method.isUndefined())) {
            return null;
        }

        // Check visibility

        if (publicProfile.profile(method.getVisibility() == Visibility.PUBLIC)) {
            return method;
        }

        if (!ignoreVisibility) {
            if (onlyLookupPublic) {
                return null;
            }

            // Find the caller class
            final DynamicObject callerClass;
            final InternalMethod callerMethod = RubyArguments.tryGetMethod(frame);

            if (noCallerMethodProfile.profile(callerMethod == null)) {
                callerClass = coreLibrary().getObjectClass();
            } else if (!isSendProfile.profile(coreLibrary().isSend(callerMethod))) {
                callerClass = callerMetaClassNode.executeMetaClass(RubyArguments.getSelf(frame));
            } else {
                FrameInstance instance = getContext().getCallStack().getCallerFrameIgnoringSend();
                Frame callerFrame = instance.getFrame(FrameInstance.FrameAccess.READ_ONLY);
                callerClass = callerMetaClassNode.executeMetaClass(RubyArguments.getSelf(callerFrame));
            }

            if (!method.isVisibleTo(callerClass)) {
                return null;
            }
        }

        return method;
    }

    protected DynamicObject metaClass(Object object) {
        return metaClassNode.executeMetaClass(object);
    }

    protected MethodLookupResult doCachedLookup(VirtualFrame frame, Object self, String name) {
        return lookupMethodWithVisibility(getContext(), frame, self, name, ignoreVisibility, onlyLookupPublic);
    }

    public static MethodLookupResult lookupMethodWithVisibility(RubyContext context, VirtualFrame callingFrame,
            Object receiver, String name,
            boolean ignoreVisibility, boolean onlyLookupPublic) {
        DynamicObject callerClass = getCallerClass(context, callingFrame,
                ignoreVisibility, onlyLookupPublic);
        return doLookup(context, callerClass, receiver, name, ignoreVisibility, onlyLookupPublic);
    }

    protected static DynamicObject getCallerClass(RubyContext context, VirtualFrame callingFrame,
            boolean ignoreVisibility, boolean onlyLookupPublic) {
        if (ignoreVisibility || onlyLookupPublic) {
            return null; // No need to check visibility
        } else {
            InternalMethod method = RubyArguments.tryGetMethod(callingFrame);

            if (method == null) {
                return context.getCoreLibrary().getObjectClass();
            } else if (!context.getCoreLibrary().isSend(method)) {
                return context.getCoreLibrary().getMetaClass(RubyArguments.getSelf(callingFrame));
            } else {
                FrameInstance instance = context.getCallStack().getCallerFrameIgnoringSend();
                Frame callerFrame = instance.getFrame(FrameInstance.FrameAccess.READ_ONLY);
                return context.getCoreLibrary().getMetaClass(RubyArguments.getSelf(callerFrame));
            }
        }
    }

    @TruffleBoundary
    protected static MethodLookupResult doLookup(RubyContext context,
            DynamicObject callerClass, Object receiver, String name,
            boolean ignoreVisibility, boolean onlyLookupPublic) {
        if (RubyGuards.isForeignObject(receiver)) {
            throw new UnsupportedOperationException("method lookup not supported on foreign objects");
        }

        final MethodLookupResult method = ModuleOperations.lookupMethod(context.getCoreLibrary().getMetaClass(receiver), name);

        if (!method.isDefined()) {
            return method.withNoMethod();
        }

        // Check visibility
        if (!ignoreVisibility) {
            if (onlyLookupPublic) {
                if (!method.getMethod().getVisibility().isPublic()) {
                    return method.withNoMethod();
                }
            } else if (!method.getMethod().isVisibleTo(callerClass)) {
                return method.withNoMethod();
            }
        }

        return method;
    }

    protected int getCacheLimit() {
        return getContext().getOptions().METHOD_LOOKUP_CACHE;
    }

}
