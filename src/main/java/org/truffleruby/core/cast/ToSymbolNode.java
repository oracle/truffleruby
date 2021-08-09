/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.library.RubyStringLibrary;

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
            @Cached(value = "getSymbol(cachedStr)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(replaces = "toSymbolJavaString")
    protected RubySymbol toSymbolJavaStringUncached(String str) {
        return getSymbol(str);
    }

    @Specialization(
            guards = {
                    "strings.isRubyString(str)",
                    "equals.execute(strings.getRope(str), cachedRope)",
                    "strings.getEncoding(str) == cachedEncoding" },
            limit = "getCacheLimit()")
    protected RubySymbol toSymbolRubyString(Object str,
            @CachedLibrary(limit = "2") RubyStringLibrary strings,
            @Cached(value = "strings.getRope(str)") Rope cachedRope,
            @Cached(value = "strings.getEncoding(str)") RubyEncoding cachedEncoding,
            @Cached RopeNodes.EqualNode equals,
            @Cached(value = "getSymbol(cachedRope, cachedEncoding)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(guards = "strings.isRubyString(str)", replaces = "toSymbolRubyString")
    protected RubySymbol toSymbolRubyStringUncached(Object str,
            @CachedLibrary(limit = "2") RubyStringLibrary strings) {
        return getSymbol(strings.getRope(str), strings.getEncoding(str));
    }

    protected int getCacheLimit() {
        return getLanguage().options.DISPATCH_CACHE;
    }
}
