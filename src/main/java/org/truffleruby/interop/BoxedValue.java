/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.Message;
import com.oracle.truffle.api.library.ReflectionLibrary;

@ExportLibrary(ReflectionLibrary.class)
public class BoxedValue implements TruffleObject {

    private final Object value;

    public BoxedValue(Object value) {
        this.value = value;
    }

    @TruffleBoundary
    @ExportMessage
    protected Object send(Message message, Object[] args) throws Exception {
        ReflectionLibrary reflection = ReflectionLibrary.getFactory().getUncached();
        return reflection.send(value, message, args);
    }

}
