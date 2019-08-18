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
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayEnsureCapacityNode extends RubyBaseNode {

    public static ArrayEnsureCapacityNode create() {
        return ArrayEnsureCapacityNodeGen.create();
    }

    public abstract Object executeEnsureCapacity(DynamicObject array, int requiredCapacity);

    @Specialization(guards = { "!strategy.isStorageMutable()", "strategy.matches(array)" }, limit = "ARRAY_STRATEGIES")
    protected boolean ensureCapacityAndMakeMutable(DynamicObject array, int requiredCapacity,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutationStrategy,
            @Cached("strategy.capacityNode()") ArrayOperationNodes.ArrayCapacityNode capacityNode,
            @Cached("strategy.copyToNode()") ArrayOperationNodes.ArrayCopyToNode copyToNode,
            @Cached("mutationStrategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = Layouts.ARRAY.getStore(array);

        final int currentCapacity = capacityNode.execute(store);
        final int capacity;
        if (extendProfile.profile(currentCapacity < requiredCapacity)) {
            capacity = ArrayUtils.capacity(getContext(), currentCapacity, requiredCapacity);
        } else {
            capacity = currentCapacity;
        }

        final Object newStore = newStoreNode.execute(capacity);
        copyToNode.execute(store, newStore, 0, 0, currentCapacity);
        mutationStrategy.setStore(array, newStore);
        return true;
    }

    @Specialization(guards = { "strategy.isStorageMutable()", "strategy.matches(array)" }, limit = "STORAGE_STRATEGIES")
    protected boolean ensureCapacity(DynamicObject array, int requiredCapacity,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.capacityNode()") ArrayOperationNodes.ArrayCapacityNode capacityNode,
            @Cached("strategy.copyStoreNode()") ArrayOperationNodes.ArrayCopyStoreNode copyStoreNode,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = Layouts.ARRAY.getStore(array);

        final int length = capacityNode.execute(store);
        if (extendProfile.profile(length < requiredCapacity)) {
            final int capacity = ArrayUtils.capacity(getContext(), length, requiredCapacity);
            strategy.setStore(array, copyStoreNode.execute(store, capacity));
            return true;
        } else {
            return false;
        }
    }

}
