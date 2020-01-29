/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;

@ReportPolymorphism
@GenerateUncached
@ImportStatic({ RubyGuards.class, ShapeCachingGuards.class })
public abstract class HasFieldNode extends RubyBaseNode {

    public static HasFieldNode create() {
        return HasFieldNodeGen.create();
    }

    public abstract boolean execute(DynamicObject object, Object name);

    @Specialization(
            guards = { "receiver.getShape() == cachedShape", "name == cachedName" },
            assumptions = "cachedShape.getValidAssumption()",
            limit = "getCacheLimit()")
    protected boolean hasFieldCached(DynamicObject receiver, Object name,
            @Cached("receiver.getShape()") Shape cachedShape,
            @Cached("name") Object cachedName,
            @Cached("getProperty(cachedShape, cachedName)") Property cachedProperty) {
        return cachedProperty != null;
    }

    @Specialization(guards = "updateShape(object)")
    protected boolean updateShapeAndHasField(DynamicObject object, Object name) {
        return execute(object, name);
    }

    @TruffleBoundary
    @Specialization(replaces = { "hasFieldCached", "updateShapeAndHasField" })
    protected boolean hasFieldUncached(DynamicObject receiver, Object name) {
        final Shape shape = receiver.getShape();
        final Property property = getProperty(shape, name);
        return property != null;
    }

    @TruffleBoundary
    public static Property getProperty(Shape shape, Object name) {
        Property property = shape.getProperty(name);
        if (!PropertyFlags.isDefined(property)) {
            return null;
        }
        return property;
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }
}
