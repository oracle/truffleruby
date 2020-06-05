/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.Message;
import com.oracle.truffle.api.library.ReflectionLibrary;

@ExportLibrary(ReflectionLibrary.class)
public class ProxyForeignObject implements TruffleObject {

    protected final Object delegate;

    public ProxyForeignObject(Object delegate) {
        this.delegate = delegate;
    }

    @ExportMessage
    protected Object send(Message message, Object[] args,
                          @CachedLibrary("this.delegate") ReflectionLibrary reflections) throws Exception {
        return reflections.send(delegate, message, args);
    }
}
