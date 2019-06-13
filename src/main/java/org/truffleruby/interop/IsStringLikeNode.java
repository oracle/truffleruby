/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.language.RubyBaseWithoutContextNode;

@GenerateUncached
public abstract class IsStringLikeNode extends RubyBaseWithoutContextNode {

    public static IsStringLikeNode create() {
        return IsStringLikeNodeGen.create();
    }

    public abstract boolean executeIsStringLike(Object value);

    @Specialization(guards = "isRubyString(value)")
    boolean isRubyStringStringLike(DynamicObject value) {
        return true;
    }

    @Specialization(guards = "isRubySymbol(value)")
    public boolean isRubySymbolStringLike(DynamicObject value) {
        return true;
    }

    @Specialization
    public boolean isJavaStringStringLike(String value) {
        return true;
    }

    @Specialization(guards = { "!isRubyString(value)", "!isRubySymbol(value)", "!isString(value)" })
    public boolean notStringLike(Object value) {
        return false;
    }

}
