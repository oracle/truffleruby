/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra.ffi;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyPointerMessages;
import org.truffleruby.language.RubyDynamicObject;

public class RubyPointer extends RubyDynamicObject {

    public Pointer pointer;

    public RubyPointer(Shape shape, Pointer pointer) {
        super(shape);
        this.pointer = pointer;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyPointerMessages.class;
    }

}
