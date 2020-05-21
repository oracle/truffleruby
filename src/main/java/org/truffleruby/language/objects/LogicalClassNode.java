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
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@GenerateUncached
@ImportStatic(ShapeCachingGuards.class)
public abstract class LogicalClassNode extends RubyBaseNode {

    public static LogicalClassNode create() {
        return LogicalClassNodeGen.create();
    }

    public abstract DynamicObject executeLogicalClass(Object value);

    @Specialization(guards = "value")
    protected DynamicObject logicalClassTrue(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().trueClass;
    }

    @Specialization(guards = "!value")
    protected DynamicObject logicalClassFalse(boolean value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().falseClass;
    }

    @Specialization
    protected DynamicObject logicalClassInt(int value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().integerClass;
    }

    @Specialization
    protected DynamicObject logicalClassLong(long value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().integerClass;
    }

    @Specialization
    protected DynamicObject logicalClassDouble(double value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().floatClass;
    }

    @Specialization
    protected DynamicObject logicalClassNil(Nil value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().nilClass;
    }

    @Specialization
    protected DynamicObject logicalClassSymbol(RubySymbol value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().symbolClass;
    }

    @Specialization(
            guards = "object.getShape() == cachedShape",
            assumptions = "cachedShape.getValidAssumption()",
            limit = "getCacheLimit()")
    protected DynamicObject logicalClassCached(DynamicObject object,
            @Cached("object.getShape()") Shape cachedShape,
            @Cached("getLogicalClass(cachedShape)") DynamicObject logicalClass) {
        return logicalClass;
    }

    @Specialization(guards = "updateShape(object)")
    protected DynamicObject updateShapeAndLogicalClass(DynamicObject object) {
        return executeLogicalClass(object);
    }

    @Specialization(replaces = { "logicalClassCached", "updateShapeAndLogicalClass" })
    protected DynamicObject logicalClassUncached(DynamicObject object) {
        return Layouts.BASIC_OBJECT.getLogicalClass(object);
    }

    @Specialization(guards = "isForeignObject(object)")
    protected DynamicObject logicalClassForeign(Object object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().truffleInteropForeignClass;
    }

    protected static DynamicObject getLogicalClass(Shape shape) {
        return Layouts.BASIC_OBJECT.getLogicalClass(shape.getObjectType());
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().CLASS_CACHE;
    }

}
