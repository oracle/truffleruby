/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class FromJavaStringNode extends RubyBaseNode {

    public static FromJavaStringNode create() {
        return FromJavaStringNodeGen.create();
    }

    public abstract RubyString executeFromJavaString(Object value);

    @Specialization(guards = "stringsEquals(cachedValue, value)", limit = "getLimit()")
    protected RubyString doCached(String value,
            @Cached("value") String cachedValue,
            @Cached("getRope(value)") Rope cachedRope,
            @Cached StringNodes.MakeStringNode makeStringNode) {
        return makeStringNode.fromRope(cachedRope, Encodings.UTF_8);
    }

    @Specialization(replaces = "doCached")
    protected RubyString doGeneric(String value,
            @Cached StringNodes.MakeStringNode makeStringNode) {
        return makeStringNode.executeMake(value, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
    }

    protected boolean stringsEquals(String a, String b) {
        return a.equals(b);
    }

    protected Rope getRope(String value) {
        return StringOperations.encodeRope(value, UTF8Encoding.INSTANCE);
    }

    protected int getLimit() {
        return getLanguage().options.INTEROP_CONVERT_CACHE;
    }

}
