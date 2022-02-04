/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
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

@NodeChild(value = "array", type = RubyNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ArraySliceNode extends RubyContextSourceNode {

    final int from; // positive
    final int to; // negative, exclusive

    public ArraySliceNode(int from, int to) {
        assert from >= 0;
        assert to <= 0;
        this.from = from;
        this.to = to;
    }

    public abstract RubyArray execute(RubyArray array);

    @Specialization
    protected RubyArray readSlice(RubyArray array,
            @Cached ArrayCopyOnWriteNode cowNode,
            @Cached ConditionProfile emptyArray) {
        final int length = array.size + to - from;

        if (emptyArray.profile(length <= 0)) {
            return createEmptyArray();
        } else {
            final Object slice = cowNode.execute(array, from, length);
            return createArray(slice, length);
        }

    }

}
