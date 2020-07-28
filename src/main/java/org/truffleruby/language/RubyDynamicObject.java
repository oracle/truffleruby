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

    /** Each subclass should define its own Messages class, until all Layouts are migrated. */
    @ExportMessage
    public abstract Class<?> dispatch();

}
