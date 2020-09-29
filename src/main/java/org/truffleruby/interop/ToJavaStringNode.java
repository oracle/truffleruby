/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
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
            guards = { "equalsNode.execute(value.rope, cachedRope)" },
            limit = "getLimit()")
    protected String stringCached(RubyString value,
            @Cached("privatizeRope(value)") Rope cachedRope,
            @Cached("value.getJavaString()") String convertedString,
            @Cached RopeNodes.EqualNode equalsNode) {
        return convertedString;
    }

    @Specialization(replaces = "stringCached")
    protected String stringUncached(RubyString value,
            @Cached ConditionProfile asciiOnlyProfile,
            @Cached RopeNodes.AsciiOnlyNode asciiOnlyNode,
            @Cached RopeNodes.BytesNode bytesNode) {
        final Rope rope = value.rope;
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
            @Cached("symbolToString(symbol)") String convertedString) {
        return convertedString;
    }

    @Specialization(replaces = "symbolCached")
    protected String symbolUncached(RubySymbol symbol) {
        return symbolToString(symbol);
    }

    protected String symbolToString(RubySymbol symbol) {
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

    protected int getLimit() {
        return RubyLanguage.getCurrentContext().getOptions().INTEROP_CONVERT_CACHE;
    }

    protected int getIdentityCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().IDENTITY_CACHE;
    }

}
