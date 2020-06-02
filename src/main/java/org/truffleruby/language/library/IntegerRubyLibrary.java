/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.library;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.language.RubyLibrary;

@ExportLibrary(value = RubyLibrary.class, receiverType = Integer.class)
@GenerateUncached
public class IntegerRubyLibrary {

    @ExportMessage
    protected static Object freeze(Integer object) {
        return object;
    }

    @ExportMessage
    protected static boolean isFrozen(Integer object) {
        return true;
    }

    @ExportMessage
    protected static boolean isTainted(Integer object) {
        return false;
    }

    @ExportMessage
    protected static Object taint(Integer object) {
        return object;
    }

    @ExportMessage
    protected static Object untaint(Integer object) {
        return object;
    }

}
