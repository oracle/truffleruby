/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.utils.Utils;

@GenerateUncached
@NodeChild(value = "value", type = RubyBaseNodeWithExecute.class)
public abstract class ToSymbolNode extends RubyBaseNodeWithExecute {

    public static ToSymbolNode create() {
        return ToSymbolNodeGen.create(null);
    }

    public static ToSymbolNode create(RubyBaseNodeWithExecute value) {
        return ToSymbolNodeGen.create(value);
    }

    public static ToSymbolNode getUncached() {
        return ToSymbolNodeGen.getUncached();
    }

    public abstract RubySymbol execute(Object object);

    @Specialization
    protected RubySymbol symbol(RubySymbol symbol) {
        return symbol;
    }

    @Specialization(guards = "str == cachedStr", limit = "getCacheLimit()")
    protected RubySymbol javaString(String str,
            @Cached(value = "str") String cachedStr,
            @Cached(value = "getSymbol(cachedStr)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(replaces = "javaString")
    protected RubySymbol javaStringUncached(String str) {
        return getSymbol(str);
    }

    @Specialization(
            guards = {
                    "strings.isRubyString(str)",
                    "equals.execute(strings.getRope(str), cachedRope)",
                    "strings.getEncoding(str) == cachedEncoding" },
            limit = "getCacheLimit()")
    protected RubySymbol rubyString(Object str,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
            @Cached(value = "strings.getRope(str)") Rope cachedRope,
            @Cached(value = "strings.getTString(str)") AbstractTruffleString cachedTString,
            @Cached(value = "strings.getEncoding(str)") RubyEncoding cachedEncoding,
            @Cached RopeNodes.EqualNode equals,
            @Cached(value = "getSymbol(cachedTString, cachedEncoding)") RubySymbol rubySymbol) {
        return rubySymbol;
    }

    @Specialization(guards = "strings.isRubyString(str)", replaces = "rubyString")
    protected RubySymbol rubyStringUncached(Object str,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
        return getSymbol(strings.getTString(str), strings.getEncoding(str));
    }

    @Specialization(guards = { "!isRubySymbol(object)", "!isString(object)", "isNotRubyString(object)" })
    protected RubySymbol toStr(Object object,
            @Cached BranchProfile errorProfile,
            @Cached DispatchNode toStr,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
            @Cached ToSymbolNode toSymbolNode) {
        final Object coerced;
        try {
            coerced = toStr.call(object, "to_str");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(getContext(), coreExceptions().typeError(
                        Utils.concat(object, " is not a symbol nor a string"),
                        this));
            } else {
                throw e;
            }
        }

        if (libString.isRubyString(coerced)) {
            return toSymbolNode.execute(coerced);
        } else {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorBadCoercion(
                    object,
                    "String",
                    "to_str",
                    coerced,
                    this));
        }
    }

    protected int getCacheLimit() {
        return getLanguage().options.DISPATCH_CACHE;
    }
}
