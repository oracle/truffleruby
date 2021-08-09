/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@NodeChild(value = "value", type = RubyNode.class)
public abstract class SingletonClassNode extends RubySourceNode {

    public static SingletonClassNode getUncached() {
        return SingletonClassNodeGen.getUncached();
    }

    public static SingletonClassNode create() {
        return SingletonClassNodeGen.create(null);
    }

    public abstract RubyClass executeSingletonClass(Object value);

    @Specialization(guards = "value")
    protected RubyClass singletonClassTrue(boolean value) {
        return coreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected RubyClass singletonClassFalse(boolean value) {
        return coreLibrary().falseClass;
    }

    @Specialization
    protected RubyClass singletonClassNil(Nil value) {
        return coreLibrary().nilClass;
    }

    @Specialization
    protected RubyClass singletonClass(int value) {
        return noSingletonClass();
    }

    @Specialization
    protected RubyClass singletonClass(long value) {
        return noSingletonClass();
    }

    @Specialization
    protected RubyClass singletonClass(double value) {
        return noSingletonClass();
    }

    @Specialization(guards = "!isNil(value)")
    protected RubyClass singletonClassImmutableObject(ImmutableRubyObject value) {
        return noSingletonClass();
    }

    @Specialization(
            // no need to guard on the context, the rubyClass is context-specific
            guards = { "rubyClass == cachedClass", "cachedSingletonClass != null" },
            limit = "getIdentityCacheContextLimit()")
    protected RubyClass singletonClassClassCached(RubyClass rubyClass,
            @Cached("rubyClass") RubyClass cachedClass,
            @Cached("getSingletonClassOrNull(getContext(), cachedClass)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(replaces = "singletonClassClassCached")
    protected RubyClass singletonClassClassUncached(RubyClass rubyClass) {
        return ClassNodes.getSingletonClass(getContext(), rubyClass);
    }

    @Specialization(
            // no need to guard on the context, the RubyDynamicObject is context-specific
            guards = { "object == cachedObject", "!isRubyClass(cachedObject)" },
            limit = "getIdentityCacheContextLimit()")
    protected RubyClass singletonClassInstanceCached(RubyDynamicObject object,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("getSingletonClassForInstance(getContext(), object)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(guards = "!isRubyClass(object)", replaces = "singletonClassInstanceCached")
    protected RubyClass singletonClassInstanceUncached(RubyDynamicObject object) {
        return getSingletonClassForInstance(getContext(), object);
    }

    private RubyClass noSingletonClass() {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantDefineSingleton(this));
    }

    protected RubyClass getSingletonClassOrNull(RubyContext context, RubyClass rubyClass) {
        return ClassNodes.getSingletonClassOrNull(context, rubyClass);
    }

    @TruffleBoundary
    protected RubyClass getSingletonClassForInstance(RubyContext context, RubyDynamicObject object) {
        synchronized (object) {
            RubyClass metaClass = object.getMetaClass();
            if (metaClass.isSingleton) {
                return metaClass;
            }

            final RubyClass logicalClass = object.getLogicalClass();

            final RubyClass singletonClass = ClassNodes.createSingletonClassOfObject(
                    context,
                    getEncapsulatingSourceSection(),
                    logicalClass,
                    object);

            if (RubyLibrary.getUncached().isFrozen(object)) {
                RubyLibrary.getUncached().freeze(singletonClass);
            }

            SharedObjects.propagate(context.getLanguageSlow(), object, singletonClass);
            object.setMetaClass(singletonClass);

            return singletonClass;
        }
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentLanguage().options.CLASS_CACHE;
    }

    protected int getIdentityCacheContextLimit() {
        return RubyLanguage.getCurrentLanguage().options.CONTEXT_SPECIFIC_IDENTITY_CACHE;
    }

}
