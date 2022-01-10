/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.SharedIndicesMap.ContextArray;

/** A helper class to read global variables on the slow path, supporting the GlobalVariableStorage to be replaced. */
public class GlobalVariableReader {

    private final int index;
    private final ContextArray<GlobalVariableStorage> globalVariablesArray;

    private final Assumption globalVariableAliasedAssumption;
    private GlobalVariableStorage unaliasedStorage;

    GlobalVariableReader(RubyLanguage language, String name, ContextArray<GlobalVariableStorage> globalVariablesArray) {
        this.index = language.getGlobalVariableIndex(name);
        this.globalVariablesArray = globalVariablesArray;

        this.globalVariableAliasedAssumption = language.getGlobalVariableNeverAliasedAssumption(index);
        this.unaliasedStorage = globalVariablesArray.get(index);
    }

    @TruffleBoundary
    public Object getValue() {
        if (globalVariableAliasedAssumption.isValid()) {
            return unaliasedStorage.getValue();
        } else {
            return globalVariablesArray.get(index).getValue();
        }
    }

}
