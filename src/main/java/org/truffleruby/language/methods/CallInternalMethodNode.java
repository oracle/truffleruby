/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;

@ReportPolymorphism
@GenerateUncached
public abstract class CallInternalMethodNode extends RubyBaseWithoutContextNode {

    public static CallInternalMethodNode create() {
        return CallInternalMethodNodeGen.create();
    }

    public abstract Object executeCallMethod(InternalMethod method, Object[] frameArguments);

    @Specialization(
            guards = "method.getCallTarget() == cachedCallTarget",
            /* TODO(eregon, 12 June 2015) we should maybe check an Assumption here to remove
                the cache entry when the lookup changes (redefined method, hierarchy changes) */
            limit = "getCacheLimit()")
    protected Object callMethodCached(InternalMethod method, Object[] frameArguments,
            @Cached("method.getCallTarget()") RootCallTarget cachedCallTarget,
            @Cached("create(cachedCallTarget)") DirectCallNode callNode) {
        return callNode.call(frameArguments);
    }

    @Specialization(replaces = "callMethodCached")
    protected Object callMethodUncached(InternalMethod method, Object[] frameArguments,
            @Cached IndirectCallNode indirectCallNode) {
        return indirectCallNode.call(method.getCallTarget(), frameArguments);
    }

    protected int getCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().DISPATCH_CACHE;
    }

}
