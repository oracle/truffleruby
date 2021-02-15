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
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "module", type = RubyNode.class)
@NodeChild(value = "name", type = RubyNode.class)
public abstract class LookupClassVariableStorageNode extends RubyContextSourceNode {

    public static LookupClassVariableStorageNode create() {
        return LookupClassVariableStorageNodeGen.create(null, null);
    }

    public abstract ClassVariableStorage execute(RubyModule module, String name);

    @Specialization(
            guards = {
                    "name == cachedName",
                    "module.getClassVariables().getShape() == cachedClassVariableStorageShape",
                    "moduleHasVariable" })
    protected ClassVariableStorage lookupClassVariable(RubyModule module, String name,
            @Cached("name") String cachedName,
            @Cached("module.getClassVariables()") ClassVariableStorage cachedClassVariableStorage,
            @Cached("cachedClassVariableStorage.getShape()") Shape cachedClassVariableStorageShape,
            @Cached("cachedClassVariableStorage.getShape().hasProperty(cachedName)") boolean moduleHasVariable) {
        return cachedClassVariableStorage;
    }

    @Specialization(replaces = "lookupClassVariable")
    @TruffleBoundary
    protected ClassVariableStorage uncachedLookupClassVariable(RubyModule module, String name) {
        return ModuleOperations.classVariableLookup(module, m -> {
            final ClassVariableStorage classVariableStorage = m.fields.getClassVariables();
            if (classVariableStorage.getShape().hasProperty(name)) {
                return classVariableStorage;
            } else {
                return null;
            }
        });
    }

}
