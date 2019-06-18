/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyGuards;

@ReportPolymorphism
@GenerateUncached
@ImportStatic({ RubyGuards.class, ShapeCachingGuards.class })
public abstract class ReadObjectFieldNode extends RubyBaseWithoutContextNode {

    public static ReadObjectFieldNode create() {
        return ReadObjectFieldNodeGen.create();
    }

    public abstract Object execute(DynamicObject object, Object name, Object defaultValue);

    @Specialization(
            guards = {"receiver.getShape() == cachedShape", "name == cachedName"},
            assumptions = "cachedShape.getValidAssumption()",
            limit = "getCacheLimit()")
    protected Object readObjectFieldCached(DynamicObject receiver, Object name, Object defaultValue,
            @Cached("receiver.getShape()") Shape cachedShape,
            @Cached("name") Object cachedName,
            @Cached("getProperty(cachedShape, cachedName)") Property cachedProperty) {
        return readOrDefault(receiver, cachedShape, cachedProperty, defaultValue);
    }

    @Specialization(guards = "updateShape(object)")
    public Object updateShapeAndRead(DynamicObject object, Object name, Object defaultValue) {
        return execute(object, name, defaultValue);
    }

    @Specialization(replaces = { "readObjectFieldCached", "updateShapeAndRead" })
    protected Object readObjectFieldUncached(DynamicObject receiver, Object name, Object defaultValue) {
        final Shape shape = receiver.getShape();
        final Property property = getProperty(shape, name);
        return readOrDefault(receiver, shape, property, defaultValue);
    }

    public static Property getProperty(Shape shape, Object name) {
        Property property = shape.getProperty(name);
        if (!PropertyFlags.isDefined(property)) {
            return null;
        }
        return property;
    }

    private static Object readOrDefault(DynamicObject object, Shape shape, Property property, Object defaultValue) {
        if (property != null) {
            return property.get(object, shape);
        } else {
            return defaultValue;
        }
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }
}
