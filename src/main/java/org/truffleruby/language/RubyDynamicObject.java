/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.core.basicobject.BasicObjectType;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.StringUtils;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

/** All Ruby DynamicObjects will eventually extend this.
 *
 * {@link org.truffleruby.Layouts} still use DynamicObjectImpl until migrated. */
@ExportLibrary(DynamicDispatchLibrary.class)
public abstract class RubyDynamicObject extends DynamicObject {

    public RubyDynamicObject(Shape shape) {
        super(shape);
    }

    public final RubyClass getLogicalClass() {
        return BasicObjectType.getLogicalClass(getShape());
    }

    public final RubyClass getMetaClass() {
        return BasicObjectType.getMetaClass(getShape());
    }

    @Override
    @TruffleBoundary
    public String toString() {
        final String className = getLogicalClass().fields.getName();
        return StringUtils.format("%s@%x<%s>", getClass().getSimpleName(), System.identityHashCode(this), className);
    }

    /** Each subclass should define its own Messages class, until all Layouts are migrated. */
    @ExportMessage
    public abstract Class<?> dispatch();

}
