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

import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

@GenerateUncached
@ReportPolymorphism
public abstract class SymbolToIDNode extends RubyBaseNode {

    public abstract Object execute(Object value);

    @Specialization(guards = "symbol == cachedSymbol", limit = "2")
    protected Object getIDCached(RubySymbol symbol,
            @Cached WrapNode wrapNode,
            @Cached("symbol") RubySymbol cachedSymbol,
            @Cached("getID(cachedSymbol, wrapNode)") Object cachedID) {
        return cachedID;
    }

    @Specialization(replaces = "getIDCached")
    protected Object getIDUncached(RubySymbol symbol,
            @Cached WrapNode wrapNode,
            @Cached ConditionProfile staticSymbolProfile) {
        if (staticSymbolProfile.profile(symbol.getId() != RubySymbol.UNASSIGNED_ID)) {
            return symbol.getId();
        }
        return wrapNode.execute(symbol);
    }

    protected Object getID(RubySymbol symbol,
            WrapNode wrapNode) {
        if (symbol.getId() != RubySymbol.UNASSIGNED_ID) {
            return symbol.getId();
        }
        return wrapNode.execute(symbol);
    }
}
