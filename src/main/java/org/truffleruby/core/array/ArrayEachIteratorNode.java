/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayNodes.ArrayElementConsumerNode;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayEachIteratorNode extends RubyBaseNode {

    @Child private ArrayEachIteratorNode recurseNode;

    public static ArrayEachIteratorNode create() {
        return ArrayEachIteratorNodeGen.create();
    }

    public abstract DynamicObject execute(DynamicObject array, DynamicObject block, int startAt, ArrayElementConsumerNode consumerNode);

    @Specialization(guards = { "strategy.matches(array)", "strategy.getSize(array) == 1", "startAt == 0" }, limit = "STORAGE_STRATEGIES")        
    public DynamicObject iterateOne(DynamicObject array, DynamicObject block, int startAt, ArrayElementConsumerNode consumerNode,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode) {
        final Object store = Layouts.ARRAY.getStore(array);

        consumerNode.accept(array, block, getNode.execute(store, 0), 0);
        if (Layouts.ARRAY.getSize(array) > 1) {
            return getRecurseNode().execute(array, block, 1, consumerNode);
        }
        return array;
    }

    @Specialization(guards = { "strategy.matches(array)", "strategy.getSize(array) != 1" }, limit = "STORAGE_STRATEGIES")
    public DynamicObject iterateMany(DynamicObject array, DynamicObject block, int startAt, ArrayElementConsumerNode consumerNode,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode,
            @Cached("createBinaryProfile()") ConditionProfile strategyMatchProfile) {
        final Object startStore = Layouts.ARRAY.getStore(array);
        Object store = startStore;

        int n = 0;
        int i = startAt;
        try {
            for (; i < strategy.getSize(array); n++, i++) {
                if (strategyMatchProfile.profile(strategy.matches(array))) {
                    consumerNode.accept(array, block, getNode.execute(store, i), i);
                    store = Layouts.ARRAY.getStore(array);
                } else {
                    return getRecurseNode().execute(array, block, i, consumerNode);
                }
            }
        } finally {
            if (CompilerDirectives.inInterpreter()) {
                LoopNode.reportLoopCount(this, n);
            }
        }

        return array;
    }

    private ArrayEachIteratorNode getRecurseNode() {
        if (recurseNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            recurseNode = insert(ArrayEachIteratorNode.create());
        }
        return recurseNode;
    }
}
