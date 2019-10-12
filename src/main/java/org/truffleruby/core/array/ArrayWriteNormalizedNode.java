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
import static org.truffleruby.core.array.ArrayHelpers.setSize;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@ImportStatic(ArrayGuards.class)
@ReportPolymorphism
public abstract class ArrayWriteNormalizedNode extends RubyBaseNode {

    @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

    public abstract Object executeWrite(DynamicObject array, int index, Object value);

    // Writing within an existing array with a compatible type

    @Specialization(
            guards = { "isInBounds(array, index)", "strategy.matches(array)", "strategy.accepts(value)" },
            limit = "STORAGE_STRATEGIES")
    protected Object writeWithin(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
        propagateSharingNode.propagate(array, value);
        setNode.execute(Layouts.ARRAY.getStore(array), index, value);
        return value;
    }

    // Writing within an existing array with an incompatible type - need to generalise

    @Specialization(
            guards = {
                    "isInBounds(array, index)",
                    "currentStrategy.matches(array)",
                    "valueStrategy.specializesFor(value)", },
            limit = "ARRAY_STRATEGIES")
    protected Object writeWithinGeneralizeNonMutable(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy currentStrategy,
            @Cached("forValue(value)") ArrayStrategy valueStrategy,
            @Cached("currentStrategy.generalize(valueStrategy)") ArrayStrategy generalizedStrategy,
            @Cached("currentStrategy.capacityNode()") ArrayOperationNodes.ArrayCapacityNode capacityNode,
            @Cached("currentStrategy.copyToNode()") ArrayOperationNodes.ArrayCopyToNode copyToNode,
            @Cached("generalizedStrategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode,
            @Cached("generalizedStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
        final int size = getSize(array);
        final Object store = Layouts.ARRAY.getStore(array);
        final Object newStore = newStoreNode.execute(capacityNode.execute(store));
        copyToNode.execute(store, newStore, 0, 0, size);
        propagateSharingNode.propagate(array, value);
        setNode.execute(newStore, index, value);
        generalizedStrategy.setStore(array, newStore);
        return value;
    }

    // Extending an array of compatible type by just one

    @Specialization(guards = "isExtendingByOne(array, index)")
    protected Object writeExtendByOne(DynamicObject array, int index, Object value,
            @Cached ArrayAppendOneNode appendNode) {
        appendNode.executeAppendOne(array, value);
        return value;
    }

    // Writing beyond the end of an array - may need to generalize to Object[] or otherwise extend
    @Specialization(
            guards = {
                    "!isInBounds(array, index)",
                    "!isExtendingByOne(array, index)",
                    "strategy.matches(array)",
                    "mutableStrategy.isPrimitive()" },
            limit = "STORAGE_STRATEGIES")
    protected Object writeBeyondPrimitive(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached ArrayGeneralizeNode generalizeNode) {
        final int newSize = index + 1;
        final Object[] objectStore = generalizeNode.executeGeneralize(array, newSize);
        for (int n = strategy.getSize(array); n < index; n++) {
            objectStore[n] = nil();
        }
        propagateSharingNode.propagate(array, value);
        objectStore[index] = value;
        strategy.setStoreAndSize(array, objectStore, newSize);
        return value;
    }

    @Specialization(
            guards = {
                    "!isInBounds(array, index)",
                    "!isExtendingByOne(array, index)",
                    "strategy.matches(array)",
                    "!mutableStrategy.isPrimitive()" },
            limit = "STORAGE_STRATEGIES")
    protected Object writeBeyondObject(DynamicObject array, int index, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached("mutableStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode,
            @Cached ArrayEnsureCapacityNode ensureCapacityNode) {
        ensureCapacityNode.executeEnsureCapacity(array, index + 1);
        final Object store = Layouts.ARRAY.getStore(array);
        for (int n = strategy.getSize(array); n < index; n++) {
            setNode.execute(store, n, nil());
        }
        propagateSharingNode.propagate(array, value);
        setNode.execute(store, index, value);
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
