/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.NodeInterface;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayEachIteratorNode extends RubyContextNode {

    public static interface ArrayElementConsumerNode extends NodeInterface {
        public abstract void accept(DynamicObject array, DynamicObject block, Object element, int index);
    }

    @Child private ArrayEachIteratorNode recurseNode;

    public static ArrayEachIteratorNode create() {
        return ArrayEachIteratorNodeGen.create();
    }

    public abstract DynamicObject execute(DynamicObject array, DynamicObject block, int startAt,
            ArrayElementConsumerNode consumerNode);

    @Specialization(
            guards = { "getSize(array) == 1", "startAt == 0" },
            limit = "STORAGE_STRATEGIES")
    protected DynamicObject iterateOne(DynamicObject array, DynamicObject block, int startAt,
            ArrayElementConsumerNode consumerNode,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays) {
        final Object store = Layouts.ARRAY.getStore(array);

        consumerNode.accept(array, block, arrays.read(store, 0), 0);

        if (Layouts.ARRAY.getSize(array) > 1) {
            // Implicitly profiles through lazy node creation
            return getRecurseNode().execute(array, block, 1, consumerNode);
        }

        return array;
    }

    @Specialization(
            guards = { "getSize(array) != 1" },
            limit = "STORAGE_STRATEGIES")
    protected DynamicObject iterateMany(DynamicObject array, DynamicObject block, int startAt,
            ArrayElementConsumerNode consumerNode,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
            @Cached("createBinaryProfile()") ConditionProfile strategyMatchProfile) {
        int i = startAt;
        try {
            for (; i < Layouts.ARRAY.getSize(array); i++) {
                if (strategyMatchProfile.profile(arrays.accepts(Layouts.ARRAY.getStore(array)))) {
                    final Object store = Layouts.ARRAY.getStore(array);
                    consumerNode.accept(array, block, arrays.read(store, i), i);
                } else {
                    return getRecurseNode().execute(array, block, i, consumerNode);
                }
            }
        } finally {
            if (CompilerDirectives.inInterpreter()) {
                LoopNode.reportLoopCount(this, i - startAt);
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
