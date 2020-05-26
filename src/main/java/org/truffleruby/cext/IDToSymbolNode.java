/*
 * Copyright (c) 20202 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;

@GenerateUncached
@ReportPolymorphism
public abstract class IDToSymbolNode extends RubyBaseNode {

    public abstract Object execute(Object value);

    public static IDToSymbolNode create() {
        return IDToSymbolNodeGen.create();
    }

    @Specialization(guards = { "value >= 0", "value <= 0xff", "value == cachedValue" }, limit = "2")
    protected Object unwrapSingleChar(long value,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("value") long cachedValue,
            @Cached("singleCharSymbol(context, value)") Object symbol) {
        return symbol;
    }

    @Specialization(guards = { "value >= 0", "value <= 0xff" }, replaces = "unwrapSingleChar")
    protected Object unwrapSingleCharUncached(long value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return singleCharSymbol(context, value);
    }

    @Specialization(guards = "!isSingleCharSymbol(value)")
    protected Object unwrapObject(Object value,
            @Cached UnwrapNode unwrapNode) {
        return unwrapNode.execute(value);
    }

    public static boolean isSingleCharSymbol(Object value) {
        if (!(value instanceof Long)) {
            return false;
        }
        long l = (long) value;
        return l >= 0 && l <= 255;
    }

    public static Object singleCharSymbol(RubyContext context, long c) {
        return context.getSymbol(new Character((char) c).toString());
    }
}
