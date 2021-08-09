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
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyBaseNode;

public abstract class LookupClassVariableNode extends RubyBaseNode {

    public static LookupClassVariableNode create() {
        return LookupClassVariableNodeGen.create();
    }

    public abstract Object execute(RubyModule module, String name);

    @Specialization
    protected Object lookupClassVariable(RubyModule module, String name,
            @Cached LookupClassVariableStorageNode lookupClassVariableStorageNode,
            @Cached ConditionProfile noStorageProfile,
            @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary) {
        final ClassVariableStorage classVariables = lookupClassVariableStorageNode.execute(module, name);

        if (noStorageProfile.profile(classVariables == null)) {
            return null;
        } else {
            return classVariables.read(name, objectLibrary);
        }
    }

}
