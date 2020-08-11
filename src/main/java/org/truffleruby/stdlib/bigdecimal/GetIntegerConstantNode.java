/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class GetIntegerConstantNode extends RubyContextNode {

    public static GetIntegerConstantNode create() {
        return GetIntegerConstantNodeGen.create();
    }

    public abstract int executeGetIntegerConstant(RubyModule module, String name);

    @Specialization
    protected int doInteger(RubyModule module, String name,
            @Cached("createLookupConstantNode()") LookupConstantNode lookupConstantNode,
            @Cached GetConstantNode getConstantNode,
            @Cached ToIntNode toIntNode) {
        final Object value = getConstantNode
                .lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookupConstantNode);
        return toIntNode.execute(value);
    }

    protected LookupConstantNode createLookupConstantNode() {
        return LookupConstantNode.create(false, true, true);
    }

}
