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

import org.truffleruby.language.RubyBaseNode;

import static org.truffleruby.core.array.ArrayHelpers.getSize;
import static org.truffleruby.core.array.ArrayHelpers.setSize;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayWriteNormalizedNode extends RubyBaseNode {

    public abstract Object executeWrite(DynamicObject array, int index, Object value);

    // Writing within an existing array with a compatible type

    @Specialization(guards = {
            "isInBounds(array, index)", "strategy.matches(array)", "strategy.accepts(value)"
    }, limit = "STORAGE_STRATEGIES")
    public Object writeWithin(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy strategy) {
        strategy.newMirror(array).set(index, value);
        return value;
    }

    // Writing within an existing array with an incompatible type - need to generalise

    @Specialization(guards = {
            "isInBounds(array, index)", "currentStrategy.matches(array)",
            "valueStrategy.specializesFor(value)",
    }, limit = "ARRAY_STRATEGIES")
    public Object writeWithinGeneralizeNonMutable(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy currentStrategy,
            @Cached("forValue(value)") ArrayStrategy valueStrategy,
            @Cached("currentStrategy.generalize(valueStrategy)") ArrayStrategy generalizedStrategy) {
        final int size = getSize(array);
        final ArrayMirror currentMirror = currentStrategy.newMirror(array);
        final ArrayMirror storeMirror = generalizedStrategy.newArray(currentMirror.getLength());
        currentMirror.copyTo(storeMirror, 0, 0, size);
        storeMirror.set(index, value);
        generalizedStrategy.setStore(array, storeMirror.getArray());
        return value;
    }

    // Extending an array of compatible type by just one

    @Specialization(guards = "isExtendingByOne(array, index)")
    public Object writeExtendByOne(DynamicObject array, int index, Object value,
                    @Cached("create()") ArrayAppendOneNode appendNode) {
        appendNode.executeAppendOne(array, value);
        return value;
    }

    // Writing beyond the end of an array - may need to generalize to Object[] or otherwise extend
    @Specialization(guards = {
            "!isInBounds(array, index)", "!isExtendingByOne(array, index)", "strategy.matches(array)", "mutableStrategy.isPrimitive()"
    }, limit = "STORAGE_STRATEGIES")
    public Object writeBeyondPrimitive(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached("create()") ArrayGeneralizeNode generalizeNode) {
        final int newSize = index + 1;
        final Object[] objectStore = generalizeNode.executeGeneralize(array, newSize);
        for (int n = strategy.getSize(array); n < index; n++) {
            objectStore[n] = nil();
        }
        objectStore[index] = value;
        strategy.setStoreAndSize(array, objectStore, newSize);
        return value;
    }

    @Specialization(guards = {
            "!isInBounds(array, index)", "!isExtendingByOne(array, index)", "strategy.matches(array)", "!mutableStrategy.isPrimitive()"
    }, limit = "STORAGE_STRATEGIES")
    public Object writeBeyondObject(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached("create()") ArrayEnsureCapacityNode ensureCapacityNode) {
        ensureCapacityNode.executeEnsureCapacity(array, index + 1);
        final ArrayMirror store = mutableStrategy.newMirror(array);
        for (int n = strategy.getSize(array); n < index; n++) {
            store.set(n, nil());
        }
        store.set(index, value);
        setSize(array, index + 1);
        return value;
    }

    // Guards

    protected static boolean isInBounds(DynamicObject array, int index) {
        return index >= 0 && index < getSize(array);
    }

    protected static boolean isExtendingByOne(DynamicObject array, int index) {
        return index == getSize(array);
    }

}
