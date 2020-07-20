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

import org.truffleruby.Layouts;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@NodeChild(value = "value", type = RubyNode.class)
public abstract class SingletonClassNode extends RubyContextSourceNode {

    public static SingletonClassNode create() {
        return SingletonClassNodeGen.create(null);
    }

    public abstract DynamicObject executeSingletonClass(Object value);

    @Specialization(guards = "value")
    protected DynamicObject singletonClassTrue(boolean value) {
        return coreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected DynamicObject singletonClassFalse(boolean value) {
        return coreLibrary().falseClass;
    }

    @Specialization
    protected DynamicObject singletonClassNil(Nil value) {
        return coreLibrary().nilClass;
    }

    @Specialization
    protected DynamicObject singletonClass(int value) {
        return noSingletonClass();
    }

    @Specialization
    protected DynamicObject singletonClass(long value) {
        return noSingletonClass();
    }

    @Specialization
    protected DynamicObject singletonClass(double value) {
        return noSingletonClass();
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected DynamicObject singletonClassBignum(DynamicObject value) {
        return noSingletonClass();
    }

    @Specialization
    protected DynamicObject singletonClassSymbol(RubySymbol value) {
        return noSingletonClass();
    }

    @Specialization(
            guards = {
                    "isRubyClass(rubyClass)",
                    "rubyClass.getShape() == cachedShape",
                    "cachedSingletonClass != null" },
            limit = "getCacheLimit()")
    protected DynamicObject singletonClassClassCached(DynamicObject rubyClass,
            @Cached("rubyClass.getShape()") Shape cachedShape,
            @Cached("getSingletonClassOrNull(rubyClass)") DynamicObject cachedSingletonClass) {

        return cachedSingletonClass;
    }

    @Specialization(guards = "isRubyClass(rubyClass)", replaces = "singletonClassClassCached")
    protected DynamicObject singletonClassClassUncached(DynamicObject rubyClass) {
        return ClassNodes.getSingletonClass(getContext(), rubyClass);
    }

    @Specialization(
            guards = {
                    "object == cachedObject",
                    "!isRubyBignum(cachedObject)",
                    "!isRubyClass(cachedObject)" },
            limit = "getIdentityCacheLimit()")
    protected DynamicObject singletonClassInstanceCached(DynamicObject object,
            @Cached("object") DynamicObject cachedObject,
            @Cached("getSingletonClassForInstance(object)") DynamicObject cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(
            guards = { "!isRubyBignum(object)", "!isRubyClass(object)" },
            replaces = "singletonClassInstanceCached")
    protected DynamicObject singletonClassInstanceUncached(DynamicObject object) {
        return getSingletonClassForInstance(object);
    }

    private DynamicObject noSingletonClass() {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantDefineSingleton(this));
    }

    protected DynamicObject getSingletonClassOrNull(DynamicObject object) {
        return ClassNodes.getSingletonClassOrNull(getContext(), object);
    }

    @TruffleBoundary
    protected DynamicObject getSingletonClassForInstance(DynamicObject object) {
        synchronized (object) {
            DynamicObject metaClass = Layouts.BASIC_OBJECT.getMetaClass(object);
            if (Layouts.CLASS.getIsSingleton(metaClass)) {
                return metaClass;
            }

            final DynamicObject logicalClass = Layouts.BASIC_OBJECT.getLogicalClass(object);

            final String name = StringUtils.format(
                    "#<Class:#<%s:0x%x>>",
                    Layouts.MODULE.getFields(logicalClass).getName(),
                    ObjectIDNode.getUncached().execute(object));

            final DynamicObject singletonClass = ClassNodes.createSingletonClassOfObject(
                    getContext(),
                    getEncapsulatingSourceSection(),
                    logicalClass,
                    object,
                    name);

            if (RubyLibrary.getUncached().isFrozen(object)) {
                RubyLibrary.getUncached().freeze(singletonClass);
            }

            SharedObjects.propagate(getContext(), object, singletonClass);

            Layouts.BASIC_OBJECT.setMetaClass(object, singletonClass);
            return singletonClass;
        }
    }

    protected int getCacheLimit() {
        return getContext().getOptions().CLASS_CACHE;
    }

}
