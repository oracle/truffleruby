/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;

import static org.truffleruby.core.array.ArrayHelpers.setSize;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayPopOneNode extends RubyBaseNode {

    public abstract Object executePopOne(DynamicObject array);

    // Pop from an empty array

    @Specialization(guards = "isEmptyArray(array)")
    public DynamicObject popOneEmpty(DynamicObject array) {
        return nil();
    }

    // Pop from a non-empty array

    @Specialization(guards = { "strategy.matches(array)", "!isEmptyArray(array)" }, limit = "STORAGE_STRATEGIES")
    public Object popOne(DynamicObject array,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode) {
        final int size = Layouts.ARRAY.getSize(array);
        final Object value = getNode.execute(Layouts.ARRAY.getStore(array), size - 1);
        setSize(array, size - 1);
        return value;
    }

}
