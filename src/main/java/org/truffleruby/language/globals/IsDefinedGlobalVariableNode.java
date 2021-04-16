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

import com.oracle.truffle.api.dsl.Bind;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class IsDefinedGlobalVariableNode extends RubyContextNode {

    public static IsDefinedGlobalVariableNode create(String name) {
        return IsDefinedGlobalVariableNodeGen.create(name);
    }

    @Child LookupGlobalVariableStorageNode lookupGlobalVariableStorageNode;

    public IsDefinedGlobalVariableNode(String name) {
        lookupGlobalVariableStorageNode = LookupGlobalVariableStorageNode.create(name);
    }

    public abstract Object executeIsDefined(VirtualFrame frame);

    @Specialization(guards = "storage.isSimple()")
    protected Object executeDefined(VirtualFrame frame,
            @Bind("getStorage(frame)") GlobalVariableStorage storage) {
        if (storage.isDefined()) {
            return coreStrings().GLOBAL_VARIABLE.createInstance(getContext());
        } else {
            return nil;
        }
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 0" })
    protected Object executeDefinedHooks(VirtualFrame frame,
            @Cached("getLanguage().getGlobalVariableIndex(lookupGlobalVariableStorageNode.name)") int index,
            @Bind("getStorage(frame)") GlobalVariableStorage storage,
            @Cached("isDefinedArity(storage)") int arity,
            @Cached CallBlockNode yieldNode) {
        return yieldNode.yield(storage.getIsDefined());
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 1" })
    protected Object executeDefinedHooksWithBinding(VirtualFrame frame,
            @Bind("getStorage(frame)") GlobalVariableStorage storage,
            @Cached("isDefinedArity(storage)") int arity,
            @Cached CallBlockNode yieldNode,
            @Cached GetSpecialVariableStorage readStorage) {
        return yieldNode.yield(storage.getIsDefined(), readStorage.execute(frame));
    }

    protected int isDefinedArity(GlobalVariableStorage storage) {
        return storage.getIsDefined().getArityNumber();
    }

    protected GlobalVariableStorage getStorage(VirtualFrame dynamicArgument) {
        return lookupGlobalVariableStorageNode.execute(dynamicArgument);
    }

}
