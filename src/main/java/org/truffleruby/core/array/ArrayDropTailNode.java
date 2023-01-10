/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild(value = "arrayNode", type = RubyNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ArrayDropTailNode extends RubyContextSourceNode {

    final int index;

    public ArrayDropTailNode(int index) {
        this.index = index;
    }

    public abstract RubyNode getArrayNode();

    @Specialization
    protected RubyArray dropTail(RubyArray array,
            @Cached ArrayCopyOnWriteNode cowNode,
            @Cached ConditionProfile indexLargerThanSize) {
        final int size = array.size;
        if (indexLargerThanSize.profile(index >= size)) {
            return createEmptyArray();
        } else {
            final int newSize = size - index;
            final Object withoutTail = cowNode.execute(array, 0, newSize);
            return createArray(withoutTail, newSize);
        }
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = ArrayDropTailNodeGen.create(
                index,
                getArrayNode().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
