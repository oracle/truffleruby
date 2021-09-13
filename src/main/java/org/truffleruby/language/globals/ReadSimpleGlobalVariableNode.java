/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ReadSimpleGlobalVariableNode extends RubyBaseNode {

    public final String name;
    @Child LookupGlobalVariableStorageNode lookupGlobalVariableStorageNode;

    public static ReadSimpleGlobalVariableNode create(String name) {
        return ReadSimpleGlobalVariableNodeGen.create(name);
    }

    public ReadSimpleGlobalVariableNode(String name) {
        this.name = name;
    }

    public abstract Object execute();

    @Specialization(
            guards = "getLanguage().singleContext",
            assumptions = {
                    "storage.getUnchangedAssumption()",
                    "getLanguage().getGlobalVariableNeverAliasedAssumption(index)" })
    protected Object readConstant(
            @Cached("getLanguage().getGlobalVariableIndex(name)") int index,
            @Cached("getContext().getGlobalVariableStorage(index)") GlobalVariableStorage storage,
            @Cached("storage.getValue()") Object value) {
        return value;
    }

    @Specialization
    protected Object read() {
        if (lookupGlobalVariableStorageNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupGlobalVariableStorageNode = insert(LookupGlobalVariableStorageNode.create(name));
        }
        final GlobalVariableStorage storage = lookupGlobalVariableStorageNode.execute(null);

        return storage.getValue();
    }

}
