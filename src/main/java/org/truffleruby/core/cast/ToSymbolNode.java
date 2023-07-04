/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

@GenerateUncached
@GenerateCached
@GenerateInline(inlineByDefault = true)
public abstract class ToSymbolNode extends RubyBaseNode {

    public final RubySymbol executeCached(Object object) {
        return execute(this, object);
    }

    public static RubySymbol executeUncached(Object object) {
        return ToSymbolNodeGen.getUncached().execute(null, object);
    }

    public abstract RubySymbol execute(Node node, Object object);

    @Specialization
    protected static RubySymbol symbol(RubySymbol symbol) {
        return symbol;
    }

    @Specialization(guards = "str == cachedStr", limit = "getCacheLimit()")
    protected static RubySymbol javaString(String str,
            @Cached(value = "str") String cachedStr,
            @Cached(value = "getSymbol(cachedStr)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(replaces = "javaString")
    protected static RubySymbol javaStringUncached(Node node, String str) {
        return getSymbol(node, str);
    }

    @Specialization(
            guards = {
                    "strings.isRubyString(str)",
                    "equalNode.execute(node, strings, str, cachedTString, cachedEncoding)" },
            limit = "getCacheLimit()")
    protected static RubySymbol rubyString(Node node, Object str,
            @Cached @Shared RubyStringLibrary strings,
            @Cached(value = "asTruffleStringUncached(str)") TruffleString cachedTString,
            @Cached(value = "strings.getEncoding(str)") RubyEncoding cachedEncoding,
            @Cached StringHelperNodes.EqualSameEncodingNode equalNode,
            @Cached(value = "getSymbol(node, cachedTString, cachedEncoding)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(guards = "strings.isRubyString(str)", replaces = "rubyString", limit = "1")
    protected static RubySymbol rubyStringUncached(Node node, Object str,
            @Cached @Shared RubyStringLibrary strings) {
        return getSymbol(node, strings.getTString(str), strings.getEncoding(str));
    }

    @Specialization(guards = { "!isRubySymbol(object)", "!isString(object)", "isNotRubyString(object)" })
    protected static RubySymbol toStr(Node node, Object object,
            @Cached InlinedBranchProfile errorProfile,
            @Cached DispatchNode toStrNode,
            @Cached @Exclusive RubyStringLibrary strings,
            @Cached(inline = false) ToSymbolNode toSymbolNode) {
        var coerced = toStrNode.call(
                coreLibrary(node).truffleTypeModule,
                "rb_convert_type",
                object,
                coreLibrary(node).stringClass,
                coreSymbols(node).TO_STR);

        if (strings.isRubyString(coerced)) {
            return toSymbolNode.executeCached(coerced);
        } else {
            errorProfile.enter(node);
            throw new RaiseException(getContext(node), coreExceptions(node).typeErrorBadCoercion(
                    object,
                    "String",
                    "to_str",
                    coerced,
                    node));
        }
    }

    protected int getCacheLimit() {
        return getLanguage().options.DISPATCH_CACHE;
    }
}
