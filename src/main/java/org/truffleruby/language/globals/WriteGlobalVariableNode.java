/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild(value = "valueNode", type = RubyNode.class)
public abstract class WriteGlobalVariableNode extends RubyContextSourceNode implements AssignableNode {

    protected final String name;
    @Child LookupGlobalVariableStorageNode lookupGlobalVariableStorageNode;

    public WriteGlobalVariableNode(String name) {
        this.name = name;
        lookupGlobalVariableStorageNode = LookupGlobalVariableStorageNode.create(name);
    }

    public abstract Object execute(VirtualFrame frame, Object value);

    @Override
    public void assign(VirtualFrame frame, Object value) {
        execute(frame, value);
    }

    @Specialization(guards = "storage.isSimple()")
    protected Object write(VirtualFrame frame, Object value,
            @Bind("getStorage(frame)") GlobalVariableStorage storage,
            @Cached("create(name)") WriteSimpleGlobalVariableNode simpleNode) {
        simpleNode.execute(value);
        return value;
    }

    @Specialization(guards = { "storage.hasHooks()", "arity != 2" })
    protected Object writeHooks(VirtualFrame frame, Object value,
            @Bind("getStorage(frame)") GlobalVariableStorage storage,
            @Bind("setterArity(storage)") int arity,
            @Cached @Exclusive CallBlockNode yieldNode) {
        yieldNode.yield(storage.getSetter(), value);
        return value;
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 2" })
    protected Object writeHooksWithStorage(VirtualFrame frame, Object value,
            @Bind("getStorage(frame)") GlobalVariableStorage storage,
            @Bind("setterArity(storage)") int arity,
            @Cached @Exclusive CallBlockNode yieldNode,
            @Cached GetSpecialVariableStorage storageNode) {
        yieldNode.yield(
                storage.getSetter(),
                value,
                storageNode.execute(frame));
        return value;
    }

    protected int setterArity(GlobalVariableStorage storage) {
        return storage.getSetter().getArityNumber();
    }

    protected GlobalVariableStorage getStorage(VirtualFrame dynamicArgument) {
        return lookupGlobalVariableStorageNode.execute(dynamicArgument);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return coreStrings().ASSIGNMENT.createInstance(context);
    }

    @Override
    public AssignableNode toAssignableNode() {
        // Cannot reassign a @NodeChild
        final WriteGlobalVariableNode node = WriteGlobalVariableNodeGen.create(name, null);
        node.copySourceSection(this);
        return node;
    }
}
