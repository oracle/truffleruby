/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayGetNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayNewStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArraySetNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Dup an array, without using any method lookup. This isn't a call - it's an operation on a core class.
 */
@ImportStatic(ArrayGuards.class)
public abstract class ArrayDupNode extends RubyBaseNode {

    @Child private AllocateObjectNode allocateNode;

    public abstract DynamicObject executeDup(VirtualFrame frame, DynamicObject array);

    @Specialization(
            guards = {
                    "strategy.matches(from)",
                    "strategy.getSize(from) == cachedSize",
                    "cachedSize <= ARRAY_MAX_EXPLODE_SIZE" },
            limit = "getCacheLimit()")
    protected DynamicObject dupProfiledSize(DynamicObject from,
            @Cached("of(from)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached("strategy.getSize(from)") int cachedSize,
            @Cached("strategy.getNode()") ArrayGetNode getNode,
            @Cached("mutableStrategy.setNode()") ArraySetNode setNode,
            @Cached("mutableStrategy.newStoreNode()") ArrayNewStoreNode newStoreNode) {
        return copyArraySmall(newStoreNode, getNode, setNode, from, cachedSize);
    }

    @ExplodeLoop
    private DynamicObject copyArraySmall(ArrayNewStoreNode newStoreNode, ArrayGetNode getNode, ArraySetNode setNode,
            DynamicObject from, int cachedSize) {
        final Object original = Layouts.ARRAY.getStore(from);
        final Object copy = newStoreNode.execute(cachedSize);
        for (int i = 0; i < cachedSize; i++) {
            setNode.execute(copy, i, getNode.execute(original, i));
        }
        return allocateArray(coreLibrary().getArrayClass(), copy, cachedSize);
    }

    @Specialization(guards = "strategy.matches(from)", replaces = "dupProfiledSize", limit = "STORAGE_STRATEGIES")
    protected DynamicObject dup(DynamicObject from,
            @Cached("of(from)") ArrayStrategy strategy,
            @Cached("strategy.extractRangeCopyOnWriteNode()") ArrayOperationNodes.ArrayExtractRangeCopyOnWriteNode extractRangeCopyOnWriteNode) {
        final int size = strategy.getSize(from);
        final Object copy = extractRangeCopyOnWriteNode.execute(from, 0, size);
        return allocateArray(coreLibrary().getArrayClass(), copy, size);
    }

    private DynamicObject allocateArray(DynamicObject arrayClass, Object store, int size) {
        if (allocateNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            allocateNode = insert(AllocateObjectNode.create());
        }
        return allocateNode.allocateArray(arrayClass, store, size);
    }

    protected int getCacheLimit() {
        return getContext().getOptions().ARRAY_DUP_CACHE;
    }

}
