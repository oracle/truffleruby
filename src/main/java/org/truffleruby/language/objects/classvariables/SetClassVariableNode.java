/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.classvariables;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

public abstract class SetClassVariableNode extends RubyContextNode {

    public static SetClassVariableNode create() {
        return SetClassVariableNodeGen.create();
    }

    public abstract Object execute(RubyModule module, String name, Object value);

    @Specialization(guards = "!objectLibrary.isShared(classVariableStorage)")
    protected Object setClassVariableLocal(RubyModule module, String name, Object value,
            @Bind("module.fields.getClassVariables()") ClassVariableStorage classVariableStorage,
            @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary,
            @Cached @Shared("slowPath") BranchProfile slowPath) {
        if (!objectLibrary.putIfPresent(classVariableStorage, name, value)) {
            slowPath.enter();
            ModuleOperations.setClassVariable(getLanguage(), getContext(), module, name, value, this);
        }
        return value;
    }

    @Specialization(guards = "objectLibrary.isShared(classVariableStorage)")
    protected Object setClassVariableShared(RubyModule module, String name, Object value,
            @Bind("module.fields.getClassVariables()") ClassVariableStorage classVariableStorage,
            @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary,
            @Cached WriteBarrierNode writeBarrierNode,
            @Cached @Shared("slowPath") BranchProfile slowPath) {
        // See WriteObjectFieldNode
        writeBarrierNode.executeWriteBarrier(value);

        final boolean set = classVariableStorage.putIfPresent(name, value, objectLibrary);
        if (!set) {
            slowPath.enter();
            ModuleOperations.setClassVariable(getLanguage(), getContext(), module, name, value, this);
        }
        return value;
    }

}
