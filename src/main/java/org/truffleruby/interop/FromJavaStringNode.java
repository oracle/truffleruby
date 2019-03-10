/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;

public abstract class FromJavaStringNode extends RubyBaseNode {

    public static FromJavaStringNode create() {
        return FromJavaStringNodeGen.create();
    }

    public abstract DynamicObject executeFromJavaString(Object value);

    @Specialization(guards = "stringsEquals(cachedValue, value)", limit = "getLimit()")
    public DynamicObject doCached(String value,
            @Cached("value") String cachedValue,
            @Cached("getRope(value)") Rope cachedRope,
            @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
        return makeStringNode.fromRope(cachedRope);
    }

    @Specialization(replaces = "doCached")
    public DynamicObject doGeneric(String value,
                                        @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
        return makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
    }

    protected boolean stringsEquals(String a, String b) {
        return a.equals(b);
    }

    protected Rope getRope(String value) {
        return StringOperations.encodeRope(value, UTF8Encoding.INSTANCE);
    }

    protected int getLimit() {
        return getContext().getOptions().INTEROP_CONVERT_CACHE;
    }

}
