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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

@NodeChild(value = "module", type = RubyNode.class)
@NodeChild(value = "name", type = RubyNode.class)
public abstract class LookupClassVariableNode extends RubyContextSourceNode {

    public static LookupClassVariableNode create() {
        return LookupClassVariableNodeGen.create(null, null);
    }

    public abstract Object execute(RubyModule module, String name);

    @Specialization
    protected Object lookupClassVariable(RubyModule module, String name,
            @Cached LookupClassVariableStorageNode lookupClassVariableStorageNode,
            @Cached ConditionProfile noStorageProfile,
            @CachedLibrary(limit = "getRubyLibraryCacheLimit()") DynamicObjectLibrary readStorageNode) {
        final ClassVariableStorage objectForClassVariables = lookupClassVariableStorageNode.execute(module, name);

        if (noStorageProfile.profile(objectForClassVariables == null)) {
            return null;
        } else {
            return readStorageNode.getOrDefault(objectForClassVariables, name, null);
        }
    }

}
