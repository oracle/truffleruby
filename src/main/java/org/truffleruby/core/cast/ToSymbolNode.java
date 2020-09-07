/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyBaseNode;

@GenerateUncached
public abstract class ToSymbolNode extends RubyBaseNode {

    public static ToSymbolNode create() {
        return ToSymbolNodeGen.create();
    }

    public static ToSymbolNode getUncached() {
        return ToSymbolNodeGen.getUncached();
    }

    public abstract RubySymbol execute(Object object);

    @Specialization
    protected RubySymbol toSymbolSymbol(RubySymbol symbol) {
        return symbol;
    }

    @Specialization(guards = "str == cachedStr", limit = "getCacheLimit()")
    protected RubySymbol toSymbolJavaString(String str,
            @Cached(value = "str") String cachedStr,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached(value = "context.getSymbol(cachedStr)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(replaces = "toSymbolJavaString")
    protected RubySymbol toSymbolJavaStringUncached(String str,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getSymbol(str);
    }

    @Specialization(guards = "equals.execute(str.rope, cachedRope)", limit = "getCacheLimit()")
    protected RubySymbol toSymbolRubyString(RubyString str,
            @Cached(value = "str.rope") Rope cachedRope,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached RopeNodes.EqualNode equals,
            @Cached(value = "context.getSymbol(cachedRope)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(replaces = "toSymbolRubyString")
    protected RubySymbol toSymbolRubyStringUncached(RubyString str,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getSymbol(str.rope);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().DISPATCH_CACHE;
    }
}
