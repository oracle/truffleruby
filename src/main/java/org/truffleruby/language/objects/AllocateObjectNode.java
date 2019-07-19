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

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;

@ReportPolymorphism
public abstract class AllocateObjectNode extends RubyBaseNode {

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

    @Specialization(guards = {
            "cachedClassToAllocate == classToAllocate",
            "!cachedIsSingleton",
            "!isTracing()"
    }, assumptions = "getTracingAssumption()", limit = "getCacheLimit()")
    public DynamicObject allocateCached(
            DynamicObject classToAllocate,
            Object[] values,
            @Cached("classToAllocate") DynamicObject cachedClassToAllocate,
            @Cached("isSingleton(classToAllocate)") boolean cachedIsSingleton,
            @Cached("getInstanceFactory(classToAllocate)") DynamicObjectFactory factory) {
        return allocate(factory, values);
    }

    @CompilerDirectives.TruffleBoundary
    @Specialization(
            replaces = "allocateCached",
            guards = {"!isSingleton(classToAllocate)", "!isTracing()"},
            assumptions = "getTracingAssumption()")
    public DynamicObject allocateUncached(DynamicObject classToAllocate, Object[] values) {
        return allocate(getInstanceFactory(classToAllocate), values);
    }

    @CompilerDirectives.TruffleBoundary
    @Specialization(guards = {"!isSingleton(classToAllocate)", "isTracing()"},
                    assumptions = "getTracingAssumption()")
    public DynamicObject allocateTracing(DynamicObject classToAllocate, Object[] values) {
        final DynamicObject object = allocate(getInstanceFactory(classToAllocate), values);

        final ObjectSpaceManager objectSpaceManager = getContext().getObjectSpaceManager();
        if (!objectSpaceManager.isTracingPaused()) {
            objectSpaceManager.setTracingPaused(true);
            try {
                callTraceAllocation(object);
            } finally {
                objectSpaceManager.setTracingPaused(false);
            }
        }

        return object;
    }

    @CompilerDirectives.TruffleBoundary
    private void callTraceAllocation(DynamicObject object) {
        final SourceSection allocatingSourceSection = getContext().getCallStack().getTopMostUserSourceSection(getEncapsulatingSourceSection());

        final Frame allocatingFrame = getContext().getCallStack().getCurrentFrame(FrameAccess.READ_ONLY);

        final Object allocatingSelf = RubyArguments.getSelf(allocatingFrame);
        final String allocatingMethod = RubyArguments.getMethod(allocatingFrame).getName();

        getContext().send(coreLibrary().getObjectSpaceModule(), "trace_allocation",
                object,
                string(Layouts.CLASS.getFields(coreLibrary().getLogicalClass(allocatingSelf)).getName()),
                getSymbol(allocatingMethod),
                string(getContext().getPath(allocatingSourceSection.getSource())),
                allocatingSourceSection.getStartLine(),
                ObjectSpaceManager.getCollectionCount());
    }

    protected DynamicObjectFactory getInstanceFactory(DynamicObject classToAllocate) {
        return Layouts.CLASS.getInstanceFactory(classToAllocate);
    }

    private DynamicObject string(String value) {
        // No point to use MakeStringNode (which uses AllocateObjectNode) here, as we should not trace the allocation
        // of Strings used for tracing allocations.
        return StringOperations.createString(getContext(), StringOperations.encodeRope(value, UTF8Encoding.INSTANCE));
    }

    @Specialization(guards = "isSingleton(classToAllocate)")
    public DynamicObject allocateSingleton(DynamicObject classToAllocate, Object[] values) {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
    }

    protected Assumption getTracingAssumption() {
        return getContext().getObjectSpaceManager().getTracingAssumption();
    }

    protected boolean isTracing() {
        return getContext().getObjectSpaceManager().isTracing();
    }

    protected boolean isSingleton(DynamicObject classToAllocate) {
        return Layouts.CLASS.getIsSingleton(classToAllocate);
    }

    protected int getCacheLimit() {
        return getContext().getOptions().ALLOCATE_CLASS_CACHE;
    }

    private DynamicObject allocate(DynamicObjectFactory factory, Object[] values) {
        final AllocationReporter allocationReporter = getContext().getAllocationReporter();

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
