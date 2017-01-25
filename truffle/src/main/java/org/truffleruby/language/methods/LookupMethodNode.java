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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyNode;
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

    public LookupMethodNode(boolean ignoreVisibility, boolean onlyLookupPublic) {
        this.ignoreVisibility = ignoreVisibility;
        this.onlyLookupPublic = onlyLookupPublic;
    }

    public abstract InternalMethod executeLookupMethod(VirtualFrame frame, Object self, String name);

    @Specialization(
            guards = {
                    "metaClass(self) == selfMetaClass",
                    "name == cachedName"
            },
            assumptions = "getUnmodifiedAssumption(selfMetaClass)",
            limit = "getCacheLimit()")
    protected InternalMethod lookupMethodCached(VirtualFrame frame, Object self, String name,
            @Cached("metaClass(self)") DynamicObject selfMetaClass,
            @Cached("name") String cachedName,
            @Cached("doLookup(frame, self, name)") MethodLookupResult method) {
        return method.getMethod();
    }

    protected Assumption getUnmodifiedAssumption(DynamicObject module) {
        return Layouts.MODULE.getFields(module).getMethodsUnmodifiedAssumption();
    }

    @Specialization
    protected InternalMethod lookupMethodUncached(VirtualFrame frame, Object self, String name) {
        return doLookup(frame, self, name).getMethod();
    }

    protected DynamicObject metaClass(Object object) {
        return metaClassNode.executeMetaClass(object);
    }

    protected MethodLookupResult doLookup(VirtualFrame frame, Object self, String name) {
        return lookupMethodWithVisibility(getContext(), frame, self, name, ignoreVisibility, onlyLookupPublic);
    }

    @TruffleBoundary
    protected static MethodLookupResult doLookup(RubyContext context,
            DynamicObject callerClass, Object receiver, String name,
            boolean ignoreVisibility, boolean onlyLookupPublic) {

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

    protected static DynamicObject getCallerClass(RubyContext context, VirtualFrame callingFrame,
            boolean ignoreVisibility, boolean onlyLookupPublic) {
        if (ignoreVisibility || onlyLookupPublic) {
            return null; // No need to check visibility
        } else {
            InternalMethod method = RubyArguments.getMethod(callingFrame);
            if (!context.getCoreLibrary().isSend(method)) {
                return context.getCoreLibrary().getMetaClass(RubyArguments.getSelf(callingFrame));
            } else {
                FrameInstance instance = context.getCallStack().getCallerFrameIgnoringSend();
                Frame callerFrame = instance.getFrame(FrameInstance.FrameAccess.READ_ONLY, true);
                return context.getCoreLibrary().getMetaClass(RubyArguments.getSelf(callerFrame));
            }
        }
    }

    public static MethodLookupResult lookupMethodWithVisibility(RubyContext context, VirtualFrame callingFrame,
            Object receiver, String name,
            boolean ignoreVisibility, boolean onlyLookupPublic) {
        DynamicObject callerClass = getCallerClass(context, callingFrame,
                ignoreVisibility, onlyLookupPublic);
        return doLookup(context, callerClass, receiver, name, ignoreVisibility, onlyLookupPublic);
    }

    protected int getCacheLimit() {
        return getContext().getOptions().METHOD_LOOKUP_CACHE;
    }

}
