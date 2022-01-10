/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class IsStringLikeNode extends RubyBaseNode {

    public static IsStringLikeNode create() {
        return IsStringLikeNodeGen.create();
    }

    public abstract boolean executeIsStringLike(Object value);

    @Specialization
    protected boolean isRubyStringStringLike(RubyString value) {
        return true;
    }

    @Specialization
    protected boolean isImmutableRubyStringStringLike(ImmutableRubyString value) {
        return true;
    }

    @Specialization
    protected boolean isRubySymbolStringLike(RubySymbol value) {
        return true;
    }

    @Specialization
    protected boolean isJavaStringStringLike(String value) {
        return true;
    }

    @Specialization(guards = { "isNotRubyString(value)", "!isRubySymbol(value)", "!isString(value)" })
    protected boolean notStringLike(Object value) {
        return false;
    }

}
