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

import static org.truffleruby.core.array.ArrayHelpers.getSize;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayReadSliceNormalizedNode extends RubyContextNode {

    @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

    public abstract Object executeReadSlice(DynamicObject array, int index, int length);

    // Index out of bounds or negative length always gives you nil

    @Specialization(guards = "!indexInBounds(array, index)")
    protected Object readIndexOutOfBounds(DynamicObject array, int index, int length) {
        return nil;
    }

    @Specialization(guards = "!lengthPositive(length)")
    protected Object readNegativeLength(DynamicObject array, int index, int length) {
        return nil;
    }

    // Reading within bounds on an array with actual storage

    @Specialization(
            guards = {
                    "indexInBounds(array, index)",
                    "lengthPositive(length)",
                    "endInBounds(array, index, length)" })
    protected DynamicObject readInBounds(DynamicObject array, int index, int length,
            @Cached ArrayCopyOnWriteNode cowNode) {
        final Object slice = cowNode.execute(array, index, length);
        return createArrayOfSameClass(array, slice, length);
    }

    // Reading beyond upper bounds on an array with actual storage needs clamping

    @Specialization(
            guards = {
                    "indexInBounds(array, index)",
                    "lengthPositive(length)",
                    "!endInBounds(array, index, length)" })
    protected DynamicObject readOutOfBounds(DynamicObject array, int index, int length,
            @Cached ArrayCopyOnWriteNode cowNode) {
        final int end = Layouts.ARRAY.getSize(array);
        final Object slice = cowNode.execute(array, index, end - index);
        return createArrayOfSameClass(array, slice, end - index);
    }

    // Guards

    protected DynamicObject createArrayOfSameClass(DynamicObject array, Object store, int size) {
        return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), store, size);
    }

    protected static boolean indexInBounds(DynamicObject array, int index) {
        return index >= 0 && index <= getSize(array);
    }

    protected static boolean lengthPositive(int length) {
        return length >= 0;
    }

    protected static boolean endInBounds(DynamicObject array, int index, int length) {
        return index + length <= getSize(array);
    }

}
