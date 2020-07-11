/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

@GenerateUncached
public abstract class ToRubySymbolNode extends Node {

    public abstract Object execute(String str);

    public static ToRubySymbolNode create() {
        return ToRubySymbolNodeGen.create();
    }

    public static ToRubySymbolNode getUncached() {
        return ToRubySymbolNodeGen.getUncached();
    }

    @Specialization(guards = "str == cachedStr")
    protected Object symbolFromString(String str,
            @Cached(value = "str", allowUncached = true) String cachedStr,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached(value = "context.getSymbol(cachedStr)", allowUncached = true) Object rubySymbol) {
        return rubySymbol;
    }
}
