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

import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild(value = "array", type = RubyNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ArrayGetTailNode extends RubyContextSourceNode {

    final int index;

    public ArrayGetTailNode(int index) {
        this.index = index;
    }

    @Specialization(limit = "storageStrategyLimit()")
    protected RubyArray getTail(RubyArray array,
            @CachedLibrary("array.store") ArrayStoreLibrary stores,
            @Cached ArrayCopyOnWriteNode cowNode,
            @Cached ConditionProfile indexLargerThanSize) {
        final int size = array.size;
        if (indexLargerThanSize.profile(index >= size)) {
            return ArrayHelpers.createEmptyArray(getContext());
        } else {
            final Object newStore = cowNode.execute(array, index, size - index);
            return createArray(newStore, size - index);
        }
    }

}
