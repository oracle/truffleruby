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
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

@ReportPolymorphism
@GenerateUncached
public abstract class AllocateObjectNode extends RubyBaseNode {

    public static AllocateObjectNode create() {
        return AllocateObjectNodeGen.create();
    }

    public DynamicObject allocate(RubyClass classToAllocate, Object... values) {
        return executeAllocate(classToAllocate, values);
    }

    protected abstract DynamicObject executeAllocate(RubyClass classToAllocate, Object[] values);

    @Specialization(
            guards = { "cachedClassToAllocate == classToAllocate", "!cachedIsSingleton" },
            limit = "getCacheLimit()")
    protected DynamicObject allocateCached(RubyClass classToAllocate, Object[] values,
            @Cached("classToAllocate") RubyClass cachedClassToAllocate,
            @Cached("isSingleton(classToAllocate)") boolean cachedIsSingleton,
            @Cached("getInstanceFactory(classToAllocate)") DynamicObjectFactory factory,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        final DynamicObject instance = factory.newInstance(values);
        trace(language, context, instance);
        return instance;
    }

    // because the factory is not constant
    @TruffleBoundary
    @Specialization(guards = { "!isSingleton(classToAllocate)" }, replaces = "allocateCached")
    protected DynamicObject allocateUncached(RubyClass classToAllocate, Object[] values,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        final DynamicObject instance = getInstanceFactory(classToAllocate).newInstance(values);
        trace(language, context, instance);
        return instance;
    }

    @Specialization(guards = "isSingleton(classToAllocate)")
    protected DynamicObject allocateSingleton(RubyClass classToAllocate, Object[] values,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
    }

    private void trace(RubyLanguage language, RubyContext context, DynamicObject instance) {
        AllocationTracing.trace(language, context, instance, this);
    }

    protected DynamicObjectFactory getInstanceFactory(RubyClass classToAllocate) {
        return classToAllocate.instanceFactory;
    }

    protected static boolean isSingleton(RubyClass classToAllocate) {
        return classToAllocate.isSingleton;
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().ALLOCATE_CLASS_CACHE;
    }

}
