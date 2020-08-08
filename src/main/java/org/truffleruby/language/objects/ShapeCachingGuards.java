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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.basicobject.BasicObjectType;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.shared.SharedObjects;

public abstract class ShapeCachingGuards {

    public static boolean updateShape(DynamicObject object) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        // TODO (eregon, 14 July 2020): review callers, once they use the library they should not need to update the Shape manually anymore
        boolean updated = DynamicObjectLibrary.getUncached().updateShape(object);
        if (updated) {
            assert !SharedObjects.isShared(RubyLanguage.getCurrentContext(), object);
        }
        return updated;
    }

    public static boolean isBasicObjectShape(Shape shape) {
        return shape.getObjectType().getClass() == BasicObjectType.class &&
                // TODO: when all layouts are migrated, this can accept all RubyDynamicObject subclasses with no internal fields
                !RubyDynamicObject.class.isAssignableFrom(shape.getLayout().getType());
    }

}
