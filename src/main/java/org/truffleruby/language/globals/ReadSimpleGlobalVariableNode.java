/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ReadSimpleGlobalVariableNode extends RubyBaseNode {

    protected final String name;

    public static ReadSimpleGlobalVariableNode create(String name) {
        return ReadSimpleGlobalVariableNodeGen.create(name);
    }

    public ReadSimpleGlobalVariableNode(String name) {
        this.name = name;
    }

    public abstract Object execute();

    @Specialization(assumptions = { "storage.getUnchangedAssumption()", "storage.getValidAssumption()" })
    protected Object readConstant(
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("storage.getValue()") Object value) {
        return value;
    }

    @Specialization(assumptions = "storage.getValidAssumption()")
    protected Object read(
            @Cached("getStorage()") GlobalVariableStorage storage) {
        return storage.getValue();
    }

    protected GlobalVariableStorage getStorage() {
        return coreLibrary().globalVariables.getStorage(name);
    }

}
