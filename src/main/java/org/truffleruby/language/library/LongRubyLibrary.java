/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
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

@ExportLibrary(value = RubyLibrary.class, receiverType = Long.class)
@GenerateUncached
public class LongRubyLibrary {

    @ExportMessage
    protected static void freeze(Long object) {
    }

    @ExportMessage
    protected static boolean isFrozen(Long object) {
        return true;
    }

}
