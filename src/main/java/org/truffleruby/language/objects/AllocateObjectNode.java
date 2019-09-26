/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.source.SourceSection;

@ReportPolymorphism
@GenerateUncached
public abstract class AllocateObjectNode extends RubyBaseWithoutContextNode {

    public static AllocateObjectNode create() {
        return AllocateObjectNodeGen.create();
    }

    public DynamicObject allocate(DynamicObject classToAllocate, Object... values) {
        return executeAllocate(classToAllocate, values);
    }

    public DynamicObject allocateArray(
            DynamicObject classToAllocate,
            Object store,
            int size) {
        return allocate(classToAllocate, store, size);
    }

    protected abstract DynamicObject executeAllocate(DynamicObject classToAllocate, Object[] values);

    @Specialization(
            guards = { "cachedClassToAllocate == classToAllocate", "!cachedIsSingleton" },
            limit = "getCacheLimit()")
    protected DynamicObject allocateCached(
            DynamicObject classToAllocate,
            Object[] values,
            @Cached("classToAllocate") DynamicObject cachedClassToAllocate,
            @Cached("isSingleton(classToAllocate)") boolean cachedIsSingleton,
            @Cached("getInstanceFactory(classToAllocate)") DynamicObjectFactory factory,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached(
                    value = "lookupAllocationReporter()",
                    allowUncached = true) AllocationReporter allocationReporter) {

        return trace(language, context, allocate(allocationReporter, factory, values));
    }

    // because the factory is not constant
    @TruffleBoundary
    @Specialization(guards = { "!isSingleton(classToAllocate)" }, replaces = "allocateCached")
    protected DynamicObject allocateUncached(
            DynamicObject classToAllocate, Object[] values,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached(
                    value = "lookupAllocationReporter()",
                    allowUncached = true) AllocationReporter allocationReporter) {

        return trace(language, context, allocate(allocationReporter, getInstanceFactory(classToAllocate), values));
    }

    protected static AllocationReporter lookupAllocationReporter() {
        // The AllocationReporter is the same for all contexts inside an Engine
        return RubyLanguage.getCurrentContext().getEnv().lookup(AllocationReporter.class);
    }

    @Specialization(guards = "isSingleton(classToAllocate)")
    protected DynamicObject allocateSingleton(
            DynamicObject classToAllocate, Object[] values,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
    }

    private DynamicObject trace(RubyLanguage language, RubyContext context, DynamicObject object) {
        if (context.getObjectSpaceManager().isTracing(language)) {
            traceBoundary(context, object);
        }
        return object;
    }

    @TruffleBoundary
    private void traceBoundary(RubyContext context, DynamicObject object) {
        final ObjectSpaceManager objectSpaceManager = context.getObjectSpaceManager();
        if (!objectSpaceManager.isTracingPaused()) {
            objectSpaceManager.setTracingPaused(true);
            try {
                callTraceAllocation(context, object);
            } finally {
                objectSpaceManager.setTracingPaused(false);
            }
        }
    }

    @TruffleBoundary
    private void callTraceAllocation(RubyContext context, DynamicObject object) {
        final SourceSection allocatingSourceSection = context
                .getCallStack()
                .getTopMostUserSourceSection(getEncapsulatingSourceSection());

        final Frame allocatingFrame = context.getCallStack().getCurrentFrame(FrameAccess.READ_ONLY);

        final Object allocatingSelf = RubyArguments.getSelf(allocatingFrame);
        final String allocatingMethod = RubyArguments.getMethod(allocatingFrame).getName();

        context.send(
                context.getCoreLibrary().getObjectSpaceModule(),
                "trace_allocation",
                object,
                string(
                        context,
                        Layouts.CLASS.getFields(context.getCoreLibrary().getLogicalClass(allocatingSelf)).getName()),
                context.getSymbolTable().getSymbol(allocatingMethod),
                string(context, context.getPath(allocatingSourceSection.getSource())),
                allocatingSourceSection.getStartLine(),
                ObjectSpaceManager.getCollectionCount());
    }

    protected DynamicObjectFactory getInstanceFactory(DynamicObject classToAllocate) {
        return Layouts.CLASS.getInstanceFactory(classToAllocate);
    }

    private DynamicObject string(RubyContext context, String value) {
        // No point to use MakeStringNode (which uses AllocateObjectNode) here, as we should not
        // trace the allocation
        // of Strings used for tracing allocations.
        return StringOperations.createString(context, StringOperations.encodeRope(value, UTF8Encoding.INSTANCE));
    }

    protected static boolean isSingleton(DynamicObject classToAllocate) {
        return Layouts.CLASS.getIsSingleton(classToAllocate);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().ALLOCATE_CLASS_CACHE;
    }

    private DynamicObject allocate(AllocationReporter allocationReporter, DynamicObjectFactory factory,
            Object[] values) {
        if (allocationReporter.isActive()) {
            allocationReporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
        }

        final DynamicObject object = factory.newInstance(values);

        if (allocationReporter.isActive()) {
            allocationReporter.onReturnValue(object, 0, AllocationReporter.SIZE_UNKNOWN);
        }

        return object;
    }

}
