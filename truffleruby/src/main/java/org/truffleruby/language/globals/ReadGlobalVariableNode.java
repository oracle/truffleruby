/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.yield.YieldNode;

public abstract class ReadGlobalVariableNode extends RubyNode {

    private final String name;

    public ReadGlobalVariableNode(String name) {
        this.name = name;
    }

    @Specialization(guards = "storage.isSimple()", assumptions = "storage.getUnchangedAssumption()")
    public Object readConstant(
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("storage.getValue()") Object value) {
        return value;
    }

    @Specialization(guards = "storage.isSimple()")
    public Object read(@Cached("getStorage()") GlobalVariableStorage storage) {
        return storage.getValue();
    }

    @Specialization(guards = "storage.hasHooks()")
    public Object readHooks(VirtualFrame frame,
                            @Cached("getStorage()") GlobalVariableStorage storage,
                            @Cached("new()") YieldNode yieldNode) {
        return yieldNode.dispatch(frame, storage.getGetter());
    }

    protected GlobalVariableStorage getStorage() {
        return getContext().getCoreLibrary().getGlobalVariables().getStorage(name);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        final GlobalVariableStorage storage = getStorage();

        if (storage.hasHooks()) {
            CompilerDirectives.transferToInterpreter();
            throw new UnsupportedOperationException();
        }

        if (storage.getValue() != nil()) {
            return coreStrings().GLOBAL_VARIABLE.createInstance();
        } else {
            return nil();
        }
    }

}
