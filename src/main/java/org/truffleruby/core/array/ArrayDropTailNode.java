/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild("array")
@ImportStatic(ArrayGuards.class)
public abstract class ArrayDropTailNode extends RubyNode {

    final int index;

    public ArrayDropTailNode(int index) {
        this.index = index;
    }

    @Specialization(guards = "strategy.matches(array)", limit = "STORAGE_STRATEGIES")
    protected DynamicObject dropTail(DynamicObject array,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.extractRangeCopyOnWriteNode()") ArrayOperationNodes.ArrayExtractRangeCopyOnWriteNode extractRangeCopyOnWriteNode,
            @Cached("createBinaryProfile()") ConditionProfile indexLargerThanSize) {
        final int size = strategy.getSize(array);
        if (indexLargerThanSize.profile(index >= size)) {
            return createArray(null, 0);
        } else {
            final int newSize = size - index;
            final Object withoutTail = extractRangeCopyOnWriteNode.execute(array, 0, newSize);
            return createArray(withoutTail, newSize);
        }
    }

}
