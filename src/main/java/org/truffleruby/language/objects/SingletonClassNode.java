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
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.RubyBignum;
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

    @Specialization
    protected RubyClass singletonClassBignum(RubyBignum value) {
        return noSingletonClass();
    }

    @Specialization
    protected RubyClass singletonClassSymbol(RubySymbol value) {
        return noSingletonClass();
    }

    @Specialization(
            guards = {
                    "rubyClass.getShape() == cachedShape",
                    "cachedSingletonClass != null" },
            limit = "getCacheLimit()")
    protected RubyClass singletonClassClassCached(RubyClass rubyClass,
            @Cached("rubyClass.getShape()") Shape cachedShape,
            @Cached("getSingletonClassOrNull(rubyClass)") RubyClass cachedSingletonClass) {

        return cachedSingletonClass;
    }

    @Specialization(replaces = "singletonClassClassCached")
    protected RubyClass singletonClassClassUncached(RubyClass rubyClass) {
        return ClassNodes.getSingletonClass(getContext(), rubyClass);
    }

    @Specialization(
            guards = {
                    "object == cachedObject",
                    "!isRubyBignum(cachedObject)",
                    "!isRubyClass(cachedObject)" },
            limit = "getIdentityCacheLimit()")
    protected RubyClass singletonClassInstanceCached(DynamicObject object,
            @Cached("object") DynamicObject cachedObject,
            @Cached("getSingletonClassForInstance(object)") RubyClass cachedSingletonClass) {
        return cachedSingletonClass;
    }

    @Specialization(
            guards = { "!isRubyBignum(object)", "!isRubyClass(object)" },
            replaces = "singletonClassInstanceCached")
    protected RubyClass singletonClassInstanceUncached(DynamicObject object) {
        return getSingletonClassForInstance(object);
    }

    private RubyClass noSingletonClass() {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantDefineSingleton(this));
    }

    protected RubyClass getSingletonClassOrNull(RubyClass rubyClass) {
        return ClassNodes.getSingletonClassOrNull(getContext(), rubyClass);
    }

    @TruffleBoundary
    protected RubyClass getSingletonClassForInstance(DynamicObject object) {
        synchronized (object) {
            RubyClass metaClass = Layouts.BASIC_OBJECT.getMetaClass(object);
            if (metaClass.isSingleton) {
                return metaClass;
            }

            final RubyClass logicalClass = Layouts.BASIC_OBJECT.getLogicalClass(object);

            final String name = StringUtils.format(
                    "#<Class:#<%s:0x%x>>",
                    logicalClass.fields.getName(),
                    ObjectIDNode.getUncached().execute(object));

            final RubyClass singletonClass = ClassNodes.createSingletonClassOfObject(
                    getContext(),
                    getEncapsulatingSourceSection(),
                    logicalClass,
                    object,
                    name);

            if (RubyLibrary.getUncached().isFrozen(object)) {
                RubyLibrary.getUncached().freeze(singletonClass);
            }

            SharedObjects.propagate(getContext(), object, singletonClass);

            ClassNodes.setMetaClass(object, singletonClass);
            return singletonClass;
        }
    }

    protected int getCacheLimit() {
        return getContext().getOptions().CLASS_CACHE;
    }

}
