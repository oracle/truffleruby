/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class LookupGlobalVariableStorageNode extends RubyContextNode {

    protected final String name;

    public static LookupGlobalVariableStorageNode create(String name) {
        return LookupGlobalVariableStorageNodeGen.create(name);
    }

    public LookupGlobalVariableStorageNode(String name) {
        this.name = name;
    }

    public final GlobalVariableStorage execute(Object dynamicArgument) {
        return executeInternal();
    }

    protected abstract GlobalVariableStorage executeInternal();

    @Specialization(
            guards = "getLanguage().singleContext",
            assumptions = "getLanguage().getGlobalVariableNeverAliasedAssumption(index)")
    protected GlobalVariableStorage singleContext(
            @Cached("getLanguage().getGlobalVariableIndex(name)") int index,
            @Cached("getContext().getGlobalVariableStorage(index)") GlobalVariableStorage storage) {
        return storage;
    }

    @Specialization(
            assumptions = "getLanguage().getGlobalVariableNeverAliasedAssumption(index)")
    protected GlobalVariableStorage multiContext(
            @Cached("getLanguage().getGlobalVariableIndex(name)") int index) {
        return getContext().getGlobalVariableStorage(index);
    }

    @Specialization(guards = "!getLanguage().getGlobalVariableNeverAliasedAssumption(index).isValid()")
    protected GlobalVariableStorage aliased(
            @Cached("getLanguage().getGlobalVariableIndex(name)") int index) {
        return getContext().globalVariablesArray.get(index);
    }

}
