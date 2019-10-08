/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.language.objects;

import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;

public abstract class ShapeCachingGuards {

    public static boolean updateShape(DynamicObject object) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        boolean updated = object.updateShape();
        if (updated) {
            assert !SharedObjects.isShared(RubyLanguage.getCurrentContext(), object);
        }
        return updated;
    }

    public static boolean isArrayShape(Shape shape) {
        return Layouts.ARRAY.isArray(shape.getObjectType());
    }

    public static boolean isQueueShape(Shape shape) {
        return Layouts.QUEUE.isQueue(shape.getObjectType());
    }

    private static final ObjectType BASIC_OBJECT_OBJECT_TYPE = Layouts.BASIC_OBJECT
            .createBasicObjectShape(null, null)
            .getShape()
            .getObjectType();

    public static boolean isBasicObjectShape(Shape shape) {
        return shape.getObjectType().getClass() == BASIC_OBJECT_OBJECT_TYPE.getClass();
    }

}
