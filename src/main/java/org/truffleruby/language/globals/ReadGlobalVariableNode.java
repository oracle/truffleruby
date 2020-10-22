/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import org.truffleruby.RubyContext;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ReadGlobalVariableNode extends RubyContextSourceNode {

    protected final String name;
    @Child private IsDefinedGlobalVariableNode definedNode;

    public ReadGlobalVariableNode(String name) {
        this.name = name;
    }

    @Specialization(guards = "storage.isSimple()", assumptions = "storage.getValidAssumption()")
    protected Object read(
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("create(name)") ReadSimpleGlobalVariableNode simpleNode) {
        return simpleNode.execute();
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 0" }, assumptions = "storage.getValidAssumption()")
    protected Object readHooks(VirtualFrame frame,
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("getterArity(storage)") int arity,
            @Cached YieldNode yieldNode) {
        return yieldNode.executeDispatch(storage.getGetter());
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 1" }, assumptions = "storage.getValidAssumption()")
    protected Object readHooksWithStorage(VirtualFrame frame,
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("getterArity(storage)") int arity,
            @Cached YieldNode yieldNode,
            @Cached GetSpecialVariableStorage storageNode) {
        return yieldNode
                .executeDispatch(storage.getGetter(), storageNode.execute(frame));
    }

    protected int getterArity(GlobalVariableStorage storage) {
        return storage.getGetter().getArityNumber();
    }

    protected GlobalVariableStorage getStorage() {
        return getContext().getCoreLibrary().globalVariables.getStorage(name);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        if (definedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            definedNode = insert(IsDefinedGlobalVariableNode.create(name));
        }

        return definedNode.executeIsDefined(frame);
    }

}
