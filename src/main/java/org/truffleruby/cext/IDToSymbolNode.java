/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.profiles.BranchProfile;

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

    @Specialization(guards = "isSingleCharSymbol(value)")
    protected Object unwrapSingleCharUncached(long value,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached BranchProfile profile) {
        return context.getSymbolTable().getSingleByteSymbol((char) value, profile);
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
}
