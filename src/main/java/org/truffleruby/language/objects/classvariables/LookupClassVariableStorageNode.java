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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyBaseNode;

public abstract class LookupClassVariableStorageNode extends RubyBaseNode {

    public static LookupClassVariableStorageNode create() {
        return LookupClassVariableStorageNodeGen.create();
    }

    public abstract ClassVariableStorage execute(RubyModule module, String name);

    @Specialization(guards = "objectLibrary.containsKey(classVariableStorage, name)")
    protected ClassVariableStorage lookupClassVariable(RubyModule module, String name,
            @Bind("module.fields.getClassVariables()") ClassVariableStorage classVariableStorage,
            @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary) {
        return classVariableStorage;
    }

    @Specialization(replaces = "lookupClassVariable")
    @TruffleBoundary
    protected ClassVariableStorage uncachedLookupClassVariable(RubyModule module, String name) {
        return ModuleOperations.classVariableLookup(module, m -> {
            final ClassVariableStorage classVariables = m.fields.getClassVariables();
            if (classVariables.getShape().hasProperty(name)) {
                return classVariables;
            } else {
                return null;
            }
        });
    }

}
