/*
 * Copyright (c) 2015, 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;

@GenerateUncached
@ImportStatic(ShapeCachingGuards.class)
public abstract class MetaClassNode extends RubyBaseWithoutContextNode {

    public static MetaClassNode create() {
        return MetaClassNodeGen.create();
    }

    public abstract DynamicObject executeMetaClass(Object value);

    // Cover all primitives

    @Specialization(guards = "value")
    protected DynamicObject metaClassTrue(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getTrueClass();
    }

    @Specialization(guards = "!value")
    protected DynamicObject metaClassFalse(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getFalseClass();
    }

    @Specialization
    protected DynamicObject metaClassInt(int value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getIntegerClass();
    }

    @Specialization
    protected DynamicObject metaClassLong(long value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getIntegerClass();
    }

    @Specialization
    protected DynamicObject metaClassDouble(double value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getFloatClass();
    }

    // Cover all DynamicObject cases with cached and uncached

    @Specialization(guards = "object.getShape() == cachedShape", assumptions = "cachedShape.getValidAssumption()", limit = "getCacheLimit()")
    protected DynamicObject metaClassCached(DynamicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            // used only during instantiation when it's always correct for a given object
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("getMetaClass(context, cachedShape)") DynamicObject metaClass) {
        return metaClass;
    }

    @Specialization(guards = "updateShape(object)")
    protected DynamicObject updateShapeAndMetaClass(DynamicObject object) {
        return executeMetaClass(object);
    }

    @Specialization(guards = "isRubyBasicObject(object)", replaces = { "metaClassCached", "updateShapeAndMetaClass" })
    protected DynamicObject metaClassUncached(DynamicObject object) {
        return Layouts.BASIC_OBJECT.getMetaClass(object);
    }

    @Specialization(guards = "!isRubyBasicObject(object)", replaces = { "metaClassCached", "updateShapeAndMetaClass" })
    protected DynamicObject metaClassForeign(DynamicObject object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getTruffleInteropForeignClass();
    }

    // Cover remaining non objects which are not primitives nor DynamicObject

    @Specialization(guards = { "!isPrimitive(object)", "!isDynamicObject(object)" })
    protected DynamicObject metaClassFallback(Object object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().getTruffleInteropForeignClass();
    }

    protected DynamicObject getMetaClass(RubyContext context, Shape shape) {
        final ObjectType objectType = shape.getObjectType();
        if (Layouts.BASIC_OBJECT.isBasicObject(objectType)) {
            return Layouts.BASIC_OBJECT.getMetaClass(objectType);
        } else {
            return context.getCoreLibrary().getTruffleInteropForeignClass();
        }
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().CLASS_CACHE;
    }

}
