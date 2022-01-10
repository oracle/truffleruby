/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;

@GenerateUncached
@ImportStatic({ StringCachingGuards.class, StringOperations.class })
@NodeChild(value = "value", type = RubyNode.class)
public abstract class ToJavaStringNode extends RubySourceNode {

    public static ToJavaStringNode create() {
        return ToJavaStringNodeGen.create(null);
    }

    public static ToJavaStringNode create(RubyNode string) {
        return ToJavaStringNodeGen.create(string);
    }

    public abstract String executeToJavaString(Object name);

    @Specialization(
            guards = { "strings.isRubyString(value)", "equalsNode.execute(strings.getRope(value), cachedRope)" },
            limit = "getLimit()")
    protected String stringCached(Object value,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
            @Cached("strings.getRope(value)") Rope cachedRope,
            @Cached("strings.getJavaString(value)") String convertedString,
            @Cached RopeNodes.EqualNode equalsNode) {
        return convertedString;
    }

    @Specialization(guards = "strings.isRubyString(value)", replaces = "stringCached")
    protected String stringUncached(Object value,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
            @Cached ConditionProfile asciiOnlyProfile,
            @Cached RopeNodes.AsciiOnlyNode asciiOnlyNode,
            @Cached RopeNodes.BytesNode bytesNode) {
        final Rope rope = strings.getRope(value);
        final byte[] bytes = bytesNode.execute(rope);

        if (asciiOnlyProfile.profile(asciiOnlyNode.execute(rope))) {
            return RopeOperations.decodeAscii(bytes);
        } else {
            return RopeOperations.decodeNonAscii(rope.getEncoding(), bytes, 0, bytes.length);
        }
    }

    @Specialization(
            guards = "symbol == cachedSymbol",
            limit = "getIdentityCacheLimit()")
    protected String symbolCached(RubySymbol symbol,
            @Cached("symbol") RubySymbol cachedSymbol,
            @Cached("cachedSymbol.getString()") String convertedString) {
        return convertedString;
    }

    @Specialization(replaces = "symbolCached")
    protected String symbolUncached(RubySymbol symbol) {
        return symbol.getString();
    }

    @Specialization(guards = "string == cachedString", limit = "getIdentityCacheLimit()")
    protected String javaStringCached(String string,
            @Cached("string") String cachedString) {
        return cachedString;
    }

    @Specialization(replaces = "javaStringCached")
    protected String javaStringUncached(String value) {
        return value;
    }

    @Fallback
    protected String fallback(Object value) {
        throw new RaiseException(
                getContext(),
                coreExceptions()
                        .typeError("This interop message requires a String or Symbol for the member name", this));
    }

    protected int getLimit() {
        return getLanguage().options.INTEROP_CONVERT_CACHE;
    }

}
