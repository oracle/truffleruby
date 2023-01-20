/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.dsl.NeverDefault;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class WriteSimpleGlobalVariableNode extends RubyBaseNode {

    protected final String name;
    @Child protected ReferenceEqualNode referenceEqualNode = ReferenceEqualNode.create();
    @Child protected WriteBarrierNode writeBarrierNode = WriteBarrierNode.create();

    @NeverDefault
    public static WriteSimpleGlobalVariableNode create(String name) {
        return WriteSimpleGlobalVariableNodeGen.create(name);
    }

    public WriteSimpleGlobalVariableNode(String name) {
        this.name = name;
    }

    public abstract Object execute(Object value);

    @Specialization(
            guards = {
                    "getLanguage().singleContext",
                    "referenceEqualNode.executeReferenceEqual(value, previousValue)" },
            assumptions = {
                    "storage.getUnchangedAssumption()",
                    "getLanguage().getGlobalVariableNeverAliasedAssumption(index)" })
    protected Object writeTryToKeepConstant(Object value,
            @Cached("getLanguage().getGlobalVariableIndex(name)") int index,
            @Cached("getContext().getGlobalVariableStorage(index)") GlobalVariableStorage storage,
            @Cached("storage.getValue()") Object previousValue) {
        // NOTE: we still do the volatile write to get the proper memory barrier,
        // as the global variable could be used as a publication mechanism.
        storage.setValueInternal(value);
        return previousValue;
    }

    @Specialization(
            guards = { "getLanguage().singleContext", "storage.isAssumeConstant()" },
            assumptions = {
                    "storage.getUnchangedAssumption()",
                    "getLanguage().getGlobalVariableNeverAliasedAssumption(index)" })
    protected Object writeAssumeConstant(Object value,
            @Cached(value = "getLanguage().getGlobalVariableIndex(name)", neverDefault = false) int index,
            @Cached("getContext().getGlobalVariableStorage(index)") GlobalVariableStorage storage) {
        if (getContext().getSharedObjects().isSharing()) {
            writeBarrierNode.executeWriteBarrier(value);
        }
        storage.setValueInternal(value);
        storage.updateAssumeConstant(getContext());
        return value;
    }

    @Specialization(replaces = "writeAssumeConstant")
    protected Object writeAliasedOrMultiContext(Object value,
            @Cached("create(name)") LookupGlobalVariableStorageNode lookupGlobalVariableStorageNode) {
        if (getContext().getSharedObjects().isSharing()) {
            writeBarrierNode.executeWriteBarrier(value);
        }

        final GlobalVariableStorage storage = lookupGlobalVariableStorageNode.execute(null);

        storage.setValueInternal(value);
        if (storage.isAssumeConstant()) {
            storage.updateAssumeConstant(getContext());
        }
        return value;
    }

}
