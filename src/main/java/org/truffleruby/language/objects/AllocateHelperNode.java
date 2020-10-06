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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;

@ReportPolymorphism
@GenerateUncached
public abstract class AllocateHelperNode extends RubyBaseNode {

    public static AllocateHelperNode create() {
        return AllocateHelperNodeGen.create();
    }

    public Shape getCachedShape(RubyClass classToAllocate) {
        return execute(classToAllocate);
    }

    protected abstract Shape execute(RubyClass classToAllocate);

    // Convenience method when the context is guaranteed PE constant
    public void trace(RubyDynamicObject instance, RubyNode.WithContext contextNode, RubyLanguage language) {
        final RubyContext context = contextNode.getContext();
        CompilerAsserts.partialEvaluationConstant(context);
        CompilerAsserts.partialEvaluationConstant(language);
        AllocationTracing.trace(language, context, instance, (Node) contextNode);
    }

    public void trace(RubyLanguage language, RubyContext context, RubyDynamicObject instance) {
        AllocationTracing.trace(language, context, instance, this);
    }

    @Specialization(
            guards = { "cachedClassToAllocate == classToAllocate", "!cachedIsSingleton" },
            limit = "getCacheLimit()")
    protected Shape getShapeCached(RubyClass classToAllocate,
            @Cached("classToAllocate") RubyClass cachedClassToAllocate,
            @Cached("isSingleton(classToAllocate)") boolean cachedIsSingleton,
            @Cached("classToAllocate.instanceShape") Shape cachedInstanceShape) {
        return cachedInstanceShape;
    }

    @Specialization(guards = "!isSingleton(classToAllocate)", replaces = "getShapeCached")
    protected Shape getShapeUncached(RubyClass classToAllocate) {
        return classToAllocate.instanceShape;
    }

    @Specialization(guards = "isSingleton(classToAllocate)")
    protected Shape allocateSingleton(RubyClass classToAllocate,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
    }

    protected static boolean isSingleton(RubyClass classToAllocate) {
        return classToAllocate.isSingleton;
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().ALLOCATE_CLASS_CACHE;
    }

}
