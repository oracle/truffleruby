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
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayEnsureCapacityNode extends RubyContextNode {

    public static ArrayEnsureCapacityNode create() {
        return ArrayEnsureCapacityNodeGen.create();
    }

    public abstract Object executeEnsureCapacity(DynamicObject array, int requiredCapacity);

    @Specialization(guards = "!stores.isMutable(getStore(array))", limit = "storageStrategyLimit()")
    protected boolean ensureCapacityAndMakeMutable(DynamicObject array, int requiredCapacity,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = Layouts.ARRAY.getStore(array);

        final int currentCapacity = stores.capacity(store);
        final int capacity;
        if (extendProfile.profile(currentCapacity < requiredCapacity)) {
            capacity = ArrayUtils.capacity(getContext(), currentCapacity, requiredCapacity);
        } else {
            capacity = currentCapacity;
        }

        final Object newStore = stores.allocator(store).allocate(capacity);
        stores.copyContents(store, 0, newStore, 0, currentCapacity);
        Layouts.ARRAY.setStore(array, newStore);
        return true;
    }

    @Specialization(guards = "stores.isMutable(getStore(array))", limit = "storageStrategyLimit()")
    protected boolean ensureCapacity(DynamicObject array, int requiredCapacity,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = Layouts.ARRAY.getStore(array);

        final int length = stores.capacity(store);
        if (extendProfile.profile(length < requiredCapacity)) {
            final int capacity = ArrayUtils.capacity(getContext(), length, requiredCapacity);
            Layouts.ARRAY.setStore(array, stores.expand(store, capacity));
            return true;
        } else {
            return false;
        }
    }
}
