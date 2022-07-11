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

import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class FromJavaStringNode extends RubyBaseNode {

    public static FromJavaStringNode create() {
        return FromJavaStringNodeGen.create();
    }

    public abstract RubyString executeFromJavaString(String value);

    @Specialization(guards = "stringsEquals(cachedValue, value)", limit = "getLimit()")
    protected RubyString doCached(String value,
            @Cached("value") String cachedValue,
            @Cached TruffleString.FromJavaStringNode tstringFromJavaStringNode,
            @Cached("getTString(cachedValue, tstringFromJavaStringNode)") TruffleString cachedRope) {
        var rubyString = createString(cachedRope, Encodings.UTF_8);
        rubyString.freeze();
        return rubyString;
    }

    @Specialization(replaces = "doCached")
    protected RubyString doGeneric(String value,
            @Cached StringNodes.MakeStringNode makeStringNode) {
        var rubyString = makeStringNode.executeMake(value, Encodings.UTF_8);
        rubyString.freeze();
        return rubyString;
    }

    protected boolean stringsEquals(String a, String b) {
        return a.equals(b);
    }

    protected TruffleString getTString(String value, TruffleString.FromJavaStringNode tstringFromJavaStringNode) {
        return tstringFromJavaStringNode.execute(value, TruffleString.Encoding.UTF_8);
    }

    protected int getLimit() {
        return getLanguage().options.INTEROP_CONVERT_CACHE;
    }

}
