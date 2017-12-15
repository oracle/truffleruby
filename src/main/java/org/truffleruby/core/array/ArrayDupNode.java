/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.AllocateObjectNode;

/**
 * Dup an array, without using any method lookup. This isn't a call - it's an operation on a core class.
 */
@NodeChild(value = "array", type = RubyNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ArrayDupNode extends RubyNode {

    @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

    public abstract DynamicObject executeDup(VirtualFrame frame, DynamicObject array);

    @Specialization(guards = {
            "strategy.matches(from)", "strategy.getSize(from) == cachedSize",
            "cachedSize <= ARRAY_MAX_EXPLODE_SIZE"
    }, limit = "getCacheLimit()")
    public DynamicObject dupProfiledSize(DynamicObject from,
            @Cached("of(from)") ArrayStrategy strategy,
            @Cached("strategy.getSize(from)") int cachedSize) {
        return copyExplode(from, strategy, cachedSize);
    }

    @ExplodeLoop
    private DynamicObject copyExplode(DynamicObject from, ArrayStrategy strategy, int cachedSize) {
        final ArrayMirror mirror = strategy.newMirror(from);
        final ArrayMirror copy = strategy.newArray(cachedSize);
        for (int i = 0; i < cachedSize; i++) {
            copy.set(i, mirror.get(i));
        }
        return allocateNode.allocateArray(coreLibrary().getArrayClass(), copy.getArray(), cachedSize);
    }

    @Specialization(guards = "strategy.matches(from)", replaces = "dupProfiledSize", limit = "ARRAY_STRATEGIES")
    public DynamicObject dup(DynamicObject from,
            @Cached("of(from)") ArrayStrategy strategy) {
        final int size = strategy.getSize(from);
        Object store = strategy.newMirror(from).copyArrayAndMirror().getArray();
        return allocateNode.allocateArray(coreLibrary().getArrayClass(), store, size);
    }

    protected int getCacheLimit() {
        return getContext().getOptions().ARRAY_DUP_CACHE;
    }

}
