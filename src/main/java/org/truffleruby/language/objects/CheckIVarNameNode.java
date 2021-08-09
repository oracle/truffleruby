/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.parser.Identifiers;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

@ImportStatic(Identifiers.class)
public abstract class CheckIVarNameNode extends RubyBaseNode {

    public static CheckIVarNameNode create() {
        return CheckIVarNameNodeGen.create();
    }

    public abstract void execute(RubyDynamicObject object, String name);

    @Specialization(
            guards = { "name == cachedName", "isValidInstanceVariableName(cachedName)" },
            limit = "getCacheLimit()")
    protected void cached(RubyDynamicObject object, String name,
            @Cached("name") String cachedName) {
    }

    @Specialization(replaces = "cached")
    protected void uncached(RubyDynamicObject object, String name) {
        SymbolTable.checkInstanceVariableName(getContext(), name, object, this);
    }

    protected int getCacheLimit() {
        return getLanguage().options.INSTANCE_VARIABLE_CACHE;
    }

}
