/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild("array")
@NodeChild("index")
public abstract class ArrayReadDenormalizedNode extends RubyNode {

    @Child private ArrayReadNormalizedNode readNode = ArrayReadNormalizedNodeGen.create(null, null);

    public abstract Object executeRead(DynamicObject array, int index);

    @Specialization
    protected Object read(DynamicObject array, int index,
            @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile) {
        final int normalizedIndex = ArrayOperations
                .normalizeIndex(Layouts.ARRAY.getSize(array), index, negativeIndexProfile);

        return readNode.executeRead(array, normalizedIndex);
    }

}
