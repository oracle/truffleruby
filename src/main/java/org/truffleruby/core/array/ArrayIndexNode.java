/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.AllocateObjectNode;

import static org.truffleruby.core.array.ArrayHelpers.getSize;

public abstract class ArrayIndexNode extends ArrayCoreMethodNode {

    @Child private ArrayReadDenormalizedNode readNode;
    @Child private ArrayReadSliceDenormalizedNode readSliceNode;
    @Child private ArrayReadSliceNormalizedNode readNormalizedSliceNode;
    @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

    @Specialization
    protected Object index(DynamicObject array, int index, NotProvided length) {
        if (readNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readNode = insert(ArrayReadDenormalizedNodeGen.create(null, null));
        }
        return readNode.executeRead(array, index);
    }

    @Specialization
    protected DynamicObject slice(DynamicObject array, int start, int length) {
        if (length < 0) {
            return nil();
        }

        if (readSliceNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readSliceNode = insert(ArrayReadSliceDenormalizedNodeGen.create());
        }

        return readSliceNode.executeReadSlice(array, start, length);
    }

    @Specialization(guards = "isIntRange(range)")
    protected DynamicObject slice(DynamicObject array, DynamicObject range, NotProvided len,
            @Cached("createBinaryProfile()") ConditionProfile negativeBeginProfile,
            @Cached("createBinaryProfile()") ConditionProfile negativeEndProfile,
            @Cached("create()") ArrayReadSliceNormalizedNode readNormalizedSliceNode) {
        final int size = getSize(array);
        final int normalizedBegin = ArrayOperations
                .normalizeIndex(size, Layouts.INT_RANGE.getBegin(range), negativeBeginProfile);

        if (normalizedBegin < 0 || normalizedBegin > size) {
            return nil();
        } else {
            final int end = ArrayOperations
                    .normalizeIndex(size, Layouts.INT_RANGE.getEnd(range), negativeEndProfile);
            final int exclusiveEnd = ArrayOperations
                    .clampExclusiveIndex(size, Layouts.INT_RANGE.getExcludedEnd(range) ? end : end + 1);

            if (exclusiveEnd <= normalizedBegin) {
                return allocateObjectNode
                        .allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), ArrayStrategy.NULL_ARRAY_STORE, 0);
            }

            return readNormalizedSliceNode.executeReadSlice(array, normalizedBegin, exclusiveEnd - normalizedBegin);
        }
    }

    @Specialization(guards = "isFallback(index, maybeLength)")
    protected Object fallbackIndex(DynamicObject array, Object index, Object maybeLength) {
        return fallback(array, index, maybeLength);
    }

    protected boolean isFallback(Object index, Object length) {
        return (!RubyGuards.isInteger(index) && !RubyGuards.isIntRange(index)) ||
                (RubyGuards.wasProvided(length) && !RubyGuards.isInteger(length));
    }

    protected Object fallback(DynamicObject array, Object start, Object length) {
        throw new AbstractMethodError();
    }

}
