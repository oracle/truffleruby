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

@ExportLibrary(value = RubyLibrary.class, receiverType = Boolean.class)
@GenerateUncached
public class BooleanRubyLibrary {

    @ExportMessage
    protected static void freeze(Boolean object) {
    }

    @ExportMessage
    protected static boolean isFrozen(Boolean object) {
        return true;
    }

    @ExportMessage
    protected static boolean isTainted(Boolean object) {
        return false;
    }

    @ExportMessage
    protected static void taint(Boolean object) {
    }

    @ExportMessage
    protected static void untaint(Boolean object) {
    }

}
