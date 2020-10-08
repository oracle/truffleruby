/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
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
    protected RubyClass singletonClassTrue(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected RubyClass singletonClassFalse(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().falseClass;
    }

    @Specialization
    protected RubyClass singletonClassNil(Nil value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().nilClass;
    }

    @Specialization
    protected RubyClass singletonClass(int value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return noSingletonClass(context);
    }

    @Specialization
    protected RubyClass singletonClass(long value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return noSingletonClass(context);
    }

    @Specialization
    protected RubyClass singletonClass(double value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return noSingletonClass(context);
    }

    @Specialization
    protected RubyClass singletonClassBignum(RubyBignum value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return noSingletonClass(context);
    }

    @Specialization
    protected RubyClass singletonClassSymbol(RubySymbol value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return noSingletonClass(context);
    }

    @Specialization(
            guards = { "rubyClass == cachedClass", "cachedSingletonClass != null" },
            limit = "getIdentityCacheLimit()")
    protected RubyClass singletonClassClassCached(RubyClass rubyClass,
            @Cached("rubyClass") RubyClass cachedClass,
            @Cached("getSingletonClassOrNull(cachedClass)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(replaces = "singletonClassClassCached")
    protected RubyClass singletonClassClassUncached(RubyClass rubyClass,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return ClassNodes.getSingletonClass(context, rubyClass);
    }

    @Specialization(
            guards = { "object == cachedObject", "!isRubyClass(cachedObject)" },
            limit = "getIdentityCacheLimit()")
    protected RubyClass singletonClassInstanceCached(RubyDynamicObject object,
            @Cached("object") RubyDynamicObject cachedObject,
            @Cached("getSingletonClassForInstance(object)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(guards = "!isRubyClass(object)", replaces = "singletonClassInstanceCached")
    protected RubyClass singletonClassInstanceUncached(RubyDynamicObject object) {
        return getSingletonClassForInstance(object);
    }

    private RubyClass noSingletonClass(RubyContext context) {
        throw new RaiseException(context, context.getCoreExceptions().typeErrorCantDefineSingleton(this));
    }

    protected RubyClass getSingletonClassOrNull(RubyClass rubyClass) {
        return ClassNodes.getSingletonClassOrNull(rubyClass.fields.getContext(), rubyClass);
    }

    @TruffleBoundary
    protected RubyClass getSingletonClassForInstance(RubyDynamicObject object) {
        synchronized (object) {
            RubyClass metaClass = object.getMetaClass();
            if (metaClass.isSingleton) {
                return metaClass;
            }
            final RubyContext context = metaClass.fields.getContext();

            final RubyClass logicalClass = object.getLogicalClass();

            final String name = StringUtils.format(
                    "#<Class:#<%s:0x%x>>",
                    logicalClass.fields.getName(),
                    ObjectIDNode.getUncached().execute(object));

            final RubyClass singletonClass = ClassNodes.createSingletonClassOfObject(
                    context,
                    getEncapsulatingSourceSection(),
                    logicalClass,
                    object,
                    name);

            if (RubyLibrary.getUncached().isFrozen(object)) {
                RubyLibrary.getUncached().freeze(singletonClass);
            }

            SharedObjects.propagate(context, object, singletonClass);
            object.setMetaClass(singletonClass);

            return singletonClass;
        }
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().CLASS_CACHE;
    }

    protected int getIdentityCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().IDENTITY_CACHE;
    }

}
