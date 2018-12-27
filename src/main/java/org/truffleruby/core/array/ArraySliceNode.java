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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyNode;

@NodeChildren({@NodeChild(value = "array", type = RubyNode.class)})
@ImportStatic(ArrayGuards.class)
public abstract class ArraySliceNode extends RubyNode {

    final int from; // positive
    final int to; // negative, exclusive

    public ArraySliceNode(int from, int to) {
        assert from >= 0;
        assert to <= 0;
        this.from = from;
        this.to = to;
    }

    @Specialization(guards = { "strategy.matches(array)" }, limit = "STORAGE_STRATEGIES")
    public DynamicObject readInBounds(DynamicObject array,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.extractRangeNode()") ArrayOperationNodes.ArrayExtractRangeNode extractRangeNode,
            @Cached("createBinaryProfile()") ConditionProfile emptyArray) {
        final int to = strategy.getSize(array) + this.to;

        if (emptyArray.profile(from >= to)) {
            return createArray(null, 0);
        } else {
            final Object slice = extractRangeNode.execute(Layouts.ARRAY.getStore(array), from, to);
            return createArray(slice, to - from);
        }

    }

}
