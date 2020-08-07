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
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;

@GenerateUncached
@ImportStatic(ShapeCachingGuards.class)
public abstract class MetaClassNode extends RubyBaseNode {

    public static MetaClassNode create() {
        return MetaClassNodeGen.create();
    }

    public abstract RubyClass executeMetaClass(Object value);

    // Cover all primitives

    @Specialization(guards = "value")
    protected RubyClass metaClassTrue(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected RubyClass metaClassFalse(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().falseClass;
    }

    @Specialization
    protected RubyClass metaClassInt(int value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass metaClassLong(long value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().integerClass;
    }

    @Specialization
    protected RubyClass metaClassDouble(double value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().floatClass;
    }

    // nil

    @Specialization
    protected RubyClass metaClassNil(Nil value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().nilClass;
    }

    @Specialization
    protected RubyClass metaClassSymbol(RubySymbol value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().symbolClass;
    }

    // Cover all DynamicObject cases with cached and uncached

    @Specialization(
            guards = "object.getShape() == cachedShape",
            assumptions = "cachedShape.getValidAssumption()",
            limit = "getCacheLimit()")
    protected RubyClass metaClassCached(DynamicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            // used only during instantiation when it's always correct for a given object
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("getMetaClass(context, cachedShape)") RubyClass metaClass) {
        return metaClass;
    }

    @Specialization(guards = "updateShape(object)")
    protected RubyClass updateShapeAndMetaClass(DynamicObject object) {
        return executeMetaClass(object);
    }

    @Specialization(guards = "isRubyDynamicObject(object)", replaces = { "metaClassCached", "updateShapeAndMetaClass" })
    protected RubyClass metaClassUncached(DynamicObject object) {
        return Layouts.BASIC_OBJECT.getMetaClass(object);
    }

    @Specialization(
            guards = "!isRubyDynamicObject(object)",
            replaces = { "metaClassCached", "updateShapeAndMetaClass" })
    protected RubyClass metaClassForeign(DynamicObject object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        assert RubyGuards.isForeignObject(object);
        return context.getCoreLibrary().truffleInteropForeignClass;
    }

    @Specialization(guards = "isForeignObject(object)")
    protected RubyClass metaClassForeign(Object object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().truffleInteropForeignClass;
    }

    protected RubyClass getMetaClass(RubyContext context, Shape shape) {
        final ObjectType objectType = shape.getObjectType();
        if (Layouts.BASIC_OBJECT.isBasicObject(objectType)) {
            return Layouts.BASIC_OBJECT.getMetaClass(objectType);
        } else {
            return context.getCoreLibrary().truffleInteropForeignClass;
        }
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().CLASS_CACHE;
    }

}
