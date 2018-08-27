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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.AllocateObjectNode;

/**
 * Dup an array, without using any method lookup. This isn't a call - it's an operation on a core class.
 */
@ImportStatic(ArrayGuards.class)
public abstract class ArrayDupNode extends RubyBaseNode {

    @Child private AllocateObjectNode allocateNode;

    public abstract DynamicObject executeDup(VirtualFrame frame, DynamicObject array);

    @Specialization(guards = {
            "strategy.matches(from)", "strategy.getSize(from) == cachedSize",
            "cachedSize <= ARRAY_MAX_EXPLODE_SIZE"
    }, limit = "getCacheLimit()")
    public DynamicObject dupProfiledSize(DynamicObject from,
            @Cached("of(from)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached("strategy.getSize(from)") int cachedSize) {
        return copyArraySmall(from, strategy, mutableStrategy, cachedSize);
    }

    @ExplodeLoop
    private DynamicObject copyArraySmall(DynamicObject from, ArrayStrategy strategy, ArrayStrategy mutableStrategy, int cachedSize) {
        final ArrayMirror mirror = strategy.newMirror(from);
        final ArrayMirror copy = mutableStrategy.newArray(cachedSize);
        for (int i = 0; i < cachedSize; i++) {
            copy.set(i, mirror.get(i));
        }
        return allocateArray(coreLibrary().getArrayClass(), copy.getArray(), cachedSize);
    }

    @Specialization(guards = "strategy.matches(from)", replaces = "dupProfiledSize", limit = "STORAGE_STRATEGIES")
    public DynamicObject dup(DynamicObject from,
            @Cached("of(from)") ArrayStrategy strategy) {
        final int size = strategy.getSize(from);
        final ArrayMirror copy = strategy.makeStorageShared(from).extractRange(0, size);
        return allocateArray(coreLibrary().getArrayClass(), copy.getArray(), size);
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
