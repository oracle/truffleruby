/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public class GlobalVariableReader {

    private final String name;
    @CompilationFinal private GlobalVariableStorage storage;

    GlobalVariableReader(GlobalVariables globalVariables, String name) {
        this.name = name;
        this.storage = globalVariables.getStorage(name);
    }

    public Object getValue(GlobalVariables globalVariables) {
        if (!storage.getValidAssumption().isValid()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            storage = globalVariables.getStorage(name);
        }

        return storage.getValue();
    }

}
