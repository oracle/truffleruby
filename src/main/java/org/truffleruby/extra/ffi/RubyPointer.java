/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra.ffi;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(InteropLibrary.class)
public class RubyPointer extends RubyDynamicObject {

    public Pointer pointer;

    public RubyPointer(RubyClass rubyClass, Shape shape, Pointer pointer) {
        super(rubyClass, shape);
        this.pointer = pointer;
    }

    // region Pointer
    @ExportMessage
    public boolean isPointer() {
        return true;
    }

    @ExportMessage
    public long asPointer() {
        return pointer.getAddress();
    }

    @ExportMessage
    public void toNative() {
    }
    // endregion

}
