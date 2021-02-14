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

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.module.ModuleOperations;
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
    public Object lookupClassVariable(RubyModule module, String name) {
        return ModuleOperations.lookupClassVariable(module, name);
    }

}
