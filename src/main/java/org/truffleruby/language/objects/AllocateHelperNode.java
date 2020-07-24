/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@ReportPolymorphism
@GenerateUncached
public abstract class AllocateHelperNode extends RubyBaseNode {

    public static AllocateHelperNode create() {
        return AllocateHelperNodeGen.create();
    }

    public Shape getCachedShape(DynamicObject classToAllocate) {
        return execute(classToAllocate);
    }

    protected abstract Shape execute(DynamicObject classToAllocate);

    // Convenience method when the context is guaranteed PE constant
    public void trace(RubyDynamicObject instance, RubyContextSourceNode contextNode) {
        final RubyContext context = contextNode.getContext();
        CompilerAsserts.partialEvaluationConstant(context);
        AllocationTracing.trace(context.getLanguage(), context, instance, contextNode);
    }

    public void trace(RubyLanguage language, RubyContext context, RubyDynamicObject instance) {
        AllocationTracing.trace(language, context, instance, this);
    }

    @Specialization(
            guards = { "cachedClassToAllocate == classToAllocate", "!cachedIsSingleton" },
            limit = "getCacheLimit()")
    protected Shape getShapeCached(DynamicObject classToAllocate,
            @Cached("classToAllocate") DynamicObject cachedClassToAllocate,
            @Cached("isSingleton(classToAllocate)") boolean cachedIsSingleton,
            @Cached("getInstanceShape(classToAllocate)") Shape cachedShape) {
        return cachedShape;
    }

    @Specialization(guards = "!isSingleton(classToAllocate)", replaces = "getShapeCached")
    protected Shape getShapeUncached(DynamicObject classToAllocate) {
        return getInstanceShape(classToAllocate);
    }

    @Specialization(guards = "isSingleton(classToAllocate)")
    protected Shape allocateSingleton(DynamicObject classToAllocate,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
    }

    protected Shape getInstanceShape(DynamicObject classToAllocate) {
        return Layouts.CLASS.getInstanceFactory(classToAllocate).getShape();
    }

    protected static boolean isSingleton(DynamicObject classToAllocate) {
        return Layouts.CLASS.getIsSingleton(classToAllocate);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().ALLOCATE_CLASS_CACHE;
    }

}
