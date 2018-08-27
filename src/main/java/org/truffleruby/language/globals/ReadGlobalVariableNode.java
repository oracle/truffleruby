/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.Layouts;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.yield.YieldNode;

public abstract class ReadGlobalVariableNode extends RubyNode {

    private final String name;
    @Child private IsDefinedGlobalVariableNode definedNode;

    public ReadGlobalVariableNode(String name) {
        this.name = name;
    }

    @Specialization(guards = "storage.isSimple()")
    public Object read(@Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("create(storage)") ReadSimpleGlobalVariableNode simpleNode) {
        return simpleNode.execute();
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 0" })
    public Object readHooks(VirtualFrame frame,
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("getterArity(storage)") int arity,
            @Cached("new()") YieldNode yieldNode) {
        return yieldNode.dispatch(storage.getGetter());
    }

    @Specialization(guards = { "storage.hasHooks()", "arity == 1" })
    public Object readHooksWithBinding(VirtualFrame frame,
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("getterArity(storage)") int arity,
            @Cached("new()") YieldNode yieldNode) {
        return yieldNode.dispatch(storage.getGetter(), BindingNodes.createBinding(getContext(), frame.materialize()));
    }

    protected int getterArity(GlobalVariableStorage storage) {
        return Layouts.PROC.getSharedMethodInfo(storage.getGetter()).getArity().getArityNumber();
    }

    protected GlobalVariableStorage getStorage() {
        return getContext().getCoreLibrary().getGlobalVariables().getStorage(name);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        if (definedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            definedNode = insert(IsDefinedGlobalVariableNode.create(name));
        }

        return definedNode.executeIsDefined(frame);
    }

}
