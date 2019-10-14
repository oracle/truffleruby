/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;

/**
 * Represents a value that was not provided by the user, such as optional arguments to a core library node.
 */
@ExportLibrary(InteropLibrary.class)
public final class NotProvided implements TruffleObject {

    public static final NotProvided INSTANCE = new NotProvided();

    private NotProvided() {
    }
}
