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
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.language.RubyBaseNode;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayEnsureCapacityNode extends RubyBaseNode {

    public static ArrayEnsureCapacityNode create() {
        return ArrayEnsureCapacityNodeGen.create();
    }

    public abstract Object executeEnsureCapacity(DynamicObject array, int requiredCapacity);

    @Specialization(guards = { "!strategy.isStorageMutable()", "strategy.matches(array)" }, limit = "ARRAY_STRATEGIES")
    public boolean ensureCapacityAndMakeMutable(DynamicObject array, int requiredCapacity,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutationStrategy,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final ArrayMirror mirror = strategy.newMirror(array);

        final int currentCapacity = mirror.getLength();
        final int capacity;
        if (extendProfile.profile(currentCapacity < requiredCapacity)) {
            capacity = ArrayUtils.capacity(getContext(), currentCapacity, requiredCapacity);
        } else {
            capacity = currentCapacity;
        }

        final ArrayMirror newMirror = mutationStrategy.newArray(capacity);
        mirror.copyTo(newMirror, 0, 0, currentCapacity);
        mutationStrategy.setStore(array, newMirror.getArray());
        return true;
    }

    @Specialization(guards = { "strategy.isStorageMutable()", "strategy.matches(array)" }, limit = "STORAGE_STRATEGIES")
    public boolean ensureCapacity(DynamicObject array, int requiredCapacity,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final ArrayMirror mirror = strategy.newMirror(array);

        if (extendProfile.profile(mirror.getLength() < requiredCapacity)) {
            final int capacity = ArrayUtils.capacity(getContext(), mirror.getLength(), requiredCapacity);
            strategy.setStore(array, mirror.copyArrayAndMirror(capacity).getArray());
            return true;
        } else {
            return false;
        }
    }

}
