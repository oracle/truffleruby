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

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class ArrayReadSliceDenormalizedNode extends RubyBaseNode {

    @Child private ArrayReadSliceNormalizedNode readNode = ArrayReadSliceNormalizedNodeGen.create();

    public abstract DynamicObject executeReadSlice(DynamicObject array, int index, int length);

    @Specialization
    public DynamicObject read(DynamicObject array, int index, int length,
            @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile) {
        final int normalizedIndex = ArrayOperations.normalizeIndex(Layouts.ARRAY.getSize(array), index, negativeIndexProfile);

        return readNode.executeReadSlice(array, normalizedIndex, length);
    }

}
