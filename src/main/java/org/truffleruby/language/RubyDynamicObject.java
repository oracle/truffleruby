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

import org.truffleruby.interop.messages.RubyDynamicObjectMessages;

import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

/** All Ruby DynamicObjects will eventually extend this.
 *
 * {@link org.truffleruby.Layouts} still use DynamicObjectImpl until migrated. */
@ExportLibrary(DynamicDispatchLibrary.class)
public class RubyDynamicObject extends DynamicObject {

    public RubyDynamicObject(Shape shape) {
        super(shape);
    }

    // Same dispatch as in DynamicObjectImpl, until all Layouts are migrated

    @ExportMessage
    protected Class<?> dispatch() {
        return RubyDynamicObjectMessages.class;
    }

}
